/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

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
 * This map does not support null keys nor values.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
@Beta
public final class MutableOffsetMap<K, V> extends AbstractMap<K, V> implements Cloneable, ModifiableMapPhase<K, V> {
    private static final Object[] EMPTY_ARRAY = new Object[0];
    private final Map<K, Integer> offsets;
    private final Map<K, V> newKeys;
    private V[] objects;
    private int removed = 0;
    private transient volatile int modCount;
    private boolean needClone = true;

    public MutableOffsetMap() {
        this(Collections.<K>emptySet());
    }

    @SuppressWarnings("unchecked")
    protected MutableOffsetMap(final Collection<K> keySet) {
        if (!keySet.isEmpty()) {
            removed = keySet.size();
            offsets = OffsetMapCache.offsetsFor(keySet);
            objects = (V[])new Object[removed];
        } else {
            offsets = ImmutableMap.of();
            objects = (V[])EMPTY_ARRAY;
        }

        this.newKeys = new LinkedHashMap<>();
    }

    protected MutableOffsetMap(final ImmutableOffsetMap<K, V> m) {
        this(m.offsets(), m.objects());
    }

    @SuppressWarnings("unchecked")
    protected MutableOffsetMap(final Map<K, V> m) {
        this(OffsetMapCache.offsetsFor(m.keySet()), (V[])m.values().toArray());
    }

    protected MutableOffsetMap(final MutableOffsetMap<K, V> m) {
        this.offsets = m.offsets;
        this.objects = m.objects;
        this.newKeys = new LinkedHashMap<>(m.newKeys);
        this.removed = m.removed;
    }

    private MutableOffsetMap(final Map<K, Integer> offsets, final V[] objects) {
        this.offsets = Preconditions.checkNotNull(offsets);
        this.objects = Preconditions.checkNotNull(objects);
        this.newKeys = new LinkedHashMap<>();
    }

    public static <K, V> MutableOffsetMap<K, V> copyOf(final Map<K, V> m) {
        if (m instanceof MutableOffsetMap) {
            return ((MutableOffsetMap<K, V>) m).clone();
        }
        if (m instanceof ImmutableOffsetMap) {
            return ((ImmutableOffsetMap<K, V>) m).toModifiableMap();
        }

        return new MutableOffsetMap<>(m);
    }

    public static <K, V> MutableOffsetMap<K, V> forOffsets(final Map<K, Integer> offsets) {
        @SuppressWarnings("unchecked")
        final V[] objects = (V[]) new Object[offsets.size()];
        return new MutableOffsetMap<>(offsets, objects);
    }

    public static <K, V> MutableOffsetMap<K, V> forKeySet(final Collection<K> keySet) {
        return forOffsets(OffsetMapCache.offsetsFor(keySet));
    }

    @Override
    public int size() {
        return offsets.size() + newKeys.size() - removed;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(final Object key) {
        final Integer offset = offsets.get(key);
        return offset == null ? newKeys.containsKey(key) : objects[offset] != null;
    }

    @Override
    public V get(final Object key) {
        final Integer offset = offsets.get(key);
        return offset == null ? newKeys.get(key) : objects[offset];
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
    public V put(final K key, final V value) {
        Preconditions.checkNotNull(value);
        final Integer offset = offsets.get(Preconditions.checkNotNull(key));
        if (offset == null) {
            final V ret = newKeys.put(key, value);
            if (ret == null) {
                modCount++;
            }
            return ret;
        }

        cloneArray();
        final V ret = objects[offset];
        objects[offset] = value;
        if (ret == null) {
            modCount++;
            removed--;
        }

        return ret;
    }

    @Override
    public V remove(final Object key) {
        final Integer offset = offsets.get(key);
        if (offset == null) {
            final V ret = newKeys.remove(key);
            if (ret != null) {
                modCount++;
            }
            return ret;
        }

        cloneArray();
        final V ret = objects[offset];
        objects[offset] = null;
        if (ret != null) {
            modCount++;
            removed++;
        }
        return ret;
    }

    @Override
    public void clear() {
        if (size() != 0) {
            newKeys.clear();
            cloneArray();
            Arrays.fill(objects, null);
            removed = objects.length;
            modCount++;
        }
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new EntrySet();
    }

    @Override
    public Map<K, V> toUnmodifiableMap() {
        if (newKeys.isEmpty() && removed == 0) {
            // Make sure next modification clones the array, as we leak it to the map we return.
            needClone = true;
            /*
             * TODO: we could track the ImmutableOffsetMap from which this one was instantiated and if we do not
             *       perform any modifications, just return the original instance. The trade-off is increased complexity
             *       and an additional field in this class.
             */
            return new ImmutableOffsetMap.Ordered<>(offsets, objects);
        }

        final int s = size();
        if (s == 0) {
            return ImmutableMap.of();
        }
        if (s == 1) {
            return ImmutableMap.copyOf(this);
        }

        // Construct the set of keys
        final Collection<K> keyset = new ArrayList<>(s);
        if (removed != 0) {
            if (removed != offsets.size()) {
                for (Entry<K, Integer> e : offsets.entrySet()) {
                    if (objects[e.getValue()] != null) {
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
        int i = 0;
        if (removed != 0) {
            if (removed != offsets.size()) {
                for (Entry<K, Integer> e : offsets.entrySet()) {
                    final V o = objects[e.getValue()];
                    if (o != null) {
                        values[i++] = o;
                    }
                }
            }
        } else {
            System.arraycopy(objects, 0, values, 0, offsets.size());
            i = offsets.size();
        }
        for (V v : newKeys.values()) {
            values[i++] = v;
        }

        return new ImmutableOffsetMap.Ordered<>(OffsetMapCache.offsetsFor(keyset), values);
    }

    @Override
    public MutableOffsetMap<K, V> clone() {
        // FIXME: super.clone()
        return new MutableOffsetMap<K, V>(this);
    }

    @Override
    public int hashCode() {
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
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Map)) {
            return false;
        }

        if (o instanceof ImmutableOffsetMap) {
            final ImmutableOffsetMap<?, ?> om = (ImmutableOffsetMap<?, ?>) o;

            if (newKeys.isEmpty() && offsets.equals(om.offsets())) {
                return Arrays.deepEquals(objects, om.objects());
            }
        } else if (o instanceof MutableOffsetMap) {
            final MutableOffsetMap<?, ?> om = (MutableOffsetMap<?, ?>) o;

            if (offsets.equals(om.offsets)) {
                return Arrays.deepEquals(objects, om.objects) && newKeys.equals(om.newKeys);
            }
        }

        // Fall back to brute map compare
        final Map<?, ?> other = (Map<?, ?>)o;

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
                final V v = objects[e.getValue()];
                if (v != null && !v.equals(other.get(e.getKey()))) {
                    return false;
                }
            }
        } catch (ClassCastException e) {
            // Can be thrown by other.get() and indicate we have incompatible key types
            return false;
        }

        return true;
    }

    @Override
    public Set<K> keySet() {
        return new KeySet();
    }

    @VisibleForTesting
    boolean needClone() {
        return needClone;
    }

    @VisibleForTesting
    Object array() {
        return objects;
    }

    @VisibleForTesting
    Object newKeys() {
        return newKeys;
    }

    private final class EntrySet extends AbstractSet<Entry<K, V>> {
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
        public boolean add(final Entry<K, V> e) {
            Preconditions.checkNotNull(e.getValue());
            final V p = MutableOffsetMap.this.put(e.getKey(), e.getValue());
            return !e.getValue().equals(p);
        }

        @Override
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
        private K currentKey, nextKey;

        AbstractSetIterator() {
            updateNextKey();
        }

        private void updateNextKey() {
            while (oldIterator.hasNext()) {
                final Entry<K, Integer> e = oldIterator.next();
                if (objects[e.getValue()] != null) {
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
            Preconditions.checkState(currentKey != null);

            checkModCount();
            final Integer offset = offsets.get(currentKey);
            if (offset != null) {
                cloneArray();
                objects[offset] = null;
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
