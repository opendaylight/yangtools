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
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
@Beta
public class MutableOffsetMap<K, V> extends AbstractLazyValueMap<K, V> implements Cloneable, ModifiableMapPhase<K, V> {
    private static final Object[] EMPTY_ARRAY = new Object[0];
    private static final Object NO_VALUE = new Object();
    private final Map<K, Integer> offsets;
    private final Map<K, V> newKeys;
    private Object[] objects;
    private int removed = 0;
    private transient volatile int modCount;
    private boolean needClone = true;

    public MutableOffsetMap() {
        this(Collections.<K>emptySet());
    }

    protected MutableOffsetMap(final Collection<K> keySet) {
        if (!keySet.isEmpty()) {
            removed = keySet.size();
            offsets = OffsetMapCache.offsetsFor(keySet);
            objects = new Object[removed];
            Arrays.fill(objects, NO_VALUE);
        } else {
            offsets = ImmutableMap.of();
            objects = EMPTY_ARRAY;
        }

        this.newKeys = new LinkedHashMap<>();
    }

    protected MutableOffsetMap(final ImmutableOffsetMap<K, V> m) {
        this(m.offsets(), m.objects());
    }

    protected MutableOffsetMap(final Map<K, V> m) {
        this(OffsetMapCache.offsetsFor(m.keySet()), m.values().toArray());
    }

    protected MutableOffsetMap(final MutableOffsetMap<K, V> m) {
        this.offsets = m.offsets;
        this.objects = m.objects;
        this.newKeys = new LinkedHashMap<>(m.newKeys);
        this.removed = m.removed;
    }

    private MutableOffsetMap(final Map<K, Integer> offsets, final Object[] objects) {
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
        final Object[] objects = new Object[offsets.size()];
        Arrays.fill(objects, NO_VALUE);

        return new MutableOffsetMap<>(offsets, objects);
    }

    public static <K, V> MutableOffsetMap<K, V> forKeySet(final Collection<K> keySet) {
        return forOffsets(OffsetMapCache.offsetsFor(keySet));
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

        return !NO_VALUE.equals(objects[offset]);
    }

    @Override
    public final V get(final Object key) {
        final Integer offset = offsets.get(key);
        if (offset == null) {
            return newKeys.get(key);
        }

        final Object o = objects[offset];
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
            if (!EMPTY_ARRAY.equals(objects)) {
                objects = objects.clone();
            }
        }
    }

    @Override
    public final V put(final K key, final V value) {
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
        final Object ret = objects[offset];
        objects[offset] = valueToObject(value);
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
        final Object ret = objects[offset];
        objects[offset] = NO_VALUE;
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
            Arrays.fill(objects, NO_VALUE);
            removed = objects.length;
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
            return new ImmutableOffsetMap<>(offsets, objects);
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
                    if (!NO_VALUE.equals(objects[e.getValue()])) {
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
                    final Object o = objects[e.getValue()];
                    if (!NO_VALUE.equals(o)) {
                        values[i++] = o;
                    }
                }
            }
        } else {
            System.arraycopy(objects, 0, values, 0, offsets.size());
            i = offsets.size();
        }
        for (V v : newKeys.values()) {
            values[i++] = valueToObject(v);
        }

        return new ImmutableOffsetMap<>(OffsetMapCache.offsetsFor(keyset), values);
    }

    @Override
    public MutableOffsetMap<K, V> clone() {
        return new MutableOffsetMap<K, V>(this);
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
            if (newKeys.isEmpty() && offsets == om.offsets() && Arrays.deepEquals(objects, om.objects())) {
                return true;
            }
        } else if (o instanceof MutableOffsetMap) {
            final MutableOffsetMap<?, ?> om = (MutableOffsetMap<?, ?>) o;
            if (offsets == om.offsets && Arrays.deepEquals(objects, om.objects) && newKeys.equals(om.newKeys)) {
                return true;
            }
        } else if (o instanceof Map) {
            final Map<?, ?> om = (Map<?, ?>)o;

            // Size and key sets have to match
            if (size() != om.size() || !keySet().equals(om.keySet())) {
                return false;
            }

            try {
                // Ensure all newKeys are present. Note newKeys is guaranteed to
                // not contain null value.
                for (Entry<K, V> e : newKeys.entrySet()) {
                    if (!e.getValue().equals(om.get(e.getKey()))) {
                        return false;
                    }
                }

                // Ensure all objects are present
                for (Entry<K, Integer> e : offsets.entrySet()) {
                    final Object obj = objects[e.getValue()];
                    if (!NO_VALUE.equals(obj)) {
                        final V v = objectToValue(e.getKey(), obj);
                        if (!v.equals(om.get(e.getKey()))) {
                            return false;
                        }
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
    public final Set<K> keySet() {
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
                if (!NO_VALUE.equals(objects[e.getValue()])) {
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
                objects[offset] = NO_VALUE;
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
