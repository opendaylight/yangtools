/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * A mutable version of {@link ImmutableOffsetMap}. It inherits the set of mappings from the immutable version and
 * allows updating/removing existing mappings. New mappings are stored in a dedicated {@link LinkedHashMap} to preserve
 * insertion order. It also tracks the need to duplicate the backing array, so the sequence of
 * <code>
 * ImmutableOffsetMap&lt;K, V&gt; source;
 * ImmutableOffsetMap&lt;K, V&gt; result = source.createMutableClone().immutableCopy();
 * </code>
 * results in source and result sharing the backing objects.
 *
 * <p>This map does not support null keys nor values.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
@Beta
public abstract class MutableOffsetMap<K, V> extends AbstractMap<K, V> implements Cloneable, ModifiableMapPhase<K, V> {
    static final class Ordered<K, V> extends MutableOffsetMap<K, V> {
        Ordered() {
            super(new LinkedHashMap<>());
        }

        Ordered(final Map<K, V> source) {
            super(OffsetMapCache.orderedOffsets(source.keySet()), source, new LinkedHashMap<>());
        }

        Ordered(final Map<K, Integer> offsets, final V[] objects) {
            super(offsets, objects, new LinkedHashMap<>());
        }

        @Override
        Object removedObject() {
            return REMOVED;
        }

        @Override
        UnmodifiableMapPhase<K, V> modifiedMap(final List<K> keys, final V[] objects) {
            return new ImmutableOffsetMap.Ordered<>(OffsetMapCache.orderedOffsets(keys), objects);
        }

        @Override
        UnmodifiableMapPhase<K, V> unmodifiedMap(final Map<K, Integer> offsets, final V[] objects) {
            return new ImmutableOffsetMap.Ordered<>(offsets, objects);
        }

        @Override
        SharedSingletonMap<K, V> singletonMap() {
            return SharedSingletonMap.orderedCopyOf(this);
        }
    }

    static final class Unordered<K, V> extends MutableOffsetMap<K, V> {
        Unordered() {
            super(new HashMap<>());
        }

        Unordered(final Map<K, V> source) {
            super(OffsetMapCache.unorderedOffsets(source.keySet()), source, new HashMap<>());
        }

        Unordered(final Map<K, Integer> offsets, final V[] objects) {
            super(offsets, objects, new HashMap<>());
        }

        @Override
        Object removedObject() {
            return null;
        }

        @Override
        UnmodifiableMapPhase<K, V> modifiedMap(final List<K> keys, final V[] objects) {
            final Map<K, Integer> offsets = OffsetMapCache.unorderedOffsets(keys);
            return new ImmutableOffsetMap.Unordered<>(offsets, OffsetMapCache.adjustedArray(offsets, keys, objects));
        }

        @Override
        UnmodifiableMapPhase<K, V> unmodifiedMap(final Map<K, Integer> offsets, final V[] objects) {
            return new ImmutableOffsetMap.Unordered<>(offsets, objects);
        }

        @Override
        SharedSingletonMap<K, V> singletonMap() {
            return SharedSingletonMap.unorderedCopyOf(this);
        }
    }

    private static final Object[] EMPTY_ARRAY = new Object[0];
    private static final Object REMOVED = new Object();
    private final Map<K, Integer> offsets;
    private HashMap<K, V> newKeys;
    private Object[] objects;
    private int removed = 0;
    private transient volatile int modCount;
    private boolean needClone = true;

    MutableOffsetMap(final Map<K, Integer> offsets, final V[] objects, final HashMap<K, V> newKeys) {
        verify(newKeys.isEmpty());
        this.offsets = requireNonNull(offsets);
        this.objects = requireNonNull(objects);
        this.newKeys = requireNonNull(newKeys);
    }

    @SuppressWarnings("unchecked")
    MutableOffsetMap(final HashMap<K, V> newKeys) {
        this(ImmutableMap.of(), (V[]) EMPTY_ARRAY, newKeys);
    }

    @SuppressWarnings("unchecked")
    MutableOffsetMap(final Map<K, Integer> offsets, final Map<K, V> source, final HashMap<K, V> newKeys) {
        this(offsets, (V[]) new Object[offsets.size()], newKeys);

        for (Entry<K, V> e : source.entrySet()) {
            objects[offsets.get(e.getKey())] = requireNonNull(e.getValue());
        }

        this.needClone = false;
    }

    public static <K, V> MutableOffsetMap<K, V> orderedCopyOf(final Map<K, V> map) {
        if (map instanceof Ordered) {
            return ((Ordered<K, V>) map).clone();
        }
        if (map instanceof ImmutableOffsetMap) {
            final ImmutableOffsetMap<K, V> om = (ImmutableOffsetMap<K, V>) map;
            return new Ordered<>(om.offsets(), om.objects());
        }

        return new Ordered<>(map);
    }

    public static <K, V> MutableOffsetMap<K, V> unorderedCopyOf(final Map<K, V> map) {
        if (map instanceof Unordered) {
            return ((Unordered<K, V>) map).clone();
        }
        if (map instanceof ImmutableOffsetMap) {
            final ImmutableOffsetMap<K, V> om = (ImmutableOffsetMap<K, V>) map;
            return new Unordered<>(om.offsets(), om.objects());
        }

        return new Unordered<>(map);
    }

    public static <K, V> MutableOffsetMap<K, V> ordered() {
        return new MutableOffsetMap.Ordered<>();
    }

    public static <K, V> MutableOffsetMap<K, V> unordered() {
        return new MutableOffsetMap.Unordered<>();
    }

    abstract Object removedObject();

    abstract UnmodifiableMapPhase<K, V> modifiedMap(List<K> keys, V[] objects);

    abstract UnmodifiableMapPhase<K, V> unmodifiedMap(Map<K, Integer> offsets, V[] objects);

    abstract SharedSingletonMap<K, V> singletonMap();

    @Override
    public final int size() {
        return offsets.size() + newKeys.size() - removed;
    }

    @Override
    public final boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public final boolean containsKey(final Object key) {
        final Integer offset = offsets.get(key);
        if (offset != null) {
            final Object obj = objects[offset];
            if (!REMOVED.equals(obj)) {
                return obj != null;
            }
        }

        return newKeys.containsKey(key);
    }

    @Override
    public final V get(final Object key) {
        final Integer offset = offsets.get(key);
        if (offset != null) {
            final Object obj = objects[offset];

            /*
             * This is a bit tricky:  Ordered will put REMOVED to removed objects to retain strict insertion order.
             * Unordered will add null, indicating that the slot may be reused in future. Hence if we see a REMOVED
             * marker, we need to fall back to checking with new keys.
             */
            if (!REMOVED.equals(obj)) {
                @SuppressWarnings("unchecked")
                final V ret = (V)obj;
                return ret;
            }
        }

        return newKeys.get(key);
    }

    private void cloneArray() {
        if (needClone) {
            needClone = false;
            if (!EMPTY_ARRAY.equals(objects)) {
                objects = objects.clone();
            }
        }
    }

    @Override
    public final V put(final K key, final V value) {
        requireNonNull(value);
        final Integer offset = offsets.get(requireNonNull(key));
        if (offset != null) {
            final Object obj = objects[offset];

            /*
             * Put which can potentially replace something in objects. Replacing an object does not cause iterators
             * to be invalidated and does follow insertion order (since it is not a fresh insert). If the object has
             * been removed, we fall back to newKeys.
             */
            if (!REMOVED.equals(obj)) {
                @SuppressWarnings("unchecked")
                final V ret = (V)obj;

                cloneArray();
                objects[offset] = value;
                if (ret == null) {
                    modCount++;
                    removed--;
                }

                return ret;
            }
        }

        final V ret = newKeys.put(key, value);
        if (ret == null) {
            modCount++;
        }
        return ret;
    }

    @Override
    public final V remove(final Object key) {
        final Integer offset = offsets.get(key);
        if (offset != null) {
            final Object obj = objects[offset];

            /*
             * A previous remove() may have indicated that the objects slot cannot be reused. In that case we need
             * to fall back to checking with newKeys.
             */
            if (!REMOVED.equals(obj)) {
                cloneArray();

                @SuppressWarnings("unchecked")
                final V ret = (V)obj;
                objects[offset] = removedObject();
                if (ret != null) {
                    modCount++;
                    removed++;
                }
                return ret;
            }
        }

        final V ret = newKeys.remove(key);
        if (ret != null) {
            modCount++;
        }
        return ret;
    }

    @Override
    public final void clear() {
        if (size() != 0) {
            newKeys.clear();
            cloneArray();
            Arrays.fill(objects, removedObject());
            removed = objects.length;
            modCount++;
        }
    }

    @Nonnull
    @Override
    public final Set<Entry<K, V>> entrySet() {
        return new EntrySet();
    }

    @Nonnull
    @Override
    public Map<K, V> toUnmodifiableMap() {
        if (removed == 0 && newKeys.isEmpty()) {
            // Make sure next modification clones the array, as we leak it to the map we return.
            needClone = true;

            // We have ended up with no removed objects, hence this cast is safe
            @SuppressWarnings("unchecked")
            final V[] values = (V[])objects;

            /*
             * TODO: we could track the ImmutableOffsetMap from which this one was instantiated and if we do not
             *       perform any modifications, just return the original instance. The trade-off is increased complexity
             *       and an additional field in this class.
             */
            return unmodifiedMap(offsets, values);
        }

        final int s = size();
        if (s == 0) {
            return ImmutableMap.of();
        }
        if (s == 1) {
            return singletonMap();
        }

        // Construct the set of keys
        final List<K> keyset = new ArrayList<>(s);
        if (removed != 0) {
            if (removed != offsets.size()) {
                for (Entry<K, Integer> e : offsets.entrySet()) {
                    final Object o = objects[e.getValue()];
                    if (o != null && !REMOVED.equals(o)) {
                        keyset.add(e.getKey());
                    }
                }
            }
        } else {
            keyset.addAll(offsets.keySet());
        }
        keyset.addAll(newKeys.keySet());

        // Construct the values
        @SuppressWarnings("unchecked")
        final V[] values = (V[])new Object[keyset.size()];
        int offset = 0;
        if (removed != 0) {
            if (removed != offsets.size()) {
                for (Entry<K, Integer> e : offsets.entrySet()) {
                    final Object o = objects[e.getValue()];
                    if (o != null && !REMOVED.equals(o)) {
                        @SuppressWarnings("unchecked")
                        final V v = (V) o;
                        values[offset++] = v;
                    }
                }
            }
        } else {
            System.arraycopy(objects, 0, values, 0, offsets.size());
            offset = offsets.size();
        }
        for (V v : newKeys.values()) {
            values[offset++] = v;
        }

        return modifiedMap(keyset, values);
    }

    @SuppressWarnings("unchecked")
    @Override
    public MutableOffsetMap<K, V> clone() {
        final MutableOffsetMap<K, V> ret;

        try {
            ret = (MutableOffsetMap<K, V>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("Clone is expected to work", e);
        }

        ret.newKeys = (HashMap<K, V>) newKeys.clone();
        ret.needClone = true;
        return ret;
    }

    @Override
    public final int hashCode() {
        int result = 0;

        for (Entry<K, Integer> e : offsets.entrySet()) {
            final Object v = objects[e.getValue()];
            if (v != null) {
                result += e.getKey().hashCode() ^ v.hashCode();
            }
        }

        return result + newKeys.hashCode();
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

            if (newKeys.isEmpty() && offsets.equals(om.offsets())) {
                return Arrays.deepEquals(objects, om.objects());
            }
        } else if (obj instanceof MutableOffsetMap) {
            final MutableOffsetMap<?, ?> om = (MutableOffsetMap<?, ?>) obj;

            if (offsets.equals(om.offsets)) {
                return Arrays.deepEquals(objects, om.objects) && newKeys.equals(om.newKeys);
            }
        }

        // Fall back to brute map compare
        final Map<?, ?> other = (Map<?, ?>)obj;

        // Size and key sets have to match
        if (size() != other.size() || !keySet().equals(other.keySet())) {
            return false;
        }

        try {
            // Ensure all newKeys are present. Note newKeys is guaranteed to
            // not contain null value.
            for (Entry<K, V> e : newKeys.entrySet()) {
                if (!e.getValue().equals(other.get(e.getKey()))) {
                    return false;
                }
            }

            // Ensure all objects are present
            for (Entry<K, Integer> e : offsets.entrySet()) {
                final Object val = objects[e.getValue()];
                if (val != null && !REMOVED.equals(val) && !val.equals(other.get(e.getKey()))) {
                    return false;
                }
            }
        } catch (ClassCastException e) {
            // Can be thrown by other.get() and indicate we have incompatible key types
            return false;
        }

        return true;
    }

    @Nonnull
    @Override
    public final Set<K> keySet() {
        return new KeySet();
    }

    @VisibleForTesting
    final boolean needClone() {
        return needClone;
    }

    @VisibleForTesting
    final Object array() {
        return objects;
    }

    @VisibleForTesting
    final Object newKeys() {
        return newKeys;
    }

    private final class EntrySet extends AbstractSet<Entry<K, V>> {
        @Nonnull
        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new AbstractSetIterator<Entry<K, V>>() {
                @Override
                public Entry<K, V> next() {
                    final K key = nextKey();
                    return new SimpleEntry<>(key, get(key));
                }
            };
        }

        @Override
        public int size() {
            return MutableOffsetMap.this.size();
        }

        @Override
        @SuppressWarnings("checkstyle:parameterName")
        public boolean contains(final Object o) {
            if (!(o instanceof Entry)) {
                return false;
            }

            @SuppressWarnings("unchecked")
            final Entry<K,V> e = (Entry<K,V>) o;
            if (e.getValue() == null) {
                return false;
            }

            return e.getValue().equals(MutableOffsetMap.this.get(e.getKey()));
        }

        @Override
        @SuppressWarnings("checkstyle:parameterName")
        public boolean add(final Entry<K, V> e) {
            final V v = requireNonNull(e.getValue());
            final V p = MutableOffsetMap.this.put(e.getKey(), v);
            return !v.equals(p);
        }

        @Override
        @SuppressWarnings("checkstyle:parameterName")
        public boolean remove(final Object o) {
            if (!(o instanceof Entry)) {
                return false;
            }

            @SuppressWarnings("unchecked")
            final Entry<K,V> e = (Entry<K,V>) o;
            if (e.getValue() == null) {
                return false;
            }

            final V v = MutableOffsetMap.this.get(e.getKey());
            if (e.getValue().equals(v)) {
                MutableOffsetMap.this.remove(e.getKey());
                return true;
            }
            return false;
        }

        @Override
        public void clear() {
            MutableOffsetMap.this.clear();
        }
    }

    private final class KeySet extends AbstractSet<K> {
        @Nonnull
        @Override
        public Iterator<K> iterator() {
            return new AbstractSetIterator<K>() {
                @Override
                public K next() {
                    return nextKey();
                }
            };
        }

        @Override
        public int size() {
            return MutableOffsetMap.this.size();
        }
    }

    private abstract class AbstractSetIterator<E> implements Iterator<E> {
        private final Iterator<Entry<K, Integer>> oldIterator = offsets.entrySet().iterator();
        private final Iterator<K> newIterator = newKeys.keySet().iterator();
        private int expectedModCount = modCount;
        private K currentKey;
        private K nextKey;

        AbstractSetIterator() {
            updateNextKey();
        }

        private void updateNextKey() {
            while (oldIterator.hasNext()) {
                final Entry<K, Integer> e = oldIterator.next();
                final Object obj = objects[e.getValue()];
                if (obj != null && !REMOVED.equals(obj)) {
                    nextKey = e.getKey();
                    return;
                }
            }

            nextKey = newIterator.hasNext() ? newIterator.next() : null;
        }

        private void checkModCount() {
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }

        @Override
        public final boolean hasNext() {
            checkModCount();
            return nextKey != null;
        }

        @Override
        public final void remove() {
            requireNonNull(currentKey != null);

            checkModCount();
            final Integer offset = offsets.get(currentKey);
            if (offset != null) {
                cloneArray();
                objects[offset] = removedObject();
                removed++;
            } else {
                newIterator.remove();
            }

            expectedModCount = ++modCount;
            currentKey = null;
        }

        protected final K nextKey() {
            if (nextKey == null) {
                throw new NoSuchElementException();
            }

            checkModCount();
            currentKey = nextKey;
            updateNextKey();

            return currentKey;
        }
    }
}
