/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.nodes;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * A mutable version of {@link ImmutableOffsetMap}. It inherits the set of mappings from the immutable version and
 * allows updating/removing existing mappings. New mappings are stored in a dedicated {@link LinkedHashMap} to preserve
 * insertion order. It also tracks the need to duplicate the backing array, so the sequence of
 * <code><pre>
 * ImmutableOffsetMap<K, V> source;
 * ImmutableOffsetMap<K, V> result = source.createMutableClone().immutableCopy();
 * </pre></code>
 * results in source and result sharing the backing objects.
 *
 * @param <K> key type
 * @param <V> value type
 */
final class MutableOffsetMap<K, V> extends AbstractMap<K, V> implements Cloneable {
    private final Map<K, V> newKeys;
    private final Map<K, Integer> offsets;
    private V[] array;
    private int removed = 0;
    private transient volatile int modCount;
    private boolean needClone = true;

    MutableOffsetMap(final Map<K, Integer> offsets, final V[] array) {
        this.offsets = Preconditions.checkNotNull(offsets);
        this.array = Preconditions.checkNotNull(array);
        this.newKeys = new LinkedHashMap<>();
    }

    private MutableOffsetMap(final Map<K, Integer> offsets, final V[] array, final Map<K, V> newKeys, final int removed) {
        this.offsets = Preconditions.checkNotNull(offsets);
        this.array = Preconditions.checkNotNull(array);
        this.newKeys = new LinkedHashMap<>(newKeys);
        this.removed = removed;
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
        if (offset == null) {
            return newKeys.containsKey(key);
        }

        return array[offset] != null;
    }

    @Override
    public V get(final Object key) {
        final Integer offset = offsets.get(key);
        if (offset == null) {
            return newKeys.get(key);
        }

        return array[offset];
    }

    private void cloneArray() {
        if (needClone) {
            array = array.clone();
            needClone = false;
        }
    }

    @Override
    public V put(final K key, final V value) {
        Preconditions.checkNotNull(value);
        final Integer offset = offsets.get(key);
        if (offset == null) {
            final V ret = newKeys.put(key, value);
            if (ret == null) {
                modCount++;
            }
            return ret;
        }

        cloneArray();
        final V ret = array[offset];
        array[offset] = value;
        if (ret == null) {
            modCount++;
            removed--;
        }

        return ret;
    }

    @Override
    public final V remove(final Object key) {
        final Integer offset = offsets.get(key);
        if (offset == null) {
            final V ret = newKeys.remove(key);
            if (ret != null) {
                modCount++;
            }
            return ret;
        }

        cloneArray();
        final V ret = array[offset];
        array[offset] = null;
        if (ret != null) {
            modCount++;
            removed++;
        }

        return ret;
    }

    @Override
    public final void clear() {
        if (size() != 0) {
            newKeys.clear();
            cloneArray();
            Arrays.fill(array, null);
            removed = array.length;
            modCount++;
        }
    }

    @Override
    public final Set<Entry<K, V>> entrySet() {
        return new EntrySet();
    }

    @Nonnull Map<K, V> immutableCopy() {
        if (newKeys.isEmpty() && removed == 0) {
            // Make sure next modification clones the array, as we leak it to the map we return.
            needClone = true;
            /*
             * TODO: we could track the ImmutableOffsetMap from which this one was instantiated and if we do not
             *       perform any modifications, just return the original instance. The trade-off is increased complexity
             *       and an additional field in this class.
             */
            return new ImmutableOffsetMap<>(offsets, array);
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
                    if (array[e.getValue()] != null) {
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
        final V[] values = (V[]) new Object[keyset.size()];
        int o = 0;
        if (removed != 0) {
            if (removed != offsets.size()) {
                for (Entry<K, Integer> e : offsets.entrySet()) {
                    final V v = array[e.getValue()];
                    if (v != null) {
                        values[o++] = v;
                    }
                }
            }
        } else {
            System.arraycopy(array, 0, values, 0, offsets.size());
            o = offsets.size();
        }
        for (V v : newKeys.values()) {
            values[o++] = v;
        }

        return new ImmutableOffsetMap<>(OffsetMapCache.offsetsFor(keyset), values);
    }

    @Override
    public MutableOffsetMap<K, V> clone() {
        return new MutableOffsetMap<K, V>(offsets, array, newKeys, removed);
    }

    @VisibleForTesting
    boolean needClone() {
        return needClone;
    }

    @VisibleForTesting
    Object array() {
        return array;
    }

    @VisibleForTesting
    Object newKeys() {
        return newKeys;
    }

    private final class EntrySet extends AbstractSet<Entry<K, V>> {
        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new EntrySetIterator();
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

    private final class EntrySetIterator implements Iterator<Entry<K, V>> {
        private final Iterator<Entry<K, Integer>> oldIterator = offsets.entrySet().iterator();
        private final Iterator<Entry<K, V>> newIterator = newKeys.entrySet().iterator();
        private int expectedModCount = modCount;
        private K currentKey, nextKey;

        EntrySetIterator() {
            calculateNextKey();
        }

        private void calculateNextKey() {
            while (oldIterator.hasNext()) {
                final Entry<K, Integer> e = oldIterator.next();
                if (array[e.getValue()] != null) {
                    nextKey = e.getKey();
                    return;
                }
            }

            nextKey = newIterator.hasNext() ? newIterator.next().getKey() : null;
        }

        private void checkModCount() {
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }

        @Override
        public boolean hasNext() {
            checkModCount();
            return nextKey != null;
        }

        @Override
        public Entry<K, V> next() {
            if (nextKey == null) {
                throw new NoSuchElementException();
            }

            checkModCount();
            currentKey = nextKey;
            calculateNextKey();

            return new SimpleEntry<>(currentKey, get(currentKey));
        }

        @Override
        public void remove() {
            Preconditions.checkState(currentKey != null);

            checkModCount();
            final Integer offset = offsets.get(currentKey);
            if (offset != null) {
                cloneArray();
                array[offset] = null;
                removed++;
            } else {
                newIterator.remove();
            }

            expectedModCount = ++modCount;
            currentKey = null;
        }
    }
}
