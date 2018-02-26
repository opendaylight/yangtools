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
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * Utility class for adapting a Collection as a Set. The collection is examined for element duplicates when this wrapper
 * is instantiated. Backing collection is required to be effectively immutable. If this requirement is violated, this
 * class may behave in unpredictable ways.
 *
 * @param <E> the type of elements maintained by this set
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public final class CollectionWrappedAsSet<E> extends AbstractSet<E> implements Immutable {
    private final Collection<E> delegate;

    private CollectionWrappedAsSet(final Collection<E> delegate) {
        this.delegate = requireNonNull(delegate);
    }

    /**
     * Wrap the specified {@link Collection} as a {@link Set}. If the collection is already a Set, it is wrapped in
     * a {@link Collections#unmodifiableSet(Set)} to prevent mutability leaking. If the collection is determined
     * to be empty, an empty set is returned instead. If the collection is a known-immutable implementation of Set
     * interface, it is returned unwrapped.
     *
     * @param collection Collection to be wrapped
     * @return An effectively-immutable wrapper of the collection.
     * @throws NullPointerException if collection is null or any of its elements is null
     * @throws IllegalArgumentException if the collection's contents do not conform to the Set contract
     */
    public static <E> Set<E> of(final Collection<E> collection) {
        if (collection.isEmpty()) {
            return ImmutableSet.of();
        }
        if (collection instanceof Set) {
            final Set<E> cast = (Set<E>) collection;
            if (collection instanceof CollectionWrappedAsSet || collection instanceof ImmutableSet
                    || collection instanceof SingletonSet) {
                return cast;
            }
            return Collections.unmodifiableSet(cast);
        }

        final Set<E> check = ImmutableSet.copyOf(collection);
        checkArgument(collection.size() == check.size(), "Supplied collection %s has duplicate elements", collection);
        return new CollectionWrappedAsSet<>(collection);
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
