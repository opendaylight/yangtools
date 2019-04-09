/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Delegator;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * Utility class for adapting a {@link Collection}s to {@link Set}s and {@link List}s.
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public final class CollectionWrappers {
    private static final class ListWrapper<E> extends AbstractList<E> implements Delegator<Collection<E>> {
        private final Collection<E> delegate;

        ListWrapper(final Collection<E> delegate) {
            this.delegate = requireNonNull(delegate);
        }

        @Override
        public Collection<E> getDelegate() {
            return delegate;
        }

        @Override
        public Iterator<E> iterator() {
            return Iterators.unmodifiableIterator(delegate.iterator());
        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public Spliterator<E> spliterator() {
            return delegate.spliterator();
        }

        @Override
        public Stream<E> parallelStream() {
            return delegate.parallelStream();
        }

        @Override
        public Stream<E> stream() {
            return delegate.stream();
        }

        @Override
        public E get(final int index) {
            return Iterables.get(delegate, index);
        }
    }

    private static final class MapWrapper<K, V> extends AbstractList<V> implements Delegator<Map<K, V>> {
        private final Map<K, V> delegate;

        MapWrapper(final Map<K, V> delegate) {
            this.delegate = requireNonNull(delegate);
        }

        @Override
        public Map<K, V> getDelegate() {
            return delegate;
        }

        @Override
        public Iterator<V> iterator() {
            return Iterators.unmodifiableIterator(delegate.values().iterator());
        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public Spliterator<V> spliterator() {
            return delegate.values().spliterator();
        }

        @Override
        public Stream<V> parallelStream() {
            return delegate.values().parallelStream();
        }

        @Override
        public Stream<V> stream() {
            return delegate.values().stream();
        }

        @Override
        public V get(final int index) {
            return Iterables.get(delegate.values(), index);
        }
    }

    private static final class SetWrapper<E> extends AbstractSet<E> implements Delegator<Collection<E>> {
        private final Collection<E> delegate;

        SetWrapper(final Collection<E> delegate) {
            this.delegate = requireNonNull(delegate);
        }

        @Override
        public Collection<E> getDelegate() {
            return delegate;
        }

        @Override
        public Iterator<E> iterator() {
            return Iterators.unmodifiableIterator(delegate.iterator());
        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public Spliterator<E> spliterator() {
            return delegate.spliterator();
        }

        @Override
        public Stream<E> parallelStream() {
            return delegate.parallelStream();
        }

        @Override
        public Stream<E> stream() {
            return delegate.stream();
        }
    }

    private CollectionWrappers() {

    }

    /**
     * Wrap the specified {@link Collection} as a {@link List}. If the collection is already a List, it is wrapped in
     * a {@link Collections#unmodifiableList(List)} to prevent mutability leaking. If the collection is determined
     * to be empty, an empty list is returned instead. If the collection is a known-immutable implementation of List
     * interface, it is returned unwrapped. Backing collection is required to be effectively immutable. If this
     * requirement is violated, the returned object may behave in unpredictable ways.
     *
     * @param collection Collection to be wrapped
     * @return An effectively-immutable wrapper of the collection.
     * @throws NullPointerException if collection is null
     */
    public static <E> List<E> wrapAsList(final Collection<E> collection) {
        if (collection.isEmpty()) {
            return ImmutableList.of();
        }
        if (collection instanceof SetWrapper) {
            return wrapAsList(((SetWrapper<E>) collection).getDelegate());
        }
        if (collection instanceof List) {
            final List<E> cast = (List<E>) collection;
            return cast instanceof ListWrapper || cast instanceof MapWrapper || cast instanceof Immutable
                    || cast instanceof ImmutableList ? cast : Collections.unmodifiableList(cast);
        }

        return new ListWrapper<>(collection);
    }

    /**
     * Wrap the specified {@link Map}'s values as a {@link List}. If the map is determined to be empty, an empty list is
     * returned instead. Backing map is required to be effectively immutable, with fixed iteration order. If this
     * requirement is violated, the returned object may behave in unpredictable ways.
     *
     * @param map Map to be wrapped
     * @return An effectively-immutable wrapper of the map.
     * @throws NullPointerException if map is null
     */
    public static <K, V> List<V> wrapAsList(final Map<K, V> map) {
        return map.isEmpty() ? ImmutableList.of() : new MapWrapper<>(map);
    }

    /**
     * Wrap the specified {@link Collection} as a {@link Set}. If the collection is already a Set, it is wrapped in
     * a {@link Collections#unmodifiableSet(Set)} to prevent mutability leaking. If the collection is determined
     * to be empty, an empty set is returned instead. If the collection is a known-immutable implementation of Set
     * interface, it is returned unwrapped. The collection is checked for duplicates at instantiation time, such that
     * it effectively implements the Set contract. Backing collection is required to be effectively immutable. If this
     * requirement is violated, the returned object may behave in unpredictable ways.
     *
     * @param collection Collection to be wrapped
     * @return An effectively-immutable wrapper of the collection.
     * @throws NullPointerException if collection is null or any of its elements is null
     * @throws IllegalArgumentException if the collection's contents do not conform to the Set contract
     */
    public static <E> Set<E> wrapAsSet(final Collection<E> collection) {
        if (collection.isEmpty()) {
            return ImmutableSet.of();
        }
        if (collection instanceof ListWrapper) {
            return wrapAsSet(((ListWrapper<E>) collection).getDelegate());
        }
        if (collection instanceof Set) {
            final Set<E> cast = (Set<E>) collection;
            return cast instanceof SetWrapper || cast instanceof Immutable || cast instanceof SingletonSet
                    || cast instanceof ImmutableSet ? cast : Collections.unmodifiableSet(cast);
        }

        final Set<E> check = ImmutableSet.copyOf(collection);
        checkArgument(collection.size() == check.size(), "Supplied collection %s has duplicate elements", collection);
        return new SetWrapper<>(collection);
    }
}
