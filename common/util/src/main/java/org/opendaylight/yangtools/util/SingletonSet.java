/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Iterators;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * A {@link Set} containing a single value. For some reason neither Java nor Guava provide direct access to the retained
 * element -- which is desirable in some situations, as is the case in {@link SharedSingletonMap#entrySet()}.
 */
public abstract sealed class SingletonSet<E> implements Set<E>, Immutable, Serializable {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    public static <E> @NonNull SingletonSet<E> of(final @Nullable E element) {
        return element == null ? (SingletonSet<E>) NullElement.INSTANCE : new Regular<>(element);
    }

    /**
     * Return the single element contained in this set.
     *
     * @return This set's element.
     */
    public abstract E getElement();

    @Override
    public final int size() {
        return 1;
    }

    @Override
    public final boolean isEmpty() {
        return false;
    }

    @Override
    public final @NonNull Iterator<E> iterator() {
        return Iterators.singletonIterator(getElement());
    }

    @Override
    public abstract @NonNull Spliterator<E> spliterator();

    @Override
    public final @NonNull Object[] toArray() {
        return new Object[] { getElement() };
    }

    @SuppressWarnings({ "unchecked", "checkstyle:parameterName" })
    @Override
    public final <T> @NonNull T[] toArray(final T[] a) {
        if (a.length > 0) {
            a[0] = (T)getElement();
            return a;
        }

        return (T[]) new Object[] {getElement()};
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public final boolean add(final E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public final boolean remove(final Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public final boolean containsAll(final Collection<?> c) {
        return c.isEmpty() || c.size() == 1 && otherContains(c);
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public final boolean addAll(final Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public final boolean retainAll(final Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public final boolean removeAll(final Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public abstract int hashCode();

    @Override
    @SuppressWarnings("checkstyle:equalsHashCode")
    public final boolean equals(final Object obj) {
        return obj == this || obj instanceof Set<?> other && other.size() == 1 && otherContains(other);
    }

    @java.io.Serial
    final Object writeReplace() {
        return new SSv1(getElement());
    }

    @SuppressFBWarnings(value = "DCN_NULLPOINTER_EXCEPTION",
        justification = "https://github.com/spotbugs/spotbugs/issues/1954")
    private boolean otherContains(final @NonNull Collection<?> other) {
        try {
            return other.contains(getElement());
        } catch (ClassCastException | NullPointerException e) {
            return false;
        }
    }

    private static final class NullElement<E> extends SingletonSet<E> {
        @java.io.Serial
        private static final long serialVersionUID = 1L;
        static final @NonNull NullElement<?> INSTANCE = new NullElement<>();

        @Override
        @SuppressWarnings("checkstyle:parameterName")
        public boolean contains(final Object o) {
            return o == null;
        }

        @Override
        @SuppressWarnings("checkstyle:equalsHashCode")
        public int hashCode() {
            return 0;
        }

        @Override
        public E getElement() {
            return null;
        }

        @Override
        public Spliterator<E> spliterator() {
            return SingletonSpliterators.immutableOfNull();
        }

        @Override
        public String toString() {
            return "[null]";
        }
    }

    @NonNullByDefault
    private static final class Regular<E> extends SingletonSet<E> {
        @java.io.Serial
        private static final long serialVersionUID = 1L;

        private final @NonNull E element;

        Regular(final E element) {
            this.element = requireNonNull(element);
        }

        @Override
        @SuppressWarnings("checkstyle:parameterName")
        public boolean contains(final @Nullable Object o) {
            return element.equals(o);
        }

        @Override
        public @NonNull E getElement() {
            return element;
        }

        @Override
        @SuppressWarnings("checkstyle:equalsHashCode")
        public int hashCode() {
            return getElement().hashCode();
        }

        @Override
        public String toString() {
            return "[" + element + ']';
        }

        @Override
        public Spliterator<E> spliterator() {
            return SingletonSpliterators.immutableOf(element);
        }
    }
}
