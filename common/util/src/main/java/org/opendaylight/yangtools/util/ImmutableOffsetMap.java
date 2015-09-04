/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.UnmodifiableIterator;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * Implementation of the {@link Map} interface which stores a set of immutable mappings using a key-to-offset map and
 * a backing array. This is useful for situations where the same key set is shared across a multitude of maps, as this
 * class uses a global cache to share the key-to-offset mapping.
 *
 * This map supports creation of value objects on the fly. To achieve that, subclasses should override {@link #valueToObject(Object)},
 * {@link #objectToValue(Object, Object)}, {@link #clone()} and {@link #toModifiableMap()} methods.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
@Beta
public class ImmutableOffsetMap<K, V> extends AbstractLazyValueMap<K, V> implements UnmodifiableMapPhase<K, V>, Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<K, Integer> offsets;
    private final Object[] objects;

    /**
     * Construct a new instance backed by specified key-to-offset map and array of objects.
     *
     * @param offsets Key-to-offset map, may not be null
     * @param objects Array of value object, may not be null. The array is stored as is, the caller
     *              is responsible for ensuring its contents remain unmodified.
     */
    ImmutableOffsetMap(@Nonnull final Map<K, Integer> offsets, @Nonnull final Object[] objects) {
        this.offsets = Preconditions.checkNotNull(offsets);
        this.objects = Preconditions.checkNotNull(objects);
        Preconditions.checkArgument(offsets.size() == objects.length);
    }

    /**
     * Construct a new instance based on some other instance.
     *
     * @param m Instance to share data with, may not be null.
     */
    protected ImmutableOffsetMap(@Nonnull final ImmutableOffsetMap<K, V> m) {
        this.offsets = m.offsets;
        this.objects = m.objects;
    }

    /**
     * Create an {@link ImmutableOffsetMap} as a copy of an existing map. This is actually not completely true,
     * as this method returns an {@link ImmutableMap} for empty and singleton inputs, as those are more memory-efficient.
     * This method also recognizes {@link ImmutableOffsetMap} on input, and returns it back without doing anything else.
     * It also recognizes {@link MutableOffsetMap} (as returned by {@link #toModifiableMap()}) and makes an efficient
     * copy of its contents. All other maps are converted to an {@link ImmutableOffsetMap} with the same iteration
     * order as input.
     *
     * @param m Input map, may not be null.
     * @return An isolated, immutable copy of the input map
     */
    @Nonnull public static <K, V> Map<K, V> copyOf(@Nonnull final Map<K, V> m) {
        // Prevent a copy. Note that ImmutableMap is not listed here because of its potentially larger keySet overhead.
        if (m instanceof ImmutableOffsetMap || m instanceof SingletonImmutableOffsetMap) {
            return m;
        }

        // Familiar and efficient to copy
        if (m instanceof MutableOffsetMap) {
            return ((MutableOffsetMap<K, V>) m).toUnmodifiableMap();
        }

        final int size = m.size();
        if (size == 0) {
            // Shares a single object
            return ImmutableMap.of();
        }
        if (size == 1) {
            // Efficient single-entry implementation
            final Entry<K, V> e = m.entrySet().iterator().next();
            return new SingletonImmutableOffsetMap<K, V>(e.getKey(), e.getValue());
        }

        final Map<K, Integer> offsets = OffsetMapCache.offsetsFor(m.keySet());
        @SuppressWarnings("unchecked")
        final V[] array = (V[]) new Object[offsets.size()];
        for (Entry<K, V> e : m.entrySet()) {
            array[offsets.get(e.getKey())] = e.getValue();
        }

        return new ImmutableOffsetMap<>(offsets, array);
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
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (o == null) {
            return false;
        }

        if (o instanceof ImmutableOffsetMap) {
            final ImmutableOffsetMap<?, ?> om = (ImmutableOffsetMap<?, ?>) o;
            if (offsets.equals(om.offsets) && Arrays.deepEquals(objects, om.objects)) {
                return true;
            }
        } else if (o instanceof MutableOffsetMap) {
            // Let MutableOffsetMap do the actual work.
            return o.equals(this);
        } else if (o instanceof Map) {
            final Map<?, ?> om = (Map<?, ?>)o;

            // Size and key sets have to match
            if (size() != om.size() || !keySet().equals(om.keySet())) {
                return false;
            }

            try {
                // Ensure all objects are present
                for (Entry<K, Integer> e : offsets.entrySet()) {
                    final V v = objectToValue(e.getKey(), objects[e.getValue()]);
                    if (!v.equals(om.get(e.getKey()))) {
                        return false;
                    }
                }
            } catch (ClassCastException e) {
                // Can be thrown by om.get() and indicate we have incompatible key types
                return false;
            }

            return true;
        }

        return false;
    }

    @Override
    public final boolean containsKey(final Object key) {
        return offsets.containsKey(key);
    }

    @Override
    public final boolean containsValue(final Object value) {
        @SuppressWarnings("unchecked")
        final Object obj = valueToObject((V)value);
        for (Object o : objects) {
            if (Objects.equals(obj, o)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final V get(final Object key) {
        final Integer offset = offsets.get(key);
        if (offset == null) {
            return null;
        }

        return objectToValue((K) key, objects[offset]);
    }

    @Override
    public final V remove(final Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
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
    public final Set<Entry<K, V>> entrySet() {
        return new EntrySet();
    }

    @Override
    public MutableOffsetMap<K, V> toModifiableMap() {
        return new MutableOffsetMap<>(this);
    }

    Map<K, Integer> offsets() {
        return offsets;
    }

    Object[] objects() {
        return objects;
    }

    private final class EntrySet extends AbstractSet<Entry<K, V>> {
        @Override
        public Iterator<Entry<K, V>> iterator() {
            final Iterator<Entry<K, Integer>> it = offsets.entrySet().iterator();

            return new UnmodifiableIterator<Entry<K, V>>() {
                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }

                @Override
                public Entry<K, V> next() {
                    final Entry<K, Integer> e = it.next();
                    return new SimpleEntry<>(e.getKey(), objectToValue(e.getKey(), objects[e.getValue()]));
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

    private static Field fieldFor(final String name) {
        final Field f;
        try {
            f = ImmutableOffsetMap.class.getDeclaredField(name);
        } catch (NoSuchFieldException | SecurityException e) {
            throw new IllegalStateException("Failed to lookup field " + name, e);
        }

        f.setAccessible(true);
        return f;
    }

    private void setField(final Field field, final Object value) throws IOException {
        try {
            field.set(this, value);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new IOException("Failed to set field " + field, e);
        }
    }

    @SuppressWarnings("unchecked")
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        final int s = in.readInt();

        final List<K> keys = new ArrayList<>(s);
        final V[] values = (V[]) new Object[s];

        for (int i = 0; i < s; ++i) {
            keys.add((K)in.readObject());
            values[i] = (V)in.readObject();
        }

        setField(OFFSETS_FIELD, OffsetMapCache.offsetsFor(keys));
        setField(ARRAY_FIELD, values);
    }
}
