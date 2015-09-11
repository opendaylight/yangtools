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
import java.util.Collection;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * Factory for instantiating {@link MutableOffsetMap} instances with expected upper bound of possible keys.
 *
 * @param <K> the type of keys maintained by this factory
 */
public abstract class MutableOffsetMapFactory<K> implements Immutable {
    private static final class Ordered<K> extends MutableOffsetMapFactory<K> {
        Ordered(final Collection<K> keys) {
            super(OffsetMapCache.orderedOffsets(keys));
        }

        @Override
        <V> @NonNull MutableOffsetMap<K, V> createMap(final ImmutableMap<K, Integer> offsets) {
            return new MutableOffsetMap.Ordered<>(offsets);
        }
    }

    private static final class Unordered<K> extends MutableOffsetMapFactory<K> {
        Unordered(final Collection<K> keys) {
            super(OffsetMapCache.unorderedOffsets(keys));
        }

        @Override
        <V> @NonNull MutableOffsetMap<K, V> createMap(final ImmutableMap<K, Integer> offsets) {
            return new MutableOffsetMap.Unordered<>(offsets);
        }
    }

    private final @NonNull ImmutableMap<K, Integer> offsets;

    MutableOffsetMapFactory(final ImmutableMap<K, Integer> offsets) {
        this.offsets = requireNonNull(offsets);
    }

    public static <K> @NonNull MutableOffsetMapFactory<K> ordered(final Collection<K> keys) {
        return new Ordered<>(keys);
    }

    public static <K> @NonNull MutableOffsetMapFactory<K> unordered(final Collection<K> keys) {
        return new Unordered<>(keys);
    }

    public final <V> MutableOffsetMap<K, V> newEmptyMap() {
        return createMap(offsets);
    }

    public final Set<K> keySet() {
        return offsets.keySet();
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this).add("offsets", offsets).toString();
    }

    abstract <V> @NonNull MutableOffsetMap<K, V> createMap(ImmutableMap<K, Integer> offsets);
}
