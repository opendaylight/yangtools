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
import com.google.common.collect.Iterators;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    private static final @NonNull Set<Class<?>> SINGLETON_CLASSES;

    static {
        final var tmp = new HashSet<Class<?>>();
        // sneaky access
        tmp.add(Collections.unmodifiableCollection(new ArrayList<>()).getClass());
        tmp.add(Collections.singleton(null).getClass());
        tmp.add(Collections.singletonList(null).getClass());
        // unmodifiable implementations: on OpenJDK 21 this these are:
        // ListN
        tmp.add(List.of().getClass());
        // List12
        tmp.add(List.of("a").getClass());
        tmp.add(List.of("a", "b").getClass());
        // ListN
        tmp.add(List.of("a", "b", "c").getClass());
        // ListN
        tmp.add(List.of("a", "b", "c").subList(1, 2).getClass());
        // SetN
        tmp.add(Set.of().getClass());
        // Set12
        tmp.add(Set.of("a").getClass());
        tmp.add(Set.of("a", "b").getClass());
        // SetN
        tmp.add(Set.of("a", "b", "c").getClass());
        // TODO: and all that because we cannot see ImmutableCollections.AbstractImmutableCollection... hopefully that
        //       becomes available once JEP-401 lands as a stable feature.

        SINGLETON_CLASSES = Set.copyOf(tmp);
    }

    private final @NonNull Collection<E> delegate;

    private UnmodifiableCollection(final @NonNull Collection<E> delegate) {
        this.delegate = requireNonNull(delegate);
    }

    /**
     * Create an unmodifiable view of the target collection. If the instance is known to be unmodifiable, that instance
     * is returned.
     *
     * @param collection Target collection
     * @return An unmodifiable view of the collection
     * @throws NullPointerException if {@code collection} is null
     * @deprecated Use {@link #of(Collection)} instead
     */
    @Deprecated(forRemoval = true, since = "15.0.0")
    public static <T> @NonNull Collection<T> create(final @NonNull Collection<T> collection) {
        return of(collection);
    }

    /**
     * Create an unmodifiable view of the target collection. If the instance is known to be unmodifiable, that instance
     * is returned.
     *
     * @param collection Target collection
     * @return An unmodifiable view of the collection
     * @throws NullPointerException if {@code collection} is null
     * @since 15.0.0
     */
    public static <T> @NonNull Collection<T> of(final @NonNull Collection<T> collection) {
        return knownImmutable(collection) ? collection : new UnmodifiableCollection<>(collection);
    }

    @SuppressModernizer
    private static boolean knownImmutable(final @NonNull Collection<?> collection) {
        return collection instanceof UnmodifiableCollection || collection instanceof ImmutableCollection
            || Collections.EMPTY_LIST == collection || Collections.EMPTY_SET == collection
            || SINGLETON_CLASSES.contains(collection.getClass());
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
