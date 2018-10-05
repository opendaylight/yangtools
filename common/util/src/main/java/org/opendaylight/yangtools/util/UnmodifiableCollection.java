/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Iterators;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import javax.annotation.Nonnull;

/**
 * An unmodifiable view over a {@link Collection}. Unlike the view returned via
 * {@link Collections#unmodifiableCollection(Collection)}, this class checks its
 * argument to ensure multiple encapsulation does not occur.
 *
 * <p>This class checks
 * the argument so it prevents multiple encapsulation. Subclasses of
 * {@link ImmutableCollection} are also recognized and not encapsulated.
 *
 * @param <E> the type of elements in this collection
 */
public final class UnmodifiableCollection<E> implements Collection<E>, Serializable {
    private static final long serialVersionUID = 1L;
    private static final Class<?> UNMODIFIABLE_COLLECTION_CLASS;
    private static final Collection<Class<?>> SINGLETON_CLASSES;

    static {
        UNMODIFIABLE_COLLECTION_CLASS = Collections.unmodifiableCollection(new ArrayList<>()).getClass();

        final Builder<Class<?>> b = ImmutableSet.builder();
        b.add(Collections.singleton(null).getClass());
        b.add(Collections.singletonList(null).getClass());
        SINGLETON_CLASSES = b.build();
    }

    private final Collection<E> delegate;

    private UnmodifiableCollection(final Collection<E> delegate) {
        this.delegate = requireNonNull(delegate);
    }

    /**
     * Create an unmodifiable view of the target collection. If the instance is known
     * to be unmodifiable, that instance is returned.
     *
     * @param collection Target collection
     * @return An unmodifiable view of the collection
     */
    public static <T> Collection<T> create(@Nonnull final Collection<T> collection) {
        if (collection instanceof UnmodifiableCollection || collection instanceof ImmutableCollection
                || Collections.EMPTY_LIST == collection || Collections.EMPTY_SET == collection
                || UNMODIFIABLE_COLLECTION_CLASS.isInstance(collection)
                || SINGLETON_CLASSES.contains(collection.getClass())) {
            return collection;
        }

        return new UnmodifiableCollection<>(collection);
    }

    @Nonnull
    @Override
    public Iterator<E> iterator() {
        return Iterators.unmodifiableIterator(delegate.iterator());
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean contains(final Object o) {
        return delegate.contains(o);
    }

    @Override
    public Object[] toArray() {
        return delegate.toArray();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public <T> T[] toArray(@Nonnull final T[] a) {
        return delegate.toArray(a);
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean containsAll(@Nonnull final Collection<?> c) {
        return delegate.containsAll(c);
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean add(final E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean addAll(@Nonnull final Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean remove(final Object o) {
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
    public String toString() {
        return "UnmodifiableCollection{" + delegate + "}";
    }
}
