/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.io.Serializable;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map;
import javax.annotation.Nonnull;

/**
 * Implementation of the {@link Map} interface which stores a single mapping. The key set is shared among all instances
 * which contain the same key. This implementation does not support null keys or values.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
@Beta
public abstract class SharedSingletonMap<K, V> implements Serializable, UnmodifiableMapPhase<K, V> {
    private static final class Ordered<K, V> extends SharedSingletonMap<K, V> {
        private static final long serialVersionUID = 1L;

        Ordered(final K key, final V value) {
            super(key, value);
        }

        @Nonnull
        @Override
        public ModifiableMapPhase<K, V> toModifiableMap() {
            return MutableOffsetMap.orderedCopyOf(this);
        }
    }

    private static final class Unordered<K, V> extends SharedSingletonMap<K, V> {
        private static final long serialVersionUID = 1L;

        Unordered(final K key, final V value) {
            super(key, value);
        }

        @Nonnull
        @Override
        public ModifiableMapPhase<K, V> toModifiableMap() {
            return MutableOffsetMap.unorderedCopyOf(this);
        }
    }

    private static final long serialVersionUID = 1L;
    private static final LoadingCache<Object, SingletonSet<Object>> CACHE = CacheBuilder.newBuilder().weakValues()
            .build(new CacheLoader<Object, SingletonSet<Object>>() {
                @Override
                public SingletonSet<Object> load(@Nonnull final Object key) {
                    return SingletonSet.of(key);
                }
            });
    private final SingletonSet<K> keySet;
    private final V value;
    private int hashCode;

    @SuppressWarnings("unchecked")
    SharedSingletonMap(final K key, final V value) {
        this.keySet = (SingletonSet<K>) CACHE.getUnchecked(key);
        this.value = requireNonNull(value);
    }

    public static <K, V> SharedSingletonMap<K, V> orderedOf(final K key, final V value) {
        return new Ordered<>(key, value);
    }

    public static <K, V> SharedSingletonMap<K, V> unorderedOf(final K key, final V value) {
        return new Unordered<>(key, value);
    }

    public static <K, V> SharedSingletonMap<K, V> orderedCopyOf(final Map<K, V> map) {
        checkArgument(map.size() == 1);

        final Entry<K, V> e = map.entrySet().iterator().next();
        return new Ordered<>(e.getKey(), e.getValue());
    }

    public static <K, V> SharedSingletonMap<K, V> unorderedCopyOf(final Map<K, V> map) {
        checkArgument(map.size() == 1);

        final Entry<K, V> e = map.entrySet().iterator().next();
        return new Unordered<>(e.getKey(), e.getValue());
    }

    @Nonnull
    @Override
    public final SingletonSet<Entry<K, V>> entrySet() {
        return SingletonSet.of(new SimpleImmutableEntry<>(keySet.getElement(), value));
    }

    @Nonnull
    @Override
    public final SingletonSet<K> keySet() {
        return keySet;
    }

    @Nonnull
    @Override
    public final SingletonSet<V> values() {
        return SingletonSet.of(value);
    }

    @Override
    public final boolean containsKey(final Object key) {
        return keySet.contains(key);
    }

    @Override
    public final boolean containsValue(final Object value) {
        return this.value.equals(value);
    }

    @Override
    public final V get(final Object key) {
        return keySet.contains(key) ? value : null;
    }

    @Override
    public final int size() {
        return 1;
    }

    @Override
    public final boolean isEmpty() {
        return false;
    }

    @Override
    public final V put(final K key, final V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final V remove(final Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public final void putAll(@Nonnull final Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final int hashCode() {
        if (hashCode == 0) {
            hashCode = keySet.getElement().hashCode() ^ value.hashCode();
        }
        return hashCode;
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Map)) {
            return false;
        }

        final Map<?, ?> m = (Map<?, ?>)obj;
        return m.size() == 1 && value.equals(m.get(keySet.getElement()));
    }

    @Override
    public final String toString() {
        return "{" + keySet.getElement() + '=' + value + '}';
    }
}
