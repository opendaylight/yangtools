/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Simple integer-to-StatementContextBase map optimized for size and restricted in scope of operations. It does not
 * implement {@link java.util.Map} for simplicity's sake.
 *
 * <p>
 * Unlike other collections, this view does not detect concurrent modification. Iteration is performed in order of
 * increasing offset. In face of concurrent modification, number of elements returned through iteration may not match
 * the size reported via {@link Collection#size()}.
 *
 * @author Robert Varga
 */
abstract class StatementMap extends AbstractCollection<AbstractResumedStatement<?, ?, ?>> {
    private static final class Empty extends StatementMap {
        private static final Iterator<AbstractResumedStatement<?, ?, ?>> EMPTY_ITERATOR;

        static {
            // This may look weird, but we really want to return two Iterator implementations from StatementMap, so that
            // users have to deal with bimorphic invocation. Note that we want to invoke hasNext() here, as we want to
            // initialize state to AbstractIterator.endOfData().
            final Iterator<AbstractResumedStatement<?, ?, ?>> it = new Regular(0).iterator();
            verify(!it.hasNext());
            EMPTY_ITERATOR = it;
        }

        @Override
        AbstractResumedStatement<?, ?, ?> get(final int index) {
            return null;
        }

        @Override
        StatementMap put(final int index, final AbstractResumedStatement<?, ?, ?> obj) {
            return index == 0 ? new Singleton(obj) : new Regular(index, obj);
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        StatementMap ensureCapacity(final int expectedLimit) {
            return expectedLimit < 2 ? this : new Regular(expectedLimit);
        }

        @Override
        public void forEach(final Consumer<? super AbstractResumedStatement<?, ?, ?>> action) {
            // No-op
        }

        @Override
        public Iterator<AbstractResumedStatement<?, ?, ?>> iterator() {
            return EMPTY_ITERATOR;
        }
    }

    private static final class Regular extends StatementMap {
        private AbstractResumedStatement<?, ?, ?>[] elements;
        private int size;

        Regular(final int expectedLimit) {
            elements = new AbstractResumedStatement<?, ?, ?>[expectedLimit];
        }

        Regular(final int index, final AbstractResumedStatement<?, ?, ?> object) {
            this(index + 1, index, object);
        }

        Regular(final AbstractResumedStatement<?, ?, ?> object0, final int index,
                final AbstractResumedStatement<?, ?, ?> object) {
            this(index + 1, 0, object0);
            elements[index] = requireNonNull(object);
            size = 2;
        }

        Regular(final int expectedLimit, final int index, final AbstractResumedStatement<?, ?, ?> object) {
            this(expectedLimit);
            elements[index] = requireNonNull(object);
            size = 1;
        }

        @Override
        AbstractResumedStatement<?, ?, ?> get(final int index) {
            return index >= elements.length ? null : elements[index];
        }

        @Override
        StatementMap put(final int index, final AbstractResumedStatement<?, ?, ?> obj) {
            if (index < elements.length) {
                checkArgument(elements[index] == null);
            } else {
                // FIXME: detect linear growth
                elements = Arrays.copyOf(elements, index + 1);
            }

            elements[index] = requireNonNull(obj);
            size++;
            return this;
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        StatementMap ensureCapacity(final int expectedLimit) {
            if (elements.length < expectedLimit) {
                elements = Arrays.copyOf(elements, expectedLimit);
            }
            return this;
        }

        @Override
        public Iterator<AbstractResumedStatement<?, ?, ?>> iterator() {
            return new AbstractIterator<>() {
                private int nextOffset = 0;

                @Override
                protected AbstractResumedStatement<?, ?, ?> computeNext() {
                    while (nextOffset < elements.length) {
                        final AbstractResumedStatement<?, ?, ?> ret = elements[nextOffset++];
                        if (ret != null) {
                            return ret;
                        }
                    }

                    return endOfData();
                }
            };
        }
    }

    private static final class Singleton extends StatementMap {
        private final AbstractResumedStatement<?, ?, ?> object;

        Singleton(final AbstractResumedStatement<?, ?, ?> object) {
            this.object = requireNonNull(object);
        }

        @Override
        AbstractResumedStatement<?, ?, ?> get(final int index) {
            return index == 0 ? object : null;
        }

        @Override
        StatementMap put(final int index, final AbstractResumedStatement<?, ?, ?> obj) {
            checkArgument(index != 0);
            return new Regular(this.object, index, obj);
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        StatementMap ensureCapacity(final int expectedLimit) {
            return expectedLimit < 2 ? this : new Regular(expectedLimit, 0, object);
        }

        @Override
        public Iterator<AbstractResumedStatement<?, ?, ?>> iterator() {
            return Iterators.singletonIterator(object);
        }
    }

    private static final StatementMap EMPTY = new Empty();

    static StatementMap empty() {
        return EMPTY;
    }

    /**
     * Return the statement context at specified index.
     *
     * @param index Element index, must be non-negative
     * @return Requested element or null if there is no element at that index
     */
    abstract @Nullable AbstractResumedStatement<?, ?, ?> get(int index);

    /**
     * Add a statement at specified index.
     *
     * @param index Element index, must be non-negative
     * @param obj Object to store
     * @return New statement map
     * @throws IllegalArgumentException if the index is already occupied
     */
    abstract @NonNull StatementMap put(int index, @NonNull AbstractResumedStatement<?, ?, ?> obj);

    abstract @NonNull StatementMap ensureCapacity(int expectedLimit);
}
