/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Collection;
import java.util.Map;
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
 * <p>
 * If the keySet is static known to contain only a single key, consider using {@link SharedSingletonMapTemplate}. If
 * it is statically known to contain multiple keys, consider using {@link ImmutableOffsetMapTemplate}.
 *
 * @param <K> the type of keys maintained by this template
 */
public abstract sealed class ImmutableMapTemplate<K> implements Immutable
        permits ImmutableOffsetMapTemplate, SharedSingletonMapTemplate {
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
        return switch (keys.size()) {
            case 0 -> throw new IllegalArgumentException("Proposed keyset must not be empty");
            case 1 -> SharedSingletonMapTemplate.ordered(keys.iterator().next());
            default -> ImmutableOffsetMapTemplate.ordered(keys);
        };
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
        return switch (keys.size()) {
            case 0 -> throw new IllegalArgumentException("Proposed keyset must not be empty");
            case 1 -> SharedSingletonMapTemplate.unordered(keys.iterator().next());
            default -> ImmutableOffsetMapTemplate.unordered(keys);
        };
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
     * @throws IllegalArgumentException if {@code values.length} does not match the number of keys in this template
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

    static void checkSize(final int expected, final int encountered) {
        if (expected != encountered) {
            throw new IllegalArgumentException("Expected " + expected + " items, " + encountered + " encountered");
        }
    }
}
