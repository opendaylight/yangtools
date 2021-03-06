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
import com.google.common.collect.Iterators;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import org.eclipse.jdt.annotation.NonNull;
import org.gaul.modernizer_maven_annotations.SuppressModernizer;

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
    private static final @NonNull Class<?> UNMODIFIABLE_COLLECTION_CLASS =
        Collections.unmodifiableCollection(new ArrayList<>()).getClass();
    private static final @NonNull ImmutableSet<Class<?>> SINGLETON_CLASSES = ImmutableSet.<Class<?>>builder()
        .add(Collections.singleton(null).getClass())
        .add(Collections.singletonList(null).getClass())
        .build();

    private final @NonNull Collection<E> delegate;

    private UnmodifiableCollection(final @NonNull Collection<E> delegate) {
        this.delegate = requireNonNull(delegate);
    }

    /**
     * Create an unmodifiable view of the target collection. If the instance is known
     * to be unmodifiable, that instance is returned.
     *
     * @param collection Target collection
     * @return An unmodifiable view of the collection
     * @throws NullPointerException if {@code collection} is null
     */
    @SuppressModernizer
    public static <T> @NonNull Collection<T> create(final @NonNull Collection<T> collection) {
        if (collection instanceof UnmodifiableCollection || collection instanceof ImmutableCollection
                || Collections.EMPTY_LIST == collection || Collections.EMPTY_SET == collection
                || UNMODIFIABLE_COLLECTION_CLASS.isInstance(collection)
                || SINGLETON_CLASSES.contains(collection.getClass())) {
            return collection;
        }

        return new UnmodifiableCollection<>(collection);
    }

    @Override
    public @NonNull Iterator<E> iterator() {
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
    public <T> T[] toArray(final T[] a) {
        return delegate.toArray(a);
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean containsAll(final Collection<?> c) {
        return delegate.containsAll(c);
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean add(final E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean addAll(final Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean remove(final Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean removeAll(final Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean retainAll(final Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NonNull String toString() {
        return "UnmodifiableCollection{" + delegate + "}";
    }
}
