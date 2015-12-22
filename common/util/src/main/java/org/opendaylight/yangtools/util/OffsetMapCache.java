/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Map;

final class OffsetMapCache {
    private static final LoadingCache<Collection<?>, Map<Object, Integer>> CACHE =
            CacheBuilder.newBuilder().weakValues().build(new CacheLoader<Collection<?>, Map<Object, Integer>>() {
                @Override
                public Map<Object, Integer> load(final Collection<?> key) {
                    final Builder<Object, Integer> b = ImmutableMap.builder();
                    int i = 0;

                    for (Object arg : key) {
                        b.put(arg, i++);
                    }

                    return b.build();
                }
    });

    private OffsetMapCache() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    private static <T> Map<T, Integer> offsets(final Collection<T> args) {
        return (Map<T, Integer>) CACHE.getUnchecked(args);
    }

    @VisibleForTesting
    static void invalidateCache() {
        CACHE.invalidateAll();
    }

    static <T> Map<T, Integer> orderedOffsets(final Collection<T> args) {
        if (args.size() == 1) {
            return unorderedOffsets(args);
        }

        return offsets(ImmutableList.copyOf(args));
    }

    static <T> Map<T, Integer> unorderedOffsets(final Collection<T> args) {
        if (args instanceof SingletonSet) {
            return offsets(args);
        }

        return offsets(ImmutableSet.copyOf(args));
    }
}
