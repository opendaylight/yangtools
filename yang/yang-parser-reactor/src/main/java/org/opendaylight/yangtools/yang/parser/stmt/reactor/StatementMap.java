/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableList;
import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Simple integer-to-StatementContextBase map optimized for size and restricted in scope of operations. It does not
 * implement {@link java.util.Map} for simplicity's sake.
 *
 * @author Robert Varga
 */
abstract class StatementMap {
    private static final class Empty extends StatementMap {
        @Override
        StatementContextBase<?, ?, ?> get(final int index) {
            return null;
        }

        @Override
        StatementMap put(final int index, final StatementContextBase<?, ?, ?> obj) {
            return index == 0 ? new Singleton(obj) : new Regular(index, obj);
        }

        @Override
        Collection<StatementContextBase<?, ?, ?>> values() {
            return ImmutableList.of();
        }

        @Override
        int size() {
            return 0;
        }

        @Override
        StatementMap ensureCapacity(final int expectedLimit) {
            return expectedLimit < 2 ? this : new Regular(expectedLimit);
        }

        @Override
        int capacity() {
            return 0;
        }
    }

    private static final class Regular extends StatementMap {
        private StatementContextBase<?, ?, ?>[] elements;
        private int size;

        Regular(final int expectedLimit) {
            elements = new StatementContextBase<?, ?, ?>[expectedLimit];
        }

        Regular(final int index, final StatementContextBase<?, ?, ?> object) {
            this(index + 1, index, object);
        }

        Regular(final StatementContextBase<?, ?, ?> object0, final int index,
                final StatementContextBase<?, ?, ?> object) {
            this(index + 1, 0, object0);
            elements[index] = requireNonNull(object);
            size = 2;
        }

        Regular(final int expectedLimit, final int index, final StatementContextBase<?, ?, ?> object) {
            this(expectedLimit);
            elements[index] = requireNonNull(object);
            size = 1;
        }

        @Override
        StatementContextBase<?, ?, ?> get(final int index) {
            return index >= elements.length ? null : elements[index];
        }

        @Override
        StatementMap put(final int index, final StatementContextBase<?, ?, ?> obj) {
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
        Collection<StatementContextBase<?, ?, ?>> values() {
            return new RegularAsCollection<>(elements, size);
        }

        @Override
        int size() {
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
        int capacity() {
            return elements.length;
        }
    }

    private static final class RegularAsCollection<T> extends AbstractCollection<T> {
        private final T[] elements;
        private final int size;

        RegularAsCollection(final T[] elements, final int size) {
            this.elements = requireNonNull(elements);
            this.size = size;
        }

        @Override
        public void forEach(final Consumer<? super T> action) {
            for (T e : elements) {
                if (e != null) {
                    action.accept(e);
                }
            }
        }

        @Override
        public Iterator<T> iterator() {
            return new AbstractIterator<T>() {
                private int nextOffset = 0;

                @Override
                protected T computeNext() {
                    while (nextOffset < elements.length) {
                        final T ret = elements[nextOffset++];
                        if (ret != null) {
                            return ret;
                        }
                    }

                    return endOfData();
                }
            };
        }

        @Override
        public int size() {
            return size;
        }
    }

    private static final class Singleton extends StatementMap {
        private final StatementContextBase<?, ?, ?> object;

        Singleton(final StatementContextBase<?, ?, ?> object) {
            this.object = requireNonNull(object);
        }

        @Override
        StatementContextBase<?, ?, ?> get(final int index) {
            return index == 0 ? object : null;
        }

        @Override
        StatementMap put(final int index, final StatementContextBase<?, ?, ?> obj) {
            checkArgument(index != 0);
            return new Regular(this.object, index, obj);
        }

        @Override
        Collection<StatementContextBase<?, ?, ?>> values() {
            return ImmutableList.of(object);
        }

        @Override
        int size() {
            return 1;
        }

        @Override
        StatementMap ensureCapacity(final int expectedLimit) {
            return expectedLimit < 2 ? this : new Regular(expectedLimit, 0, object);
        }

        @Override
        int capacity() {
            return 1;
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
    abstract @Nullable StatementContextBase<?, ?, ?> get(int index);

    /**
     * Add a statement at specified index.
     *
     * @param index Element index, must be non-negative
     * @param obj Object to store
     * @return New statement map
     * @throws IllegalArgumentException if the index is already occupied
     */
    abstract @Nonnull StatementMap put(int index, @Nonnull StatementContextBase<?, ?, ?> obj);

    /**
     * Return a read-only view of the elements in this map. Unlike other maps, this view does not detect concurrent
     * modification. Iteration is performed in order of increasing offset. In face of concurrent modification, number
     * of elements returned through iteration may not match the size reported via {@link Collection#size()}.
     *
     * @return Read-only view of available statements.
     */
    abstract @Nonnull Collection<StatementContextBase<?, ?, ?>> values();

    abstract int size();

    abstract StatementMap ensureCapacity(int expectedLimit);

    abstract int capacity();
}
