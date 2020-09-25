/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding.util;

import com.google.common.annotations.Beta;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;

/**
 * Utility class for instantiating Maps containing {@link Identifiable} values. Unlike normal Map instantiation
 * utilities, methods in this class index values via their identifier, hence providing a more convenient API, amenable
 * to fluent builders.
 *
 * <p>
 * A typical example of use with generated DataObjects looks like this:
 * <pre>
 *   <code>
 *     Foo foo = new FooBuilder()
 *          .setBar(BindingMap.of(
 *              new BarBuilder().setName("one").build(),
 *              new BarBuilder().setName("two").build()))
 *          .build();
 *   </code>
 * </pre>
 *
 * <p>
 * Another alternative is to use builders:
 * <pre>
 *   <code>
 *     Foo foo = new FooBuilder()
 *          .setBar(BindingMap.&lt;BarKey, Bar&gt;builder()
 *              .add(new BarBuilder().setName("one").build())
 *              .add(new BarBuilder().setName("two").build())
 *              .build())
 *          .build();
 *   </code>
 * </pre>
 *
 * <p>
 * This class allows for two modes of operation:
 * <ul>
 *   <li>Unordered, available through {@link #of(Identifiable...)}/{@link #builder()} family of functions. Maps
 *       instantiated through this, preferred, interface will have their iteration order randomized, as explain in
 *       Java 9+ unmodifiable collections.</li>
 *   <li>Ordered, available through {@link #ordered(Identifiable...)}/{@link #orderedBuilder()} family of functions.
 *       Maps instantiated through this interface have a predictable iteration order, as per {@link ImmutableMap}
 *       class design. The use of this interface is generally discouraged, as it may lead to code relying on map
 *       iteration order. Nevertheless it may prove useful where the goal is to have predictable outcomes and hence
 *       is provided for completeness.</li>
 * </ul>
 */
@Beta
public final class BindingMap {
    private BindingMap() {
        // Hidden on purpose
    }

    /**
     * Returns an unmodifiable map containing a single mapping.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @param v1 the mapping's value
     * @return a {@code Map} containing the specified value
     * @throws NullPointerException if the value is {@code null}
     */
    public static <K extends Identifier<V>, V extends Identifiable<K>> @NonNull Map<K, V> of(final V v1) {
        return Map.of(v1.key(), v1);
    }

    /**
     * Returns an unmodifiable map containing two mappings. The resulting map is <b>NOT</b> guaranteed retain iteration
     * order of mappings.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @param v1 the first mapping's value
     * @param v2 the second mapping's value
     * @return a {@code Map} containing the specified mappings
     * @throws IllegalArgumentException if the values contain duplicate keys
     * @throws NullPointerException if any value is {@code null}
     */
    public static <K extends Identifier<V>, V extends Identifiable<K>> @NonNull Map<K, V> of(final V v1, final V v2) {
        return Map.of(v1.key(), v1, v2.key(), v2);
    }

    /**
     * Returns an unmodifiable map containing three mappings. The resulting map is <b>NOT</b> guaranteed to retain
     * iteration order of mappings.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @param v1 the first mapping's value
     * @param v2 the second mapping's value
     * @param v3 the third mapping's value
     * @return a {@code Map} containing the specified mappings
     * @throws IllegalArgumentException if the values contain duplicate keys
     * @throws NullPointerException if any value is {@code null}
     */
    public static <K extends Identifier<V>, V extends Identifiable<K>> @NonNull Map<K, V> of(final V v1, final V v2,
            final V v3) {
        return Map.of(v1.key(), v1, v2.key(), v2, v3.key(), v3);
    }

    /**
     * Returns an unmodifiable map containing four mappings. The resulting map is <b>NOT</b> guaranteed to retain
     * iteration order of mappings.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @param v1 the first mapping's value
     * @param v2 the second mapping's value
     * @param v3 the third mapping's value
     * @param v4 the fourth mapping's value
     * @return a {@code Map} containing the specified mappings
     * @throws IllegalArgumentException if the values contain duplicate keys
     * @throws NullPointerException if any value is {@code null}
     */
    public static <K extends Identifier<V>, V extends Identifiable<K>> @NonNull Map<K, V> of(final V v1, final V v2,
            final V v3, final V v4) {
        return Map.of(v1.key(), v1, v2.key(), v2, v3.key(), v3, v4.key(), v4);
    }

    /**
     * Returns an unmodifiable map containing five mappings. The resulting map is <b>NOT</b> guaranteed to retain
     * iteration order of mappings.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @param v1 the first mapping's value
     * @param v2 the second mapping's value
     * @param v3 the third mapping's value
     * @param v4 the fourth mapping's value
     * @param v5 the fifth mapping's value
     * @return a {@code Map} containing the specified mappings
     * @throws IllegalArgumentException if the values contain duplicate keys
     * @throws NullPointerException if any value is {@code null}
     */
    public static <K extends Identifier<V>, V extends Identifiable<K>> @NonNull Map<K, V> of(final V v1, final V v2,
            final V v3, final V v4, final V v5) {
        return Map.of(v1.key(), v1, v2.key(), v2, v3.key(), v3, v4.key(), v4, v5.key(), v5);
    }

    /**
     * Returns an unmodifiable map containing six mappings. The resulting map is <b>NOT</b> guaranteed to retain
     * iteration order of mappings.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @param v1 the first mapping's value
     * @param v2 the second mapping's value
     * @param v3 the third mapping's value
     * @param v4 the fourth mapping's value
     * @param v5 the fifth mapping's value
     * @param v6 the sixth mapping's value
     * @return a {@code Map} containing the specified mappings
     * @throws IllegalArgumentException if the values contain duplicate keys
     * @throws NullPointerException if any value is {@code null}
     */
    public static <K extends Identifier<V>, V extends Identifiable<K>> @NonNull Map<K, V> of(final V v1, final V v2,
            final V v3, final V v4, final V v5, final V v6) {
        return Map.of(v1.key(), v1, v2.key(), v2, v3.key(), v3, v4.key(), v4, v5.key(), v5, v6.key(), v6);
    }

    /**
     * Returns an unmodifiable map containing seven mappings. The resulting map is <b>NOT</b> guaranteed to retain
     * iteration order of mappings.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @param v1 the first mapping's value
     * @param v2 the second mapping's value
     * @param v3 the third mapping's value
     * @param v4 the fourth mapping's value
     * @param v5 the fifth mapping's value
     * @param v6 the sixth mapping's value
     * @param v7 the seventh mapping's value
     * @return a {@code Map} containing the specified mappings
     * @throws IllegalArgumentException if the values contain duplicate keys
     * @throws NullPointerException if any value is {@code null}
     */
    public static <K extends Identifier<V>, V extends Identifiable<K>> @NonNull Map<K, V> of(final V v1, final V v2,
            final V v3, final V v4, final V v5, final V v6, final V v7) {
        return Map.of(v1.key(), v1, v2.key(), v2, v3.key(), v3, v4.key(), v4, v5.key(), v5, v6.key(), v6, v7.key(), v7);
    }

    /**
     * Returns an unmodifiable map containing eight mappings. The resulting map is <b>NOT</b> guaranteed to retain
     * iteration order of mappings.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @param v1 the first mapping's value
     * @param v2 the second mapping's value
     * @param v3 the third mapping's value
     * @param v4 the fourth mapping's value
     * @param v5 the fifth mapping's value
     * @param v6 the sixth mapping's value
     * @param v7 the seventh mapping's value
     * @param v8 the eighth mapping's value
     * @return a {@code Map} containing the specified mappings
     * @throws IllegalArgumentException if the values contain duplicate keys
     * @throws NullPointerException if any value is {@code null}
     */
    public static <K extends Identifier<V>, V extends Identifiable<K>> @NonNull Map<K, V> of(final V v1, final V v2,
            final V v3, final V v4, final V v5, final V v6, final V v7, final V v8) {
        return Map.of(v1.key(), v1, v2.key(), v2, v3.key(), v3, v4.key(), v4, v5.key(), v5, v6.key(), v6, v7.key(), v7,
            v8.key(), v8);
    }

    /**
     * Returns an unmodifiable map containing nine mappings. The resulting map is <b>NOT</b> guaranteed to retain
     * iteration order of mappings.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @param v1 the first mapping's value
     * @param v2 the second mapping's value
     * @param v3 the third mapping's value
     * @param v4 the fourth mapping's value
     * @param v5 the fifth mapping's value
     * @param v6 the sixth mapping's value
     * @param v7 the seventh mapping's value
     * @param v8 the eighth mapping's value
     * @param v9 the ninth mapping's value
     * @return a {@code Map} containing the specified mappings
     * @throws IllegalArgumentException if the values contain duplicate keys
     * @throws NullPointerException if any value is {@code null}
     */
    public static <K extends Identifier<V>, V extends Identifiable<K>> @NonNull Map<K, V> of(final V v1, final V v2,
            final V v3, final V v4, final V v5, final V v6, final V v7, final V v8, final V v9) {
        return Map.of(v1.key(), v1, v2.key(), v2, v3.key(), v3, v4.key(), v4, v5.key(), v5, v6.key(), v6, v7.key(), v7,
            v8.key(), v8, v9.key(), v9);
    }

    /**
     * Returns an unmodifiable map containing ten mappings. The resulting map is <b>NOT</b> guaranteed to retain
     * iteration order of mappings.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @param v1 the first mapping's value
     * @param v2 the second mapping's value
     * @param v3 the third mapping's value
     * @param v4 the fourth mapping's value
     * @param v5 the fifth mapping's value
     * @param v6 the sixth mapping's value
     * @param v7 the seventh mapping's value
     * @param v8 the eighth mapping's value
     * @param v9 the ninth mapping's value
     * @param v10 the ninth mapping's value
     * @return a {@code Map} containing the specified mappings
     * @throws IllegalArgumentException if the values contain duplicate keys
     * @throws NullPointerException if any value is {@code null}
     */
    public static <K extends Identifier<V>, V extends Identifiable<K>> @NonNull Map<K, V> of(final V v1, final V v2,
            final V v3, final V v4, final V v5, final V v6, final V v7, final V v8, final V v9, final V v10) {
        return Map.of(v1.key(), v1, v2.key(), v2, v3.key(), v3, v4.key(), v4, v5.key(), v5, v6.key(), v6, v7.key(), v7,
            v8.key(), v8, v9.key(), v9, v10.key(), v10);
    }

    /**
     * Returns an unmodifiable map containing given values. The resulting map is <b>NOT</b> guaranteed to retain
     * iteration order of the input array.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @param values values from which the map is populated
     * @return a {@code Map} containing the specified values
     * @throws IllegalArgumentException if there are any duplicate keys in the provided values
     * @throws NullPointerException if any value is {@code null}, or if the {@code values} array is {@code null}
     */
    @SafeVarargs
    public static <K extends Identifier<V>, V extends Identifiable<K>> @NonNull Map<K, V> of(final V... values) {
        return of(Arrays.asList(values));
    }

    /**
     * Returns an unmodifiable map containing given values. The resulting map is <b>NOT</b> guaranteed to retain
     * iteration order of the input collection.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @param values values from which the map is populated
     * @return a {@code Map} containing the specified values
     * @throws IllegalArgumentException if there are any duplicate keys in the provided values
     * @throws NullPointerException if any value is {@code null}, or if the {@code values} array is {@code null}
     */
    public static <K extends Identifier<V>, V extends Identifiable<K>> @NonNull Map<K, V> of(
            final Collection<V> values) {
        return values.stream().collect(toMap());
    }

    /**
     * Returns a collector which collects binding {@link Identifiable} objects into an unmodifiable map. The resulting
     * map is <b>NOT</b> guaranteed to retain iteration order of the stream it collects.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @return A collector that accumulates the input elements into an unmodifiable map.
     */
    public static <K extends Identifier<V>, V extends Identifiable<K>>
            @NonNull Collector<V, ?, ? extends Map<K, V>> toMap() {
        return Collectors.toUnmodifiableMap(Identifiable::key, v -> v);
    }

    /**
     * Create a builder on an unmodifiable map, which does not retain value insertion order. The builder will be
     * pre-sized to hold {@value Builder#DEFAULT_INITIAL_CAPACITY} elements.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @return A {@link Builder} instance.
     */
    public static <K extends Identifier<V>, V extends Identifiable<K>> @NonNull Builder<K, V> builder() {
        return builder(Builder.DEFAULT_INITIAL_CAPACITY);
    }

    /**
     * Create a builder on an unmodifiable map, which does not retain value insertion order. The builder will be
     * pre-sized to hold specified number of elements.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @param expectedSize Expected number of values in the resulting map
     * @return A {@link Builder} instance.
     */
    public static <K extends Identifier<V>, V extends Identifiable<K>> @NonNull Builder<K, V> builder(
            final int expectedSize) {
        return new UnorderedBuilder<>(expectedSize);
    }

    /**
     * Returns an unmodifiable map containing two mappings. The resulting map will retain iteration order of mappings.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @param v1 the first mapping's value
     * @param v2 the second mapping's value
     * @return a {@code Map} containing the specified mappings
     * @throws IllegalArgumentException if the values contain duplicate keys
     * @throws NullPointerException if any value is {@code null}
     */
    public static <K extends Identifier<V>, V extends Identifiable<K>> @NonNull Map<K, V> ordered(final V v1,
            final V v2) {
        return ImmutableMap.of(v1.key(), v1, v2.key(), v2);
    }

    /**
     * Returns an unmodifiable map containing three mappings. The resulting map will retain iteration order of mappings.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @param v1 the first mapping's value
     * @param v2 the second mapping's value
     * @param v3 the third mapping's value
     * @return a {@code Map} containing the specified mappings
     * @throws IllegalArgumentException if the values contain duplicate keys
     * @throws NullPointerException if any value is {@code null}
     */
    public static <K extends Identifier<V>, V extends Identifiable<K>> @NonNull Map<K, V> ordered(final V v1,
            final V v2, final V v3) {
        return ImmutableMap.of(v1.key(), v1, v2.key(), v2, v3.key(), v3);
    }

    /**
     * Returns an unmodifiable map containing four mappings. The resulting map will retain iteration order of mappings.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @param v1 the first mapping's value
     * @param v2 the second mapping's value
     * @param v3 the third mapping's value
     * @param v4 the fourth mapping's value
     * @return a {@code Map} containing the specified mappings
     * @throws IllegalArgumentException if the values contain duplicate keys
     * @throws NullPointerException if any value is {@code null}
     */
    public static <K extends Identifier<V>, V extends Identifiable<K>> @NonNull Map<K, V> ordered(final V v1,
            final V v2, final V v3, final V v4) {
        return ImmutableMap.of(v1.key(), v1, v2.key(), v2, v3.key(), v3, v4.key(), v4);
    }

    /**
     * Returns an unmodifiable map containing five mappings. The resulting map will retain iteration order of mappings.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @param v1 the first mapping's value
     * @param v2 the second mapping's value
     * @param v3 the third mapping's value
     * @param v4 the fourth mapping's value
     * @param v5 the fifth mapping's value
     * @return a {@code Map} containing the specified mappings
     * @throws IllegalArgumentException if the values contain duplicate keys
     * @throws NullPointerException if any value is {@code null}
     */
    public static <K extends Identifier<V>, V extends Identifiable<K>> @NonNull Map<K, V> ordered(final V v1,
            final V v2, final V v3, final V v4, final V v5) {
        return ImmutableMap.of(v1.key(), v1, v2.key(), v2, v3.key(), v3, v4.key(), v4, v5.key(), v5);
    }

    /**
     * Returns an unmodifiable map containing given values. Resulting {@code Map} will retain the iteration order of
     * values.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @param values values from which the map is populated
     * @return a {@code Map} containing the specified values
     * @throws IllegalArgumentException if there are any duplicate keys in the provided values
     * @throws NullPointerException if any value is {@code null}, or if the {@code values} array is {@code null}
     */
    @SafeVarargs
    public static <K extends Identifier<V>, V extends Identifiable<K>> @NonNull Map<K, V> ordered(final V... values) {
        return ordered(Arrays.asList(values));
    }

    /**
     * Returns an unmodifiable map containing given values. Resulting {@code Map} will retain the iteration order of
     * values.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @param values values from which the map is populated
     * @return a {@code Map} containing the specified values
     * @throws IllegalArgumentException if there are any duplicate keys in the provided values
     * @throws NullPointerException if any value is {@code null}, or if the {@code values} array is {@code null}
     */
    public static <K extends Identifier<V>, V extends Identifiable<K>> @NonNull Map<K, V> ordered(
            final Collection<V> values) {
        return values.stream().collect(toOrderedMap());
    }

    /**
     * Returns a collector which collects binding {@link Identifiable} objects into an unmodifiable map. The resulting
     * map will retain iteration order of the stream it collects.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @return A collector that accumulates the input elements into an unmodifiable map.
     */
    public static <K extends Identifier<V>, V extends Identifiable<K>>
            @NonNull Collector<V, ?, ? extends Map<K, V>> toOrderedMap() {
        return ImmutableMap.<V, K, V>toImmutableMap(Identifiable::key, v -> v);
    }

    /**
     * Create a builder on an unmodifiable map, which retains value insertion order. The builder will be pre-sized to
     * hold {@value Builder#DEFAULT_INITIAL_CAPACITY} elements.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @return A {@link Builder} instance.
     */
    public static <K extends Identifier<V>, V extends Identifiable<K>> @NonNull Builder<K, V> orderedBuilder() {
        return orderedBuilder(Builder.DEFAULT_INITIAL_CAPACITY);
    }

    /**
     * Create a builder on an unmodifiable map, which retains value insertion order. The builder will be pre-sized to
     * hold specified number of elements.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @param expectedSize Expected number of values in the resulting map
     * @return A {@link Builder} instance.
     */
    public static <K extends Identifier<V>, V extends Identifiable<K>> @NonNull Builder<K, V> orderedBuilder(
            final int expectedSize) {
        return new OrderedBuilder<>(expectedSize);
    }

    /**
     * Builder producing a Map containing binding {@link Identifiable} values.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     */
    public abstract static class Builder<K extends Identifier<V>, V extends Identifiable<K>>
            implements org.opendaylight.yangtools.concepts.Builder<Map<K, V>> {
        static final int DEFAULT_INITIAL_CAPACITY = 4;

        Builder() {
            // Hidden on purpose
        }

        /**
         * Add a value to this builder.
         *
         * @param value the value to add
         * @return this builder
         * @throws NullPointerException if value is {@code null}
         */
        public final @NonNull Builder<K, V> add(final V value) {
            addEntry(value.key(), value);
            return this;
        }

        /**
         * Add values to this builder.
         *
         * @param values the values to add
         * @return this builder
         * @throws NullPointerException if value is, or contains, {@code null}
         */
        @SafeVarargs
        public final @NonNull Builder<K, V> addAll(final V... values) {
            return addAll(Arrays.asList(values));
        }

        /**
         * Add values to this builder.
         *
         * @param values the values to add
         * @return this builder
         * @throws NullPointerException if value is, or contains, {@code null}
         */
        public final @NonNull Builder<K, V> addAll(final Collection<V> values) {
            addEntries(Collections2.transform(values, value -> Map.entry(value.key(), value)));
            return this;
        }

        abstract void addEntry(K key, V value);

        abstract void addEntries(Collection<Entry<K, V>> entries);
    }

    private static final class OrderedBuilder<K extends Identifier<V>, V extends Identifiable<K>>
            extends Builder<K, V> {
        private final ImmutableMap.Builder<K, V> delegate;

        OrderedBuilder(final int expectedSize) {
            delegate = ImmutableMap.builderWithExpectedSize(expectedSize);
        }

        @Override
        public Map<K, V> build() {
            return delegate.build();
        }

        @Override
        void addEntry(final K key, final V value) {
            delegate.put(key, value);
        }

        @Override
        void addEntries(final Collection<Entry<K, V>> entries) {
            delegate.putAll(entries);
        }
    }

    private static final class UnorderedBuilder<K extends Identifier<V>, V extends Identifiable<K>>
            extends Builder<K, V> {
        private final ArrayList<Entry<K, V>> buffer;

        UnorderedBuilder(final int expectedSize) {
            buffer = new ArrayList<>(expectedSize);
        }

        @Override
        @SuppressWarnings("unchecked")
        public Map<K, V> build() {
            return Map.ofEntries(buffer.toArray(new Entry[0]));
        }

        @Override
        void addEntry(final K key, final V value) {
            buffer.add(Map.entry(key, value));
        }

        @Override
        void addEntries(final Collection<Entry<K, V>> entries) {
            buffer.addAll(entries);
        }
    }
}
