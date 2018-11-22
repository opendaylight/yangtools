/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Verify;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class OffsetMapCache {
    /*
     * Cache for offsets where order matters. The key is a List, which defines the iteration order. Since we want
     * to retain this order, it is okay to use a simple LoadingCache.
     */
    private static final LoadingCache<List<?>, ImmutableMap<?, Integer>> ORDERED_CACHE =
            CacheBuilder.newBuilder().weakValues().build(new CacheLoader<List<?>, ImmutableMap<?, Integer>>() {
                @Override
                public ImmutableMap<?, Integer> load(final List<?> key) {
                    return createMap(key);
                }
            });
    /*
     * Cache for offsets where order does not mapper. The key is a Set of elements. We use manual two-stage loading
     * because of the nature of the objects we store as values, which is ImmutableMaps. An ImmutableMap, when queried
     * for keys (as is done in ImmutableOffsetMap.keySet()), will instantiate an ImmutableSet to hold these keys. It
     * would be wasteful to use one Set for lookup only to have the map have an exact copy.
     *
     * We perform the first look up using a Set (which may come from the user, for example via
     * ImmutableOffsetMap.unorderedCopyOf()), hence potentially saving a copy operation. If we fail to find an entry,
     * we construct the map and put it conditionally with Map.keySet() as the key. This will detect concurrent loading
     * and also lead to the cache and the map sharing the same Set.
     */
    private static final Cache<Set<?>, ImmutableMap<?, Integer>> UNORDERED_CACHE =
            CacheBuilder.newBuilder().weakValues().build();

    private OffsetMapCache() {
        throw new UnsupportedOperationException();
    }

    @VisibleForTesting
    static void invalidateCache() {
        ORDERED_CACHE.invalidateAll();
        UNORDERED_CACHE.invalidateAll();
    }

    @SuppressWarnings("unchecked")
    static <T> ImmutableMap<T, Integer> orderedOffsets(final Collection<T> args) {
        if (args.size() == 1) {
            return unorderedOffsets(args);
        }

        return (ImmutableMap<T, Integer>) ORDERED_CACHE.getUnchecked(ImmutableList.copyOf(args));
    }

    static <T> ImmutableMap<T, Integer> unorderedOffsets(final Collection<T> args) {
        return unorderedOffsets(args instanceof Set ? (Set<T>)args : ImmutableSet.copyOf(args));
    }

    @SuppressWarnings("unchecked")
    private static <T> ImmutableMap<T, Integer> unorderedOffsets(final Set<T> args) {
        final ImmutableMap<T, Integer> existing = (ImmutableMap<T, Integer>) UNORDERED_CACHE.getIfPresent(args);
        if (existing != null) {
            return existing;
        }

        final ImmutableMap<T, Integer> newMap = createMap(args);
        final ImmutableMap<?, Integer> raced = UNORDERED_CACHE.asMap().putIfAbsent(newMap.keySet(), newMap);
        return raced == null ? newMap : (ImmutableMap<T, Integer>)raced;
    }

    static <K, V> V[] adjustedArray(final Map<K, Integer> offsets, final List<K> keys, final V[] array) {
        Verify.verify(offsets.size() == keys.size(), "Offsets %s do not match keys %s", offsets, keys);

        // This relies on the fact that offsets has an ascending iterator
        final Iterator<K> oi = offsets.keySet().iterator();
        final Iterator<K> ki = keys.iterator();

        while (oi.hasNext()) {
            final K o = oi.next();
            final K k = ki.next();
            if (!k.equals(o)) {
                return adjustArray(offsets, keys, array);
            }
        }

        return array;
    }

    private static <T> ImmutableMap<T, Integer> createMap(final Collection<T> keys) {
        final Builder<T, Integer> b = ImmutableMap.builder();
        int counter = 0;

        for (T arg : keys) {
            b.put(arg, counter++);
        }

        return b.build();
    }

    private static <K, V> V[] adjustArray(final Map<K, Integer> offsets, final List<K> keys, final V[] array) {
        @SuppressWarnings("unchecked")
        final V[] ret = (V[]) Array.newInstance(array.getClass().getComponentType(), array.length);

        int offset = 0;
        for (final K k : keys) {
            final Integer o = Verify.verifyNotNull(offsets.get(k), "Key %s not present in offsets %s", k, offsets);
            ret[o] = array[offset++];
        }

        return ret;
    }
}
