/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.UnmodifiableIterator;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
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
@Beta
public abstract class ImmutableOffsetMap<K, V> implements UnmodifiableMapPhase<K, V>, Serializable {
    static final class Ordered<K, V> extends ImmutableOffsetMap<K, V> {
        private static final long serialVersionUID = 1L;

        Ordered(final ImmutableMap<K, Integer> offsets, final V[] objects) {
            super(offsets, objects);
        }

        @Override
        public @NonNull MutableOffsetMap<K, V> toModifiableMap() {
            return MutableOffsetMap.orderedCopyOf(this);
        }

        @Override
        void setFields(final List<K> keys, final V[] values) throws IOException {
            setField(this, OFFSETS_FIELD, OffsetMapCache.orderedOffsets(keys));
            setField(this, ARRAY_FIELD, values);
        }
    }

    static final class Unordered<K, V> extends ImmutableOffsetMap<K, V> {
        private static final long serialVersionUID = 1L;

        Unordered(final ImmutableMap<K, Integer> offsets, final V[] objects) {
            super(offsets, objects);
        }

        @Override
        public @NonNull MutableOffsetMap<K, V> toModifiableMap() {
            return MutableOffsetMap.unorderedCopyOf(this);
        }

        @Override
        void setFields(final List<K> keys, final V[] values) throws IOException {
            final Map<K, Integer> newOffsets = OffsetMapCache.unorderedOffsets(keys);

            setField(this, OFFSETS_FIELD, newOffsets);
            setField(this, ARRAY_FIELD, OffsetMapCache.adjustedArray(newOffsets, keys, values));
        }
    }

    private static final long serialVersionUID = 1L;

    private final transient @NonNull ImmutableMap<K, Integer> offsets;
    private final transient @NonNull V[] objects;
    private transient int hashCode;

    /**
     * Construct a new instance backed by specified key-to-offset map and array of objects.
     *
     * @param offsets Key-to-offset map, may not be null
     * @param objects Array of value object, may not be null. The array is stored as is, the caller
     *              is responsible for ensuring its contents remain unmodified.
     */
    ImmutableOffsetMap(final ImmutableMap<K, Integer> offsets, final V[] objects) {
        this.offsets = requireNonNull(offsets);
        this.objects = requireNonNull(objects);
        checkArgument(offsets.size() == objects.length);
    }

    @Override
    public abstract @NonNull MutableOffsetMap<K, V> toModifiableMap();

    abstract void setFields(List<K> keys, V[] values) throws IOException;

    /**
     * Create an {@link ImmutableOffsetMap} as a copy of an existing map. This is actually not completely true, as this
     * method returns an {@link ImmutableMap} for empty and singleton inputs, as those are more memory-efficient. This
     * method also recognizes {@link ImmutableOffsetMap} and {@link SharedSingletonMap} on input, and returns it back
     * without doing anything else. It also recognizes {@link MutableOffsetMap} (as returned by
     * {@link #toModifiableMap()}) and makes an efficient copy of its contents. All other maps are converted to an
     * {@link ImmutableOffsetMap} with the same iteration order as input.
     *
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
        @SuppressWarnings("unchecked")
        final V[] array = (V[]) new Object[offsets.size()];
        for (Entry<K, V> e : map.entrySet()) {
            array[offsets.get(e.getKey())] = e.getValue();
        }

        return new Ordered<>(offsets, array);
    }

    /**
     * Create an {@link ImmutableOffsetMap} as a copy of an existing map. This is actually not completely true, as this
     * method returns an {@link ImmutableMap} for empty and singleton inputs, as those are more memory-efficient. This
     * method also recognizes {@link ImmutableOffsetMap} and {@link SharedSingletonMap} on input, and returns it back
     * without doing anything else. It also recognizes {@link MutableOffsetMap} (as returned by
     * {@link #toModifiableMap()}) and makes an efficient copy of its contents. All other maps are converted to an
     * {@link ImmutableOffsetMap}. Iterator order is not guaranteed to be retained.
     *
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
        @SuppressWarnings("unchecked")
        final V[] array = (V[]) new Object[offsets.size()];
        for (Entry<K, V> e : map.entrySet()) {
            array[offsets.get(e.getKey())] = e.getValue();
        }

        return new Unordered<>(offsets, array);
    }

    private static <K, V> @Nullable Map<K, V> commonCopy(final @NonNull Map<K, V> map) {
        // Prevent a copy. Note that ImmutableMap is not listed here because of its potentially larger keySet overhead.
        if (map instanceof ImmutableOffsetMap || map instanceof SharedSingletonMap) {
            return map;
        }

        // Familiar and efficient to copy
        if (map instanceof MutableOffsetMap) {
            return ((MutableOffsetMap<K, V>) map).toUnmodifiableMap();
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
        if (!(obj instanceof Map)) {
            return false;
        }

        if (obj instanceof ImmutableOffsetMap) {
            final ImmutableOffsetMap<?, ?> om = (ImmutableOffsetMap<?, ?>) obj;

            // If the offset match, the arrays have to match, too
            if (offsets.equals(om.offsets)) {
                return Arrays.deepEquals(objects, om.objects);
            }
        } else if (obj instanceof MutableOffsetMap) {
            // Let MutableOffsetMap do the actual work.
            return obj.equals(this);
        }

        final Map<?, ?> other = (Map<?, ?>)obj;

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
            return new UnmodifiableIterator<Entry<K, V>>() {
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

    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.writeInt(offsets.size());
        for (Entry<K, V> e : entrySet()) {
            out.writeObject(e.getKey());
            out.writeObject(e.getValue());
        }
    }

    private static final Field OFFSETS_FIELD = fieldFor("offsets");
    private static final Field ARRAY_FIELD = fieldFor("objects");

    private static @NonNull Field fieldFor(final @NonNull String name) {
        final Field f;
        try {
            f = ImmutableOffsetMap.class.getDeclaredField(name);
        } catch (NoSuchFieldException | SecurityException e) {
            throw new IllegalStateException("Failed to lookup field " + name, e);
        }

        f.setAccessible(true);
        return f;
    }

    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "https://github.com/spotbugs/spotbugs/issues/811")
    private static void setField(final @NonNull ImmutableOffsetMap<?, ?> map, final @NonNull Field field,
            final Object value) throws IOException {
        try {
            field.set(map, value);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new IOException("Failed to set field " + field, e);
        }
    }

    @SuppressWarnings("unchecked")
    private void readObject(final @NonNull ObjectInputStream in) throws IOException, ClassNotFoundException {
        final int s = in.readInt();

        final List<K> keys = new ArrayList<>(s);
        final V[] values = (V[]) new Object[s];

        for (int i = 0; i < s; ++i) {
            keys.add((K)in.readObject());
            values[i] = (V)in.readObject();
        }

        setFields(keys, values);
    }
}
