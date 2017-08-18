/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.UnmodifiableIterator;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import javax.annotation.Nonnull;

/**
 * Internal array-backed {@link List}. It assumes the array does not contain nulls and it does not get modified
 * externally. These assumptions are not checked. It does not allow modification of the underlying array -- thus it
 * is very useful for use with {@link ImmutableOffsetMap}.
 *
 * @param <E> the type of elements in this list
 */
final class ConstantArrayCollection<E> implements Collection<E>, Serializable {
    private static final long serialVersionUID = 1L;
    private final E[] array;

    ConstantArrayCollection(final E[] array) {
        this.array = requireNonNull(array);
    }

    @Override
    public int size() {
        return array.length;
    }

    @Override
    public boolean isEmpty() {
        return array.length == 0;
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean contains(final Object o) {
        for (Object wlk : array) {
            if (o.equals(wlk)) {
                return true;
            }
        }
        return false;
    }

    @Nonnull
    @Override
    public Iterator<E> iterator() {
        return new UnmodifiableIterator<E>() {
            private int offset = 0;

            @Override
            public boolean hasNext() {
                return offset < array.length;
            }

            @Override
            public E next() {
                if (offset >= array.length) {
                    throw new NoSuchElementException();
                }
                return array[offset++];
            }
        };
    }

    @Nonnull
    @Override
    public Object[] toArray() {
        return array.clone();
    }

    @Nonnull
    @SuppressWarnings({ "unchecked", "checkstyle:parameterName" })
    @Override
    public <T> T[] toArray(@Nonnull final T[] a) {
        if (a.length < array.length) {
            return Arrays.copyOf(array, array.length, (Class<T[]>)a.getClass().getComponentType());
        }

        System.arraycopy(array, 0, a, 0, array.length);
        if (a.length > array.length) {
            a[array.length] = null;
        }
        return a;
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean add(final E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean remove(final Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean containsAll(@Nonnull final Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }

        return true;
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean addAll(@Nonnull final Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean removeAll(@Nonnull final Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean retainAll(@Nonnull final Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int hashCode() {
        int result = 1;
        for (E e : array) {
            result = 31 * result + e.hashCode();
        }
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ConstantArrayCollection)) {
            return false;
        }

        return Arrays.equals(array, ((ConstantArrayCollection<?>) obj).array);
    }

    @Override
    public String toString() {
        if (array.length == 0) {
            return "[]";
        }

        final StringBuilder sb = new StringBuilder("[");
        int offset = 0;
        while (offset < array.length - 1) {
            sb.append(String.valueOf(array[offset++])).append(", ");
        }
        return sb.append(String.valueOf(array[offset])).append(']').toString();
    }
}
