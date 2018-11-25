/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.MoreObjects;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import org.eclipse.jdt.annotation.NonNull;

public abstract class ImmutableSingletonMapTemplate<K> extends ImmutableMapTemplate<K> {
    private static final class Ordered<K> extends ImmutableSingletonMapTemplate<K> {
        Ordered(final K key) {
            super(key);
        }

        @Override
        public <V> @NonNull SharedSingletonMap<K, V> instantiateWithValue(final V value) {
            return new SharedSingletonMap.Ordered<>(keySet(), value);
        }
    }

    private static final class Unordered<K> extends ImmutableSingletonMapTemplate<K> {
        Unordered(final K key) {
            super(key);
        }

        @Override
        public <V> @NonNull SharedSingletonMap<K, V> instantiateWithValue(final V value) {
            return new SharedSingletonMap.Unordered<>(keySet(), value);
        }
    }

    private final @NonNull SingletonSet<K> keySet;

    ImmutableSingletonMapTemplate(final K key) {
        this.keySet = SharedSingletonMap.cachedSet(key);
    }

    public static <K> @NonNull ImmutableSingletonMapTemplate<K> ordered(final K key) {
        return new Ordered<>(key);
    }

    public static <K> @NonNull ImmutableSingletonMapTemplate<K> unordered(final K key) {
        return new Unordered<>(key);
    }

    @Override
    public final SingletonSet<K> keySet() {
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
        return instantiateWithValue(value);
    }

    @Override
    @SafeVarargs
    public final <V> @NonNull SharedSingletonMap<K, V> instantiateWithValues(final V... values) {
        checkArgument(values.length == 1);
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
