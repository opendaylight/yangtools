/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.Iterators;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Spliterator;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;

@Beta
public final class SingletonEntrySet<K, V> extends AbstractSet<Entry<K, V>> implements Immutable {
    private final @NonNull K key;
    private final @NonNull V value;

    private SingletonEntrySet(final K key, final V value) {
        this.key = requireNonNull(key);
        this.value = requireNonNull(value);
    }

    public static <K, V> @NonNull SingletonEntrySet<K, V> of(final K key, final V value) {
        return new SingletonEntrySet<>(key, value);
    }

    public @NonNull SimpleImmutableEntry<K, V> getEntry() {
        return new SimpleImmutableEntry<>(key, value);
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        return Iterators.singletonIterator(getEntry());
    }

    @Override
    public Spliterator<Entry<K, V>> spliterator() {
        return SingletonSpliterators.immutableOf(getEntry());
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean contains(final Object o) {
        if (o instanceof Entry) {
            final Entry<?, ?> other = (Entry<?, ?>) o;
            return key.equals(other.getKey()) && value.equals(other.getValue());
        }
        return false;
    }
}
