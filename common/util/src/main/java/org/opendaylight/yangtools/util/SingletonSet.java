/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.UnmodifiableIterator;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * A {@link Set} containing a single value. For some reason neither Java nor Guava provide direct access to the retained
 * element -- which is desirable in some situations, as is the case in {@link SingletonImmutableOffsetMap#entrySet()}.
 *
 * The choice of whether the this implementation should cache its hashCode is left up to the caller: {@link #of(Object)}
 * will result in a non-cached version, {@link #ofSlowHashCode(Object)} will eagerly precompute the hash code.
 */
public abstract class SingletonSet<E> implements Set<E>, Immutable, Serializable {
    private static final long serialVersionUID = 1L;

    private static final SingletonSet<?> NULL_SINGLETON = new SingletonSet<Object>() {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean contains(final Object o) {
            return o == null;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public Object getElement() {
            return null;
        }

        private Object readResolve() {
            return NULL_SINGLETON;
        }
    };

    @SuppressWarnings("unchecked")
    public static <E> SingletonSet<E> ofNull() {
        return (SingletonSet<E>) NULL_SINGLETON;
    }

    public static <E> SingletonSet<E> of(@Nonnull final E element) {
        return new AbstractSingletonSet<E>(element) {
            private static final long serialVersionUID = -351099536237207554L;

            @Override
            public int hashCode() {
                return getElement().hashCode();
            }
        };
    }

    public static <E> SingletonSet<E> ofSlowHashCode(@Nonnull final E element) {
        return new SlowSingletonSet<>(element);
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
        return new UnmodifiableIterator<E>() {
            private boolean done = false;

            @Override
            public boolean hasNext() {
                return done;
            }

            @Override
            public E next() {
                if (done) {
                    throw new NoSuchElementException();
                }

                done = true;
                return getElement();
            }
        };
    }

    @Override
    public final Object[] toArray() {
        return new Object[] { getElement() };
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <T> T[] toArray(final T[] a) {
        if (a.length > 0) {
            a[0] = (T)getElement();
            return a;
        }

        return (T[]) new Object[] { (T) getElement() };
    }

    @Override
    public final boolean add(final E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean remove(final Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean containsAll(final Collection<?> c) {
        if (c.isEmpty()) {
            return true;
        }
        if (c.size() != 1) {
            return false;
        }

        return otherContains(c);
    }

    @Override
    public final boolean addAll(final Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean retainAll(final Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
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
    public final boolean equals(final Object obj) {
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

    private static abstract class AbstractSingletonSet<E> extends SingletonSet<E> {
        private static final long serialVersionUID = 1L;
        private final E element;

        protected AbstractSingletonSet(final E element) {
            this.element = Preconditions.checkNotNull(element);
        }

        @Override
        public final boolean contains(final Object o) {
            return element.equals(o);
        }

        @Override
        public final E getElement() {
            return element;
        }
    }

    private static final class SlowSingletonSet<E> extends AbstractSingletonSet<E> {
        private static final long serialVersionUID = 1L;
        // FIXME: make this final
        private transient int hashCode;

        SlowSingletonSet(final E element) {
            super(element);
            this.hashCode = element.hashCode();
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        private void readObject(final java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            hashCode = getElement().hashCode();
        }
    }
}
