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
 * @param <K> key type
 * @param <V> value type
 */
@Beta
public class MutableOffsetMap<K, V> extends AbstractMap<K, V> implements Cloneable, ModifiableMapPhase<K, V> {
    private static final Object[] EMPTY_ARRAY = new Object[0];
    private static final Object NO_VALUE = new Object();
    private final Map<K, Integer> offsets;
    private final Map<K, V> newKeys;
    private Object[] array;
    private int removed = 0;
    private transient volatile int modCount;
    private boolean needClone = true;

    public MutableOffsetMap() {
        this(Collections.<K>emptySet());
    }

    public MutableOffsetMap(final Collection<K> keySet) {
        if (!keySet.isEmpty()) {
            removed = keySet.size();
            offsets = OffsetMapCache.offsetsFor(keySet);
            array = new Object[removed];
            Arrays.fill(array, NO_VALUE);
        } else {
            offsets = ImmutableMap.of();
            array = EMPTY_ARRAY;
        }

        this.newKeys = new LinkedHashMap<>();
    }

    protected MutableOffsetMap(final Map<K, Integer> offsets, final Object[] array) {
        this.offsets = Preconditions.checkNotNull(offsets);
        this.array = Preconditions.checkNotNull(array);
        this.newKeys = new LinkedHashMap<>();
    }

    protected MutableOffsetMap(final Map<K, Integer> offsets, final Object[] array, final Map<K, V> newKeys, final int removed) {
        this.offsets = Preconditions.checkNotNull(offsets);
        this.array = Preconditions.checkNotNull(array);
        this.newKeys = new LinkedHashMap<>(newKeys);
        this.removed = removed;
    }

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
        if (offset == null) {
            return newKeys.containsKey(key);
        }

        return !NO_VALUE.equals(array[offset]);
    }

    @Override
    public final V get(final Object key) {
        final Integer offset = offsets.get(key);
        if (offset == null) {
            return newKeys.get(key);
        }

        final Object o = array[offset];
        if (!NO_VALUE.equals(o)) {
            @SuppressWarnings("unchecked")
            final K k = (K)key;
            return objectToValue(k, o);
        } else {
            return null;
        }
    }

    private void cloneArray() {
        if (needClone) {
            needClone = false;
            if (!EMPTY_ARRAY.equals(array)) {
                array = array.clone();
            }
        }
    }

    @Override
    public final V put(final K key, final V value) {
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
        final Object ret = array[offset];
        array[offset] = valueToObject(value);
        if (NO_VALUE.equals(ret)) {
            modCount++;
            removed--;
            return null;
        }

        return objectToValue(key, ret);
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
        final Object ret = array[offset];
        array[offset] = NO_VALUE;
        if (!NO_VALUE.equals(ret)) {
            modCount++;
            removed++;
            @SuppressWarnings("unchecked")
            final K k = (K)key;
            return objectToValue(k, ret);
        } else {
            return null;
        }
    }

    @Override
    public final void clear() {
        if (size() != 0) {
            newKeys.clear();
            cloneArray();
            Arrays.fill(array, NO_VALUE);
            removed = array.length;
            modCount++;
        }
    }

    @Override
    public final Set<Entry<K, V>> entrySet() {
        return new EntrySet();
    }

    @Override
    public final Map<K, V> toUnmodifiableMap() {
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
                    if (!NO_VALUE.equals(array[e.getValue()])) {
                        keyset.add(e.getKey());
                    }
                }
            }
        } else {
            keyset.addAll(offsets.keySet());
        }
        keyset.addAll(newKeys.keySet());

        // Construct the values
        final Object[] values = new Object[keyset.size()];
        int i = 0;
        if (removed != 0) {
            if (removed != offsets.size()) {
                for (Entry<K, Integer> e : offsets.entrySet()) {
                    final Object o = array[e.getValue()];
                    if (!NO_VALUE.equals(o)) {
                        values[i++] = o;
                    }
                }
            }
        } else {
            System.arraycopy(array, 0, values, 0, offsets.size());
            i = offsets.size();
        }
        for (V v : newKeys.values()) {
            values[i++] = valueToObject(v);
        }

        return new ImmutableOffsetMap<>(OffsetMapCache.offsetsFor(keyset), values);
    }

    @SuppressWarnings("unchecked")
    protected V objectToValue(final K key, final Object value) {
        return (V)value;
    }

    protected Object valueToObject(final V value) {
        return value;
    }

    @Override
    public MutableOffsetMap<K, V> clone() throws CloneNotSupportedException {
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
                if (!NO_VALUE.equals(array[e.getValue()])) {
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
                array[offset] = NO_VALUE;
                removed++;
            } else {
                newIterator.remove();
            }

            expectedModCount = ++modCount;
            currentKey = null;
        }
    }
}
