/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.UnmodifiableIterator;
import java.io.Serializable;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Implementation of the {@link Map} interface which stores a set of immutable mappings using a key-to-offset map and
 * a backing array. This is useful for situations where the same key set is shared across a multitude of maps, as this
 * class uses a global cache to share the key-to-offset mapping.
 *
 * <p>
 * In case the set of keys is statically known, you can use {@link ImmutableOffsetMapTemplate} to efficiently create
 * {@link ImmutableOffsetMap} instances.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public abstract sealed class ImmutableOffsetMap<K, V> implements UnmodifiableMapPhase<K, V>, Serializable {
    static final class Ordered<K, V> extends ImmutableOffsetMap<K, V> {
        @java.io.Serial
        private static final long serialVersionUID = 1L;

        Ordered(final ImmutableMap<K, Integer> offsets, final V[] objects) {
            super(offsets, objects);
        }

        @Override
        public MutableOffsetMap<K, V> toModifiableMap() {
            return MutableOffsetMap.orderedCopyOf(this);
        }

        @Override
        Object writeReplace() {
            return new OIOMv1(this);
        }
    }

    static final class Unordered<K, V> extends ImmutableOffsetMap<K, V> {
        @java.io.Serial
        private static final long serialVersionUID = 1L;

        Unordered(final ImmutableMap<K, Integer> offsets, final V[] objects) {
            super(offsets, objects);
        }

        @Override
        public MutableOffsetMap<K, V> toModifiableMap() {
            return MutableOffsetMap.unorderedCopyOf(this);
        }

        @Override
        Object writeReplace() {
            return new UIOMv1(this);
        }
    }

    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private final @NonNull ImmutableMap<K, Integer> offsets;
    private final @NonNull V[] objects;
    private transient int hashCode;

    /**
     * Construct a new instance backed by specified key-to-offset map and array of objects.
     *
     * @param offsets Key-to-offset map, may not be null
     * @param objects Array of value object, may not be null. The array is stored as is, the caller
     *              is responsible for ensuring its contents remain unmodified.
     */
    private ImmutableOffsetMap(final ImmutableMap<K, Integer> offsets, final V[] objects) {
        this.offsets = requireNonNull(offsets);
        this.objects = requireNonNull(objects);
        checkArgument(offsets.size() == objects.length);
    }

    @Override
    public abstract @NonNull MutableOffsetMap<K, V> toModifiableMap();

    /**
     * Create an {@link ImmutableOffsetMap} as a copy of an existing map. This is actually not completely true, as this
     * method returns an {@link ImmutableMap} for empty and singleton inputs, as those are more memory-efficient. This
     * method also recognizes {@link ImmutableOffsetMap} and {@link SharedSingletonMap} on input, and returns it back
     * without doing anything else. It also recognizes {@link MutableOffsetMap} (as returned by
     * {@link #toModifiableMap()}) and makes an efficient copy of its contents. All other maps are converted to an
     * {@link ImmutableOffsetMap} with the same iteration order as input.
     *
     * @param <K> the type of keys maintained by the map
     * @param <V> the type of mapped values
     * @param map Input map, may not be null.
     * @return An isolated, immutable copy of the input map
     * @throws NullPointerException if {@code map} or any of its elements is null.
     */
    public static <K, V> @NonNull Map<K, V> orderedCopyOf(final @NonNull Map<K, V> map) {
        final Map<K, V> common = commonCopy(map);
        if (common != null) {
            return common;
        }

        final int size = map.size();
        if (size == 1) {
            // Efficient single-entry implementation
            final Entry<K, V> e = map.entrySet().iterator().next();
            return SharedSingletonMap.orderedOf(e.getKey(), e.getValue());
        }

        final ImmutableMap<K, Integer> offsets = OffsetMapCache.orderedOffsets(map.keySet());
        return new Ordered<>(offsets, createArray(offsets, map));
    }

    /**
     * Create an {@link ImmutableOffsetMap} as a copy of an existing map. This is actually not completely true, as this
     * method returns an {@link ImmutableMap} for empty and singleton inputs, as those are more memory-efficient. This
     * method also recognizes {@link ImmutableOffsetMap} and {@link SharedSingletonMap} on input, and returns it back
     * without doing anything else. It also recognizes {@link MutableOffsetMap} (as returned by
     * {@link #toModifiableMap()}) and makes an efficient copy of its contents. All other maps are converted to an
     * {@link ImmutableOffsetMap}. Iterator order is not guaranteed to be retained.
     *
     * @param <K> the type of keys maintained by the map
     * @param <V> the type of mapped values
     * @param map Input map, may not be null.
     * @return An isolated, immutable copy of the input map
     * @throws NullPointerException if {@code map} or any of its elements is null.
     */
    public static <K, V> @NonNull Map<K, V> unorderedCopyOf(final @NonNull Map<K, V> map) {
        final Map<K, V> common = commonCopy(map);
        if (common != null) {
            return common;
        }

        if (map.size() == 1) {
            // Efficient single-entry implementation
            final Entry<K, V> e = map.entrySet().iterator().next();
            return SharedSingletonMap.unorderedOf(e.getKey(), e.getValue());
        }

        final ImmutableMap<K, Integer> offsets = OffsetMapCache.unorderedOffsets(map.keySet());
        return new Unordered<>(offsets, createArray(offsets, map));
    }

    private static <K, V> V[] createArray(final ImmutableMap<K, Integer> offsets, final Map<K, V> map) {
        @SuppressWarnings("unchecked")
        final V[] array = (V[]) new Object[offsets.size()];
        for (Entry<K, V> e : map.entrySet()) {
            array[verifyNotNull(offsets.get(e.getKey()))] = e.getValue();
        }
        return array;
    }

    private static <K, V> @Nullable Map<K, V> commonCopy(final @NonNull Map<K, V> map) {
        // Prevent a copy. Note that ImmutableMap is not listed here because of its potentially larger keySet overhead.
        if (map instanceof ImmutableOffsetMap || map instanceof SharedSingletonMap) {
            return map;
        }

        // Familiar and efficient to copy
        if (map instanceof MutableOffsetMap<K, V> mop) {
            return mop.toUnmodifiableMap();
        }

        if (map.isEmpty()) {
            // Shares a single object
            return ImmutableMap.of();
        }

        return null;
    }

    @Override
    public final int size() {
        return offsets.size();
    }

    @Override
    public final boolean isEmpty() {
        return offsets.isEmpty();
    }

    @Override
    public final int hashCode() {
        if (hashCode != 0) {
            return hashCode;
        }

        int result = 0;
        for (Entry<K, Integer> e : offsets.entrySet()) {
            result += e.getKey().hashCode() ^ objects[e.getValue()].hashCode();
        }

        hashCode = result;
        return result;
    }

    @Override
    public final boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Map<?, ?> other)) {
            return false;
        }

        if (obj instanceof ImmutableOffsetMap<?, ?> om) {
            // If the offset match, the arrays have to match, too
            if (offsets.equals(om.offsets)) {
                return Arrays.deepEquals(objects, om.objects);
            }
        } else if (obj instanceof MutableOffsetMap) {
            // Let MutableOffsetMap do the actual work.
            return obj.equals(this);
        }

        // Size and key sets have to match
        if (size() != other.size() || !keySet().equals(other.keySet())) {
            return false;
        }

        try {
            // Ensure all objects are present
            for (Entry<K, Integer> e : offsets.entrySet()) {
                if (!objects[e.getValue()].equals(other.get(e.getKey()))) {
                    return false;
                }
            }
        } catch (ClassCastException e) {
            // Can be thrown by other.get() indicating we have incompatible key types
            return false;
        }

        return true;
    }

    @Override
    public final boolean containsKey(final Object key) {
        return offsets.containsKey(key);
    }

    @Override
    public final boolean containsValue(final Object value) {
        for (Object o : objects) {
            if (value.equals(o)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public final V get(final Object key) {
        Integer offset;
        return (offset = offsets.get(key)) == null ? null : objects[offset];
    }

    @Override
    public final V remove(final Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final V put(final K key, final V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public final void putAll(final Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final Set<K> keySet() {
        return offsets.keySet();
    }

    @Override
    public final @NonNull Collection<V> values() {
        return new ConstantArrayCollection<>(objects);
    }

    @Override
    public final @NonNull Set<Entry<K, V>> entrySet() {
        return new EntrySet();
    }

    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder("{");
        final Iterator<K> it = offsets.keySet().iterator();
        int offset = 0;
        while (it.hasNext()) {
            sb.append(it.next()).append('=').append(objects[offset++]);

            if (it.hasNext()) {
                sb.append(", ");
            }
        }

        return sb.append('}').toString();
    }

    final @NonNull ImmutableMap<K, Integer> offsets() {
        return offsets;
    }

    final @NonNull V[] objects() {
        return objects;
    }

    private final class EntrySet extends AbstractSet<Entry<K, V>> {
        @Override
        public @NonNull Iterator<Entry<K, V>> iterator() {
            final Iterator<Entry<K, Integer>> it = offsets.entrySet().iterator();
            return new UnmodifiableIterator<>() {
                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }

                @Override
                public Entry<K, V> next() {
                    final Entry<K, Integer> e = it.next();
                    return new SimpleImmutableEntry<>(e.getKey(), objects[e.getValue()]);
                }
            };
        }

        @Override
        public int size() {
            return offsets.size();
        }
    }

    @java.io.Serial
    abstract Object writeReplace();
}
