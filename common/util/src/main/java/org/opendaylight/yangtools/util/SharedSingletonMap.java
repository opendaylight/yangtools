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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.io.Serializable;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Implementation of the {@link Map} interface which stores a single mapping. The key set is shared among all instances
 * which contain the same key. This implementation does not support null keys or values.
 *
 * <p>
 * In case the set of keys is statically known, you can use {@link SharedSingletonMapTemplate} to efficiently create
 * {@link SharedSingletonMap} instances.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public abstract sealed class SharedSingletonMap<K, V> implements Serializable, UnmodifiableMapPhase<K, V> {
    static final class Ordered<K, V> extends SharedSingletonMap<K, V> {
        @java.io.Serial
        private static final long serialVersionUID = 1L;

        Ordered(final K key, final V value) {
            super(key, value);
        }

        Ordered(final SingletonSet<K> keySet, final V value) {
            super(keySet, value);
        }

        @Override
        public @NonNull ModifiableMapPhase<K, V> toModifiableMap() {
            return MutableOffsetMap.orderedCopyOf(this);
        }
    }

    static final class Unordered<K, V> extends SharedSingletonMap<K, V> {
        @java.io.Serial
        private static final long serialVersionUID = 1L;

        Unordered(final K key, final V value) {
            super(key, value);
        }

        Unordered(final SingletonSet<K> keySet, final V value) {
            super(keySet, value);
        }

        @Override
        public @NonNull ModifiableMapPhase<K, V> toModifiableMap() {
            return MutableOffsetMap.unorderedCopyOf(this);
        }
    }

    @java.io.Serial
    private static final long serialVersionUID = 1L;
    private static final LoadingCache<Object, SingletonSet<Object>> CACHE = CacheBuilder.newBuilder().weakValues()
            .build(new CacheLoader<Object, SingletonSet<Object>>() {
                @Override
                public SingletonSet<Object> load(final Object key) {
                    return SingletonSet.of(key);
                }
            });

    private final @NonNull SingletonSet<K> keySet;
    private final @NonNull V value;
    private int hashCode;

    private SharedSingletonMap(final SingletonSet<K> keySet, final V value) {
        this.keySet = requireNonNull(keySet);
        this.value = requireNonNull(value);
    }

    private SharedSingletonMap(final K key, final V value) {
        this(cachedSet(key), value);
    }

    /**
     * Create a {@link SharedSingletonMap} of specified {@code key} and {@code value}, which retains insertion order
     * when transformed via {@link #toModifiableMap()}.
     *
     * @param key key
     * @param value value
     * @return A SharedSingletonMap
     * @throws NullPointerException if any of the arguments is null
     */
    public static <K, V> @NonNull SharedSingletonMap<K, V> orderedOf(final K key, final V value) {
        return new Ordered<>(key, value);
    }

    /**
     * Create a {@link SharedSingletonMap} of specified {@code key} and {@code value}, which does not retain insertion
     * order when transformed via {@link #toModifiableMap()}.
     *
     * @param key key
     * @param value value
     * @return A SharedSingletonMap
     * @throws NullPointerException if any of the arguments is null
     */
    public static <K, V> @NonNull SharedSingletonMap<K, V> unorderedOf(final K key, final V value) {
        return new Unordered<>(key, value);
    }

    /**
     * Create a {@link SharedSingletonMap} of specified {@code key} and {@code value}, which retains insertion order
     * when transformed via {@link #toModifiableMap()}.
     *
     * @param map input map
     * @return A SharedSingletonMap
     * @throws NullPointerException if {@code map} is null
     * @throws IllegalArgumentException if {@code map} does not have exactly one entry
     */
    public static <K, V> @NonNull SharedSingletonMap<K, V> orderedCopyOf(final Map<K, V> map) {
        final Entry<K, V> e = singleEntry(map);
        return new Ordered<>(e.getKey(), e.getValue());
    }

    /**
     * Create a {@link SharedSingletonMap} from specified single-element map, which does not retain insertion order when
     * transformed via {@link #toModifiableMap()}.
     *
     * @param map input map
     * @return A SharedSingletonMap
     * @throws NullPointerException if {@code map} is null
     * @throws IllegalArgumentException if {@code map} does not have exactly one entry
     */
    public static <K, V> @NonNull SharedSingletonMap<K, V> unorderedCopyOf(final Map<K, V> map) {
        final Entry<K, V> e = singleEntry(map);
        return new Unordered<>(e.getKey(), e.getValue());
    }

    public final Entry<K, V> getEntry() {
        return new SimpleImmutableEntry<>(keySet.getElement(), value);
    }

    @Override
    public final @NonNull SingletonSet<Entry<K, V>> entrySet() {
        return SingletonSet.of(getEntry());
    }

    @Override
    public final @NonNull SingletonSet<K> keySet() {
        return keySet;
    }

    @Override
    public final @NonNull SingletonSet<V> values() {
        return SingletonSet.of(value);
    }

    @Override
    public final boolean containsKey(final Object key) {
        return keySet.contains(key);
    }

    @Override
    @SuppressWarnings("checkstyle:hiddenField")
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
    @SuppressWarnings("checkstyle:hiddenField")
    public final V put(final K key, final V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final V remove(final Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public final void putAll(final Map<? extends K, ? extends V> m) {
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
        return this == obj || obj instanceof Map<?, ?> other && other.size() == 1
            && value.equals(other.get(keySet.getElement()));
    }

    @Override
    public final String toString() {
        return "{" + keySet.getElement() + '=' + value + '}';
    }

    @SuppressWarnings("unchecked")
    static <K> @NonNull SingletonSet<K> cachedSet(final K key) {
        return (SingletonSet<K>) CACHE.getUnchecked(key);
    }

    private static <K, V> Entry<K, V> singleEntry(final Map<K, V> map) {
        final var it = map.entrySet().iterator();
        checkArgument(it.hasNext(), "Input map is empty");
        final var ret = it.next();
        checkArgument(!it.hasNext(), "Input map has more than one entry");
        return ret;
    }
}
