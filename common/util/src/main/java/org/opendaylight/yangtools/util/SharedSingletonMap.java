/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.io.Serializable;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;

/**
 * Implementation of the {@link Map} interface which stores a single mapping. The key set is shared among all instances
 * which contain the same key. This implementation does not support null keys or values.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
@Beta
public final class SharedSingletonMap<K, V> implements Serializable, UnmodifiableMapPhase<K, V> {
    private static final long serialVersionUID = 1L;
    private static final LoadingCache<Object, SingletonSet<Object>> CACHE = CacheBuilder.newBuilder().weakValues()
            .build(new CacheLoader<Object, SingletonSet<Object>>() {
                @Override
                public SingletonSet<Object> load(final Object key) {
                    return SingletonSet.of(key);
                }
            });
    private final SingletonSet<K> keySet;
    private final V value;
    private int hashCode;

    @SuppressWarnings("unchecked")
    private SharedSingletonMap(final K key, final V value) {
        this.keySet = (SingletonSet<K>) CACHE.getUnchecked(key);
        this.value = Preconditions.checkNotNull(value);
    }

    public static <K, V> SharedSingletonMap<K, V> of(final K key, final V value) {
        return new SharedSingletonMap<>(key, value);
    }

    public static <K, V> SharedSingletonMap<K, V> copyOf(final Map<K, V> m) {
        Preconditions.checkArgument(m.size() == 1);

        final Entry<K, V> e = m.entrySet().iterator().next();
        return new SharedSingletonMap<>(e.getKey(), e.getValue());
    }

    @Override
    public ModifiableMapPhase<K, V> toModifiableMap() {
        return new MutableOffsetMap<K, V>(this);
    }

    @Override
    public SingletonSet<Entry<K, V>> entrySet() {
        return SingletonSet.<Entry<K, V>>of(new SimpleEntry<>(keySet.getElement(), value));
    }

    @Override
    public SingletonSet<K> keySet() {
        return keySet;
    }

    @Override
    public SingletonSet<V> values() {
        return SingletonSet.of(value);
    }

    @Override
    public boolean containsKey(final Object key) {
        return keySet.contains(key);
    }

    @Override
    public boolean containsValue(final Object value) {
        return this.value.equals(value);
    }

    @Override
    public V get(final Object key) {
        return keySet.contains(key) ? value : null;
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
    public V put(final K key, final V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(final Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = keySet.getElement().hashCode() ^ value.hashCode();
        }
        return hashCode;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Map)) {
            return false;
        }

        final Map<?, ?> m = (Map<?, ?>)obj;
        return m.size() == 1 && value.equals(m.get(keySet.getElement()));
    }
}
