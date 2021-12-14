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
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementOrigin;

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

        @Override
        Collection<? extends StatementContextBase<?, ?, ?>> effectiveView() {
            return this;
        }
    }

    private static final class Regular extends AbstractNonEmpty {
        private AbstractResumedStatement<?, ?, ?>[] elements;
        private int size;

        Regular(final int expectedLimit) {
            super(0);
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
            return new Iter(this);
        }

        private static final class Iter extends AbstractIterator<AbstractResumedStatement<?, ?, ?>> {
            private int nextOffset = 0;
            private Regular map;

            Iter(final Regular map) {
                this.map = requireNonNull(map);
            }

            @Override
            protected AbstractResumedStatement<?, ?, ?> computeNext() {
                while (nextOffset < map.elements.length) {
                    final AbstractResumedStatement<?, ?, ?> ret = map.elements[nextOffset++];
                    if (ret != null) {
                        return ret;
                    }
                }

                map = null;
                return endOfData();
            }
        }
    }

    private abstract static class AbstractNonEmpty extends StatementMap {
        private final int implicitCount;

        AbstractNonEmpty(final int implicitCount) {
            this.implicitCount = implicitCount;
        }

        @Override
        Collection<? extends StatementContextBase<?, ?, ?>> effectiveView() {
            return implicitCount == 0 ? this : Collections2.transform(this, stmt -> {
                final var origin = stmt.origin();
                switch (origin) {
                case CONTEXT:
                    // FIXME: we really mean: effective view
                    final var sub = Iterables.getOnlyElement(stmt.declaredSubstatements());
                    verify(sub instanceof StatementContextBase, "Unexpected context %s", sub);
                    return (StatementContextBase<?, ?, ?>) sub;
                case DECLARATION:
                    return stmt;
                default:
                    throw new IllegalStateException("Unhandled origin " + origin);
                }
            });
        }
    }

    private static final class Singleton extends AbstractNonEmpty {
        private final AbstractResumedStatement<?, ?, ?> object;

        Singleton(final AbstractResumedStatement<?, ?, ?> object) {
            super(object.origin() == StatementOrigin.DECLARATION ? 0 : 1);
            this.object = requireNonNull(object);
        }

        @Override
        AbstractResumedStatement<?, ?, ?> get(final int index) {
            return index == 0 ? object : null;
        }

        @Override
        StatementMap put(final int index, final AbstractResumedStatement<?, ?, ?> obj) {
            checkArgument(index != 0);
            return new Regular(object, index, obj);
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

    private static final @NonNull StatementMap EMPTY = new Empty();

    static @NonNull StatementMap empty() {
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

    /**
     * Ensure storage space for at least {@code explectedLimit} substatements.
     *
     * @param expectedLimit Expected number of substatements
     * @return New statement map
     */
    abstract @NonNull StatementMap ensureCapacity(int expectedLimit);

    /**
     * Return the effective view of this map. Returned collection accounts for the difference in substatement structure
     * between {@link DeclaredStatement#declaredSubstatements()} and
     * {@link EffectiveStatement#effectiveSubstatements()}. The former, as visible via {@link #iterator()}, does not
     * include implicit structure, such as introduced by {@code case} statements implied by {@code choice}
     * substatements.
     *
     * @return Substatements including implicit parent statements
     */
    abstract @NonNull Collection<? extends StatementContextBase<?, ?, ?>> effectiveView();
}
