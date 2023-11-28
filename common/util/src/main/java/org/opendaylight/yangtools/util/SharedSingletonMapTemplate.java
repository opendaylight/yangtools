/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import com.google.common.base.MoreObjects;
import java.util.Map;
import java.util.function.BiFunction;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Template for instantiating {@link SharedSingletonMap} instances with a fixed key. The template can then be
 * used as a factory for instances via using {@link #instantiateTransformed(Map, BiFunction)} or, more efficiently,
 * using {@link #instantiateWithValue(Object)}.
 *
 * @param <K> the type of keys maintained by this template
 */
public abstract sealed class SharedSingletonMapTemplate<K> extends ImmutableMapTemplate<K> {
    private static final class Ordered<K> extends SharedSingletonMapTemplate<K> {
        Ordered(final K key) {
            super(key);
        }

        @Override
        public <V> @NonNull SharedSingletonMap<K, V> instantiateWithValue(final V value) {
            return new SharedSingletonMap.Ordered<>(keySet(), value);
        }
    }

    private static final class Unordered<K> extends SharedSingletonMapTemplate<K> {
        Unordered(final K key) {
            super(key);
        }

        @Override
        public <V> @NonNull SharedSingletonMap<K, V> instantiateWithValue(final V value) {
            return new SharedSingletonMap.Unordered<>(keySet(), value);
        }
    }

    private final @NonNull SingletonSet<K> keySet;

    private SharedSingletonMapTemplate(final K key) {
        keySet = SharedSingletonMap.cachedSet(key);
    }

    /**
     * Create a template which produces Maps with specified key. The resulting map will retain insertion order through
     * {@link UnmodifiableMapPhase#toModifiableMap()} transformations.
     *
     * @param key Single key in resulting map
     * @param <K> the type of keys maintained by resulting template
     * @return A template object.
     * @throws NullPointerException if {@code key} is null
     */
    public static <K> @NonNull SharedSingletonMapTemplate<K> ordered(final K key) {
        return new Ordered<>(key);
    }

    /**
     * Create a template which produces Maps with specified key. The resulting map will NOT retain ordering through
     * {@link UnmodifiableMapPhase#toModifiableMap()} transformations.
     *
     * @param key Single key in resulting map
     * @param <K> the type of keys maintained by resulting template
     * @return A template object.
     * @throws NullPointerException if {@code key} is null
     */
    public static <K> @NonNull SharedSingletonMapTemplate<K> unordered(final K key) {
        return new Unordered<>(key);
    }

    @Override
    public final SingletonSet<K> keySet() {
        return keySet;
    }

    @Override
    public final <T, V> @NonNull SharedSingletonMap<K, V> instantiateTransformed(final Map<K, T> fromMap,
            final BiFunction<K, T, V> valueTransformer) {
        final var it = fromMap.entrySet().iterator();
        if (!it.hasNext()) {
            throw new IllegalArgumentException("Input is empty while expecting 1 item");
        }

        final var entry = it.next();
        final var expected = keySet.getElement();
        final var actual = entry.getKey();
        if (!expected.equals(actual)) {
            throw new IllegalArgumentException("Unexpected key " + actual + ", expecting " + expected);
        }

        final var value = transformValue(actual, entry.getValue(), valueTransformer);
        if (it.hasNext()) {
            throw new IllegalArgumentException("Input has more than one item");
        }
        return instantiateWithValue(value);
    }

    @Override
    @SafeVarargs
    public final <V> @NonNull SharedSingletonMap<K, V> instantiateWithValues(final V... values) {
        checkSize(1, values.length);
        return instantiateWithValue(values[0]);
    }

    /**
     * Instantiate an immutable map with the value supplied.
     *
     * @param value Value to use
     * @param <V> the type of mapped values
     * @return An immutable map
     * @throws NullPointerException if {@code value} is null
     */
    public abstract <V> @NonNull SharedSingletonMap<K, V> instantiateWithValue(V value);

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this).add("keySet", keySet).toString();
    }
}
