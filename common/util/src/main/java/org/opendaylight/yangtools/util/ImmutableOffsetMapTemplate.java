/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Template for instantiating {@link ImmutableOffsetMap} instances with a fixed set of keys. The template can then be
 * used as a factory for instances via using {@link #instantiateTransformed(Map, BiFunction)} or, more efficiently,
 * using {@link #instantiateWithValues(Object[])} where the argument array has values ordered corresponding to the key
 * order defined by {@link #keySet()}.
 *
 * @param <K> the type of keys maintained by this template
 */
public abstract sealed class ImmutableOffsetMapTemplate<K> extends ImmutableMapTemplate<K> {
    private static final class Ordered<K> extends ImmutableOffsetMapTemplate<K> {
        Ordered(final Collection<K> keys) {
            super(OffsetMapCache.orderedOffsets(keys));
        }

        @Override
        <V> @NonNull ImmutableOffsetMap<K, V> createMap(final ImmutableMap<K, Integer> offsets, final V[] objects) {
            return new ImmutableOffsetMap.Ordered<>(offsets, objects);
        }
    }

    private static final class Unordered<K> extends ImmutableOffsetMapTemplate<K> {
        Unordered(final Collection<K> keys) {
            super(OffsetMapCache.unorderedOffsets(keys));
        }

        @Override
        <V> @NonNull ImmutableOffsetMap<K, V> createMap(final ImmutableMap<K, Integer> offsets, final V[] objects) {
            return new ImmutableOffsetMap.Unordered<>(offsets, objects);
        }
    }

    private final @NonNull ImmutableMap<K, Integer> offsets;

    private ImmutableOffsetMapTemplate(final ImmutableMap<K, Integer> offsets) {
        this.offsets = requireNonNull(offsets);
    }

    /**
     * Create a template which produces Maps with specified keys, with iteration order matching the iteration order
     * of {@code keys}. {@link #keySet()} will return these keys in exactly the same order. The resulting map will
     * retain insertion order through {@link UnmodifiableMapPhase#toModifiableMap()} transformations.
     *
     * @param keys Keys in requested iteration order.
     * @param <K> the type of keys maintained by resulting template
     * @return A template object.
     * @throws NullPointerException if {@code keys} or any of its elements is null
     * @throws IllegalArgumentException if {@code keys} is does not have at least two keys
     */
    public static <K> @NonNull ImmutableOffsetMapTemplate<K> ordered(final Collection<K> keys) {
        checkTwoKeys(keys);
        return new Ordered<>(keys);
    }

    /**
     * Create a template which produces Maps with specified keys, with unconstrained iteration order. Produced maps
     * will have the iteration order matching the order returned by {@link #keySet()}.  The resulting map will
     * NOT retain ordering through {@link UnmodifiableMapPhase#toModifiableMap()} transformations.
     *
     * @param keys Keys in any iteration order.
     * @param <K> the type of keys maintained by resulting template
     * @return A template object.
     * @throws NullPointerException if {@code keys} or any of its elements is null
     * @throws IllegalArgumentException if {@code keys} is does not have at least two keys
     */
    public static <K> @NonNull ImmutableOffsetMapTemplate<K> unordered(final Collection<K> keys) {
        checkTwoKeys(keys);
        return new Unordered<>(keys);
    }

    @Override
    public final Set<K> keySet() {
        return offsets.keySet();
    }

    @Override
    public final <T, V> @NonNull ImmutableOffsetMap<K, V> instantiateTransformed(final Map<K, T> fromMap,
            final BiFunction<K, T, V> valueTransformer) {
        final int size = offsets.size();
        checkSize(size, fromMap.size());

        @SuppressWarnings("unchecked")
        final var objects = (V[]) new Object[size];
        for (var entry : fromMap.entrySet()) {
            final var key = requireNonNull(entry.getKey());
            objects[offsetOf(key)] = transformValue(key, entry.getValue(), valueTransformer);
        }

        return createMap(offsets, objects);
    }

    private int offsetOf(final K key) {
        final var offset = offsets.get(key);
        if (offset == null) {
            throw new IllegalArgumentException("Key " + key + " present in input, but not in offsets " + offsets);
        }
        return offset;
    }

    @Override
    @SafeVarargs
    public final <V> ImmutableOffsetMap<K, V> instantiateWithValues(final V... values) {
        checkSize(offsets.size(), values.length);
        final var copy = values.clone();
        Arrays.stream(copy).forEach(Objects::requireNonNull);
        return createMap(offsets, values);
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this).add("offsets", offsets).toString();
    }

    abstract <V> @NonNull ImmutableOffsetMap<K, V> createMap(ImmutableMap<K, Integer> offsets, V[] objects);

    private static void checkTwoKeys(final Collection<?> keys) {
        final var size = keys.size();
        if (size < 2) {
            throw new IllegalArgumentException("Expected at least 2 keys, " + size + " supplied");
        }
    }
}