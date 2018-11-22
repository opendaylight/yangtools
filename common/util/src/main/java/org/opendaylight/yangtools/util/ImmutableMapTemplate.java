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
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Template for instantiating {@link ImmutableOffsetMap} and {@link SharedSingletonMap} instances with a fixed set of
 * keys. The template can then be used as a factory for instances via using the {@link #transformMap(Map, Function)}
 * method or, more efficiently, using {@link #withValues(Object[])} where the argument array has values ordered
 * corresponding to the key order defined by {@link #iterator()}.
 *
 * @param <K> the type of keys maintained by this template
 * @param <V> the type of mapped values
 */
@Beta
public abstract class ImmutableMapTemplate<K, V> implements Iterable<K> {
    private abstract static class AbstractMultiple<K, V> extends ImmutableMapTemplate<K, V> {
        private final @NonNull Map<K, Integer> offsets;

        AbstractMultiple(final Map<K, Integer> offsets) {
            this.offsets = requireNonNull(offsets);
        }

        @Override
        public final Iterator<K> iterator() {
            return offsets.keySet().iterator();
        }

        @Override
        public final <T> @NonNull ImmutableOffsetMap<K, V> transformMap(final Map<K, T> fromMap,
                final Function<T, V> transformer) {
            final int size = offsets.size();
            checkArgument(fromMap.size() == size);

            @SuppressWarnings("unchecked")
            final V[] objects = (V[]) new Object[size];
            for (Entry<K, T> entry : fromMap.entrySet()) {
                final K key = requireNonNull(entry.getKey());
                final Integer offset = offsets.get(key);
                checkArgument(offset != null, "Key %s present in input, but not in offsets %s", key, offsets);

                objects[offset.intValue()] = transformValue(entry.getValue(), transformer);
            }

            return createMap(offsets, objects);
        }

        @Override
        @SafeVarargs
        public final @NonNull Map<K, V> withValues(final V... values) {
            checkArgument(values.length == offsets.size());
            final V[] copy = values.clone();
            Arrays.stream(copy).forEach(Objects::requireNonNull);
            return createMap(offsets, values);
        }

        abstract @NonNull ImmutableOffsetMap<K, V> createMap(Map<K, Integer> offsets, V[] objects);
    }

    private static final class Ordered<K, V> extends AbstractMultiple<K, V> {
        Ordered(final Collection<K> keys) {
            super(OffsetMapCache.orderedOffsets(keys));
        }

        @Override
        @NonNull ImmutableOffsetMap<K, V> createMap(final Map<K, Integer> offsets, final V[] objects) {
            return new ImmutableOffsetMap.Ordered<>(offsets, objects);
        }
    }

    private static final class Unordered<K, V> extends AbstractMultiple<K, V> {
        Unordered(final Collection<K> keys) {
            super(OffsetMapCache.unorderedOffsets(keys));
        }

        @Override
        @NonNull ImmutableOffsetMap<K, V> createMap(final Map<K, Integer> offsets, final V[] objects) {
            return new ImmutableOffsetMap.Unordered<>(offsets, objects);
        }
    }

    private abstract static class AbstractSingle<K, V> extends ImmutableMapTemplate<K, V> {
        private final @NonNull SingletonSet<K> keySet;

        AbstractSingle(final K key) {
            this.keySet = SharedSingletonMap.cachedSet(key);
        }

        @Override
        public Iterator<K> iterator() {
            return keySet.iterator();
        }

        @Override
        public final <T> @NonNull SharedSingletonMap<K, V> transformMap(final Map<K, T> fromMap,
                final Function<T, V> transformer) {
            final Iterator<Entry<K, T>> it = fromMap.entrySet().iterator();
            checkArgument(it.hasNext(), "Input is empty while expecting 1 item");

            final Entry<K, T> entry = it.next();
            final K expected = keySet.getElement();
            final K actual = entry.getKey();
            checkArgument(expected.equals(actual), "Unexpected key %s, expecting %s", actual, expected);

            final V value = transformValue(entry.getValue(), transformer);
            checkArgument(!it.hasNext(), "Input has more than one item");

            return createMap(keySet, value);
        }

        @Override
        @SafeVarargs
        public final @NonNull Map<K, V> withValues(final V... values) {
            checkArgument(values.length == 1);
            return createMap(keySet, values[0]);
        }

        abstract @NonNull SharedSingletonMap<K, V> createMap(SingletonSet<K> keySet, V value);
    }

    private static final class SingleOrdered<K, V> extends AbstractSingle<K, V> {
        SingleOrdered(final K key) {
            super(key);
        }

        @Override
        @NonNull SharedSingletonMap<K, V> createMap(final SingletonSet<K> keySet, final V value) {
            return new SharedSingletonMap.Ordered<>(keySet, value);
        }
    }

    private static final class SingleUnordered<K, V> extends AbstractSingle<K, V> {
        SingleUnordered(final K key) {
            super(key);
        }

        @Override
        @NonNull SharedSingletonMap<K, V> createMap(final SingletonSet<K> keySet, final V value) {
            return new SharedSingletonMap.Ordered<>(keySet, value);
        }
    }

    ImmutableMapTemplate() {
        // Hidden on purpose
    }

    public static <K, V> @NonNull ImmutableMapTemplate<K, V> ordered(final Collection<K> keys) {
        switch (keys.size()) {
            case 0:
                throw new IllegalArgumentException("Proposed keyset must not be empty");
            case 1:
                return new SingleOrdered<>(keys.iterator().next());
            default:
                return new Ordered<>(keys);
        }
    }

    public static <K, V> @NonNull ImmutableMapTemplate<K, V> unordered(final Collection<K> keys) {
        switch (keys.size()) {
            case 0:
                throw new IllegalArgumentException("Proposed keyset must not be empty");
            case 1:
                return new SingleUnordered<>(keys.iterator().next());
            default:
                return new Unordered<>(keys);
        }
    }

    public abstract <T> @NonNull Map<K, V> transformMap(Map<K, T> fromMap, Function<T, V> transformer);

    @SuppressWarnings("unchecked")
    public abstract @NonNull Map<K, V> withValues(V... values);

    final <T> @NonNull V transformValue(final T input, final Function<T, V> transformer) {
        final V value = transformer.apply(input);
        checkArgument(value != null, "Transformer returned null for input %s", input);
        return value;
    }
}
