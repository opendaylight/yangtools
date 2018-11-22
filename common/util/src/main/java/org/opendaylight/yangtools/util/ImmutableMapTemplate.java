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
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * Template for instantiating {@link UnmodifiableMapPhase} instances with a fixed set of keys. The template can then be
 * used as a factory for instances via using {@link #instantiateTransformed(Map, BiFunction)} or, more efficiently,
 * using {@link #instantiateWithValues(Object[])} where the argument array has values ordered corresponding to the key
 * order defined by {@link #keySet()}.
 *
 * @param <K> the type of keys maintained by this template
 */
@Beta
public abstract class ImmutableMapTemplate<K> implements Immutable {
    private abstract static class AbstractMultiple<K> extends ImmutableMapTemplate<K> {
        private final @NonNull ImmutableMap<K, Integer> offsets;

        AbstractMultiple(final ImmutableMap<K, Integer> offsets) {
            this.offsets = requireNonNull(offsets);
        }

        @Override
        public final Set<K> keySet() {
            return offsets.keySet();
        }

        @Override
        public final <T, V> @NonNull ImmutableOffsetMap<K, V> instantiateTransformed(final Map<K, T> fromMap,
                final BiFunction<K, T, V> valueTransformer) {
            final int size = offsets.size();
            checkArgument(fromMap.size() == size);

            @SuppressWarnings("unchecked")
            final V[] objects = (V[]) new Object[size];
            for (Entry<K, T> entry : fromMap.entrySet()) {
                final K key = requireNonNull(entry.getKey());
                final Integer offset = offsets.get(key);
                checkArgument(offset != null, "Key %s present in input, but not in offsets %s", key, offsets);

                objects[offset.intValue()] = transformValue(key, entry.getValue(), valueTransformer);
            }

            return createMap(offsets, objects);
        }

        @Override
        @SafeVarargs
        public final <V> @NonNull UnmodifiableMapPhase<K, V> instantiateWithValues(final V... values) {
            checkArgument(values.length == offsets.size());
            final V[] copy = values.clone();
            Arrays.stream(copy).forEach(Objects::requireNonNull);
            return createMap(offsets, values);
        }

        @Override
        public final String toString() {
            return MoreObjects.toStringHelper(this).add("offsets", offsets).toString();
        }

        abstract <V> @NonNull ImmutableOffsetMap<K, V> createMap(ImmutableMap<K, Integer> offsets, V[] objects);
    }

    private static final class Ordered<K> extends AbstractMultiple<K> {
        Ordered(final Collection<K> keys) {
            super(OffsetMapCache.orderedOffsets(keys));
        }

        @Override
        <V> @NonNull ImmutableOffsetMap<K, V> createMap(final ImmutableMap<K, Integer> offsets, final V[] objects) {
            return new ImmutableOffsetMap.Ordered<>(offsets, objects);
        }
    }

    private static final class Unordered<K> extends AbstractMultiple<K> {
        Unordered(final Collection<K> keys) {
            super(OffsetMapCache.unorderedOffsets(keys));
        }

        @Override
        <V> @NonNull ImmutableOffsetMap<K, V> createMap(final ImmutableMap<K, Integer> offsets, final V[] objects) {
            return new ImmutableOffsetMap.Unordered<>(offsets, objects);
        }
    }

    private abstract static class AbstractSingle<K> extends ImmutableMapTemplate<K> {
        private final @NonNull SingletonSet<K> keySet;

        AbstractSingle(final K key) {
            this.keySet = SharedSingletonMap.cachedSet(key);
        }

        @Override
        public Set<K> keySet() {
            return keySet;
        }

        @Override
        public final <T, V> @NonNull SharedSingletonMap<K, V> instantiateTransformed(final Map<K, T> fromMap,
                final BiFunction<K, T, V> valueTransformer) {
            final Iterator<Entry<K, T>> it = fromMap.entrySet().iterator();
            checkArgument(it.hasNext(), "Input is empty while expecting 1 item");

            final Entry<K, T> entry = it.next();
            final K expected = keySet.getElement();
            final K actual = entry.getKey();
            checkArgument(expected.equals(actual), "Unexpected key %s, expecting %s", actual, expected);

            final V value = transformValue(actual, entry.getValue(), valueTransformer);
            checkArgument(!it.hasNext(), "Input has more than one item");

            return createMap(keySet, value);
        }

        @Override
        @SafeVarargs
        public final <V> @NonNull UnmodifiableMapPhase<K, V> instantiateWithValues(final V... values) {
            checkArgument(values.length == 1);
            return createMap(keySet, values[0]);
        }

        @Override
        public final String toString() {
            return MoreObjects.toStringHelper(this).add("keySet", keySet).toString();
        }

        abstract <V> @NonNull SharedSingletonMap<K, V> createMap(SingletonSet<K> keySet, V value);
    }

    private static final class SingleOrdered<K> extends AbstractSingle<K> {
        SingleOrdered(final K key) {
            super(key);
        }

        @Override
        <V> @NonNull SharedSingletonMap<K, V> createMap(final SingletonSet<K> keySet, final V value) {
            return new SharedSingletonMap.Ordered<>(keySet, value);
        }
    }

    private static final class SingleUnordered<K> extends AbstractSingle<K> {
        SingleUnordered(final K key) {
            super(key);
        }

        @Override
        <V> @NonNull SharedSingletonMap<K, V> createMap(final SingletonSet<K> keySet, final V value) {
            return new SharedSingletonMap.Ordered<>(keySet, value);
        }
    }

    ImmutableMapTemplate() {
        // Hidden on purpose
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
     * @throws IllegalArgumentException if {@code keys} is empty
     */
    public static <K> @NonNull ImmutableMapTemplate<K> ordered(final Collection<K> keys) {
        switch (keys.size()) {
            case 0:
                throw new IllegalArgumentException("Proposed keyset must not be empty");
            case 1:
                return new SingleOrdered<>(keys.iterator().next());
            default:
                return new Ordered<>(keys);
        }
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
     * @throws IllegalArgumentException if {@code keys} is empty
     */
    public static <K> @NonNull ImmutableMapTemplate<K> unordered(final Collection<K> keys) {
        switch (keys.size()) {
            case 0:
                throw new IllegalArgumentException("Proposed keyset must not be empty");
            case 1:
                return new SingleUnordered<>(keys.iterator().next());
            default:
                return new Unordered<>(keys);
        }
    }

    /**
     * Instantiate an immutable map by applying specified {@code transformer} to values of {@code fromMap}.
     *
     * @param fromMap Input map
     * @param keyValueTransformer Transformation to apply to values
     * @param <T> the type of input values
     * @param <V> the type of mapped values
     * @return An immutable map
     * @throws NullPointerException if any of the arguments is null or if the transformer produces a {@code null} value
     * @throws IllegalArgumentException if {@code fromMap#keySet()} does not match this template's keys
     */
    public abstract <T, V> @NonNull UnmodifiableMapPhase<K, V> instantiateTransformed(Map<K, T> fromMap,
            BiFunction<K, T, V> keyValueTransformer);

    /**
     * Instantiate an immutable map by filling values from provided array. The array MUST be ordered to match key order
     * as returned by {@link #keySet()}.
     *
     * @param values Values to use
     * @param <V> the type of mapped values
     * @return An immutable map
     * @throws NullPointerException if {@code values} or any of its elements is null
     * @throws IllegalArgumentException if {@code values.lenght} does not match the number of keys in this template
     */
    @SuppressWarnings("unchecked")
    public abstract <V> @NonNull UnmodifiableMapPhase<K, V> instantiateWithValues(V... values);

    /**
     * Returns the set of keys expected by this template, in the iteration order Maps resulting from instantiation
     * will have.
     *
     * @return This template's key set
     * @see Map#keySet()
     */
    public abstract Set<K> keySet();

    final <T, V> @NonNull V transformValue(final K key, final T input, final BiFunction<K, T, V> transformer) {
        final V value = transformer.apply(key, input);
        checkArgument(value != null, "Transformer returned null for input %s at key %s", input, key);
        return value;
    }
}
