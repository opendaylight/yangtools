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

import com.google.common.base.Preconditions;
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
        StatementMap put(final int index, final StatementContextBase<?, ?, ?> object) {
            return index == 0 ? new Singleton(object) : new Regular(index, object);
        }

        @Override
        Collection<StatementContextBase<?, ?, ?>> values() {
            return ImmutableList.of();
        }
    }

    private static final class Regular extends StatementMap {
        private StatementContextBase<?, ?, ?>[] elements;

        Regular(final int index, final StatementContextBase<?, ?, ?> object) {
            elements = new StatementContextBase<?, ?, ?>[index + 1];
            elements[index] = requireNonNull(object);
        }

        Regular(final StatementContextBase<?, ?, ?> object0, final int index,
                final StatementContextBase<?, ?, ?> object) {
            elements = new StatementContextBase<?, ?, ?>[index + 1];
            elements[0] = requireNonNull(object0);
            elements[index] = requireNonNull(object);
        }

        @Override
        StatementContextBase<?, ?, ?> get(final int index) {
            if (index >= elements.length) {
                return null;
            }

            return elements[index];
        }

        @Override
        StatementMap put(final int index, final StatementContextBase<?, ?, ?> object) {
            if (index < elements.length) {
                checkArgument(elements[index] == null);
            } else {
                elements = Arrays.copyOf(elements, index + 1);
            }

            elements[index] = requireNonNull(object);
            return this;
        }

        @Override
        Collection<StatementContextBase<?, ?, ?>> values() {
            return new RegularAsCollection<>(elements);
        }
    }

    private static final class RegularAsCollection<T> extends AbstractCollection<T> {
        private final T[] elements;

        RegularAsCollection(final T[] elements) {
            this.elements = Preconditions.checkNotNull(elements);
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
        public boolean isEmpty() {
            // This has a single-use and when it is instantiated, we know to have at least two items
            return false;
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
            // Optimized for non-sparse case
            int nulls = 0;
            for (T e : elements) {
                if (e == null) {
                    nulls++;
                }
            }

            return elements.length - nulls;
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
        StatementMap put(final int index, final StatementContextBase<?, ?, ?> object) {
            checkArgument(index != 0);
            return new Regular(this.object, index, object);
        }

        @Override
        Collection<StatementContextBase<?, ?, ?>> values() {
            return ImmutableList.of(object);
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
     * @param object Object to store
     * @return New statement map
     * @throws IllegalArgumentException if the index is already occupied
     */
    abstract @Nonnull StatementMap put(int index, @Nonnull StatementContextBase<?, ?, ?> object);

    /**
     * Return a read-only view of the elements in this map. Unlike other maps, this view does not detect concurrent
     * modification. Iteration is performed in order of increasing offset. In face of concurrent modification, number
     * of elements returned through iteration may not match the size reported via {@link Collection#size()}.
     *
     * @return Read-only view of available statements.
     */
    abstract @Nonnull Collection<StatementContextBase<?, ?, ?>> values();
}
