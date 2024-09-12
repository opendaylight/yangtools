/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.data;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterators;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.function.Predicate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.Ordering;

/**
 * An array value, corresponding to a {@code leaf-list} statement. It holds a number of distinct elements. The encounter
 * value may or may not be significant, based on {@link #ordering()}.
 *
 * @param <E> type of individual elements
 */
@NonNullByDefault
public abstract sealed class ArrayValue<E extends ScalarValue> extends AbstractCollection<E> implements Value
        permits SystemArrayValue, UserArrayValue {
    /**
     * Return this value's ordering.
     *
     * @return this value's ordering.
     */
    public abstract Ordering ordering();

    @Override
    @Deprecated
    public final boolean add(final E element) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public final boolean remove(final @Nullable Object obj) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public final boolean addAll(final @Nullable Collection<? extends E> coll) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public final boolean removeAll(final @Nullable Collection<?> coll) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public final boolean removeIf(final @Nullable Predicate<? super E> filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public final boolean retainAll(final @Nullable Collection<?> coll) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public final void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(ArrayValue.class)
            .add("ordering", ordering())
            .add("elements", Iterators.toString(iterator()))
            .toString();
    }
}
