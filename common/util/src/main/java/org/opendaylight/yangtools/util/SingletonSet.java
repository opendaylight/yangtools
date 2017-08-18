/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.Iterators;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * A {@link Set} containing a single value. For some reason neither Java nor Guava provide direct access to the retained
 * element -- which is desirable in some situations, as is the case in {@link SharedSingletonMap#entrySet()}.
 */
@Beta
public abstract class SingletonSet<E> implements Set<E>, Immutable, Serializable {
    private static final long serialVersionUID = 1L;

    private static final SingletonSet<?> NULL_SINGLETON = new SingletonSet<Object>() {
        private static final long serialVersionUID = 1L;

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
        public Object getElement() {
            return null;
        }

        @Override
        public String toString() {
            return "[null]";
        }

        private Object readResolve() {
            return NULL_SINGLETON;
        }
    };

    @SuppressWarnings("unchecked")
    public static <E> SingletonSet<E> of(@Nonnull final E element) {
        if (element == null) {
            return (SingletonSet<E>) NULL_SINGLETON;
        }
        return new RegularSingletonSet<>(element);
    }

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
    public final Iterator<E> iterator() {
        return Iterators.singletonIterator(getElement());
    }

    @Nonnull
    @Override
    public final Object[] toArray() {
        return new Object[] { getElement() };
    }

    @Nonnull
    @SuppressWarnings({ "unchecked", "checkstyle:parameterName" })
    @Override
    public final <T> T[] toArray(@Nonnull final T[] a) {
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
    public final boolean containsAll(@Nonnull final Collection<?> c) {
        if (c.isEmpty()) {
            return true;
        }
        if (c.size() != 1) {
            return false;
        }

        return otherContains(c);
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public final boolean addAll(@Nonnull final Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public final boolean retainAll(@Nonnull final Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public final boolean removeAll(@Nonnull final Collection<?> c) {
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
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Set)) {
            return false;
        }

        final Set<?> s = (Set<?>)obj;
        return s.size() == 1 && otherContains(s);
    }

    private boolean otherContains(final Collection<?> other) {
        try {
            return other.contains(getElement());
        } catch (ClassCastException | NullPointerException e) {
            return false;
        }
    }

    private static final class RegularSingletonSet<E> extends SingletonSet<E> {
        private static final long serialVersionUID = 1L;
        private final E element;

        RegularSingletonSet(final E element) {
            this.element = requireNonNull(element);
        }

        @Override
        @SuppressWarnings("checkstyle:parameterName")
        public boolean contains(final Object o) {
            return element.equals(o);
        }

        @Override
        public E getElement() {
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
    }
}
