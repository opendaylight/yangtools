/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

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
public abstract class MutableOffsetMap<K, V> extends AbstractMap<K, V> implements Cloneable, ModifiableMapPhase<K, V> {
    static final class Ordered<K, V> extends MutableOffsetMap<K, V> {
        Ordered() {
        }

        Ordered(final Map<K, V> source) {
            super(OffsetMapCache.orderedOffsets(source.keySet()), source);
        }

        Ordered(final ImmutableMap<K, Integer> offsets, final V[] objects) {
            super(offsets, objects);
        }

        @Override
        Object removedObject() {
            return REMOVED;
        }

        @Override
        UnmodifiableMapPhase<K, V> modifiedMap(final List<K> keys, final V[] values) {
            return new ImmutableOffsetMap.Ordered<>(OffsetMapCache.orderedOffsets(keys), values);
        }

        @Override
        UnmodifiableMapPhase<K, V> unmodifiedMap(final ImmutableMap<K, Integer> offsetMap, final V[] values) {
            return new ImmutableOffsetMap.Ordered<>(offsetMap, values);
        }

        @Override
        SharedSingletonMap<K, V> singletonMap() {
            return SharedSingletonMap.orderedCopyOf(this);
        }

        @Override
        HashMap<K, V> createNewKeys() {
            return new LinkedHashMap<>();
        }
    }

    static final class Unordered<K, V> extends MutableOffsetMap<K, V> {
        Unordered() {
        }

        Unordered(final Map<K, V> source) {
            super(OffsetMapCache.unorderedOffsets(source.keySet()), source);
        }

        Unordered(final ImmutableMap<K, Integer> offsets, final V[] objects) {
            super(offsets, objects);
        }

        @Override
        Object removedObject() {
            return null;
        }

        @Override
        UnmodifiableMapPhase<K, V> modifiedMap(final List<K> keys, final V[] values) {
            final var offsets = OffsetMapCache.unorderedOffsets(keys);
            return new ImmutableOffsetMap.Unordered<>(offsets, OffsetMapCache.adjustedArray(offsets, keys, values));
        }

        @Override
        UnmodifiableMapPhase<K, V> unmodifiedMap(final ImmutableMap<K, Integer> offsetMap, final V[] values) {
            return new ImmutableOffsetMap.Unordered<>(offsetMap, values);
        }

        @Override
        SharedSingletonMap<K, V> singletonMap() {
            return SharedSingletonMap.unorderedCopyOf(this);
        }

        @Override
        HashMap<K, V> createNewKeys() {
            return new HashMap<>();
        }
    }

    private static final Object[] EMPTY_ARRAY = new Object[0];
    private static final Object REMOVED = new Object();

    private final ImmutableMap<K, Integer> offsets;
    private HashMap<K, V> newKeys;
    private Object[] objects;
    private int removed = 0;

    // Fail-fast iterator guard, see java.util.ArrayList for reference.
    @SuppressFBWarnings("VO_VOLATILE_INCREMENT")
    private transient volatile int modCount;
    private boolean needClone = true;

    MutableOffsetMap(final ImmutableMap<K, Integer> offsets, final Object[] objects) {
        this.offsets = requireNonNull(offsets);
        this.objects = requireNonNull(objects);
    }

    MutableOffsetMap() {
        this(ImmutableMap.of(), EMPTY_ARRAY);
    }

    MutableOffsetMap(final ImmutableMap<K, Integer> offsets, final Map<K, V> source) {
        this(offsets, new Object[offsets.size()]);

        for (var entry : source.entrySet()) {
            objects[verifyNotNull(offsets.get(entry.getKey()))] = requireNonNull(entry.getValue());
        }

        needClone = false;
    }

    /**
     * Create a {@link MutableOffsetMap} of the specified map, retaining its iteration order.
     *
     * @param map input map
     * @return MutableOffsetMap with the same iteration order
     * @throws NullPointerException if {@code map} is null
     */
    public static <K, V> @NonNull MutableOffsetMap<K, V> orderedCopyOf(final Map<K, V> map) {
        return switch (map) {
            case Ordered<K, V> ordered -> ordered.clone();
            case ImmutableOffsetMap<K, V> iom -> new Ordered<>(iom.offsets(), iom.objects());
            default -> new Ordered<>(map);
        };
    }

    /**
     * Create a {@link MutableOffsetMap} of the specified map, potentially with a different iteration order.
     *
     * @param map input map
     * @return MutableOffsetMap with undefined iteration order
     * @throws NullPointerException if {@code map} is null
     */
    public static <K, V> @NonNull MutableOffsetMap<K, V> unorderedCopyOf(final Map<K, V> map) {
        return switch (map) {
            case Unordered<K, V> unordered -> unordered.clone();
            case ImmutableOffsetMap<K, V> iom -> new Unordered<>(iom.offsets(), iom.objects());
            default -> new Unordered<>(map);
        };
    }

    /**
     * Create an empty {@link MutableOffsetMap} which has an iteration order matching the insertion order.
     *
     * @return MutableOffsetMap which preserves insertion order
     */
    public static <K, V> @NonNull MutableOffsetMap<K, V> ordered() {
        return new MutableOffsetMap.Ordered<>();
    }

    /**
     * Create an empty {@link MutableOffsetMap} which has unspecified iteration order.
     *
     * @return An MutableOffsetMap
     */
    public static <K, V> @NonNull MutableOffsetMap<K, V> unordered() {
        return new MutableOffsetMap.Unordered<>();
    }

    abstract Object removedObject();

    abstract UnmodifiableMapPhase<K, V> modifiedMap(List<K> keys, V[] values);

    abstract UnmodifiableMapPhase<K, V> unmodifiedMap(ImmutableMap<K, Integer> offsetMap, V[] values);

    abstract SharedSingletonMap<K, V> singletonMap();

    @Override
    public final int size() {
        return offsets.size() - removed + (newKeys == null ? 0 : newKeys.size());
    }

    @Override
    public final boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public final boolean containsKey(final Object key) {
        final var offset = offsets.get(key);
        if (offset != null) {
            final var obj = objects[offset];
            if (!REMOVED.equals(obj)) {
                return obj != null;
            }
        }

        return newKeys != null && newKeys.containsKey(key);
    }

    @Override
    public final V get(final Object key) {
        final var offset = offsets.get(key);
        if (offset != null) {
            final var obj = objects[offset];

            /*
             * This is a bit tricky:  Ordered will put REMOVED to removed objects to retain strict insertion order.
             * Unordered will add null, indicating that the slot may be reused in future. Hence if we see a REMOVED
             * marker, we need to fall back to checking with new keys.
             */
            if (!REMOVED.equals(obj)) {
                @SuppressWarnings("unchecked")
                final var ret = (V)obj;
                return ret;
            }
        }

        return newKeys == null ? null : newKeys.get(key);
    }

    private void cloneArray() {
        if (needClone) {
            needClone = false;
            if (objects.length != 0) {
                objects = objects.clone();
            }
        }
    }

    @Override
    public final V put(final K key, final V value) {
        requireNonNull(value);
        final var offset = offsets.get(requireNonNull(key));
        if (offset != null) {
            final var obj = objects[offset];

            /*
             * Put which can potentially replace something in objects. Replacing an object does not cause iterators
             * to be invalidated and does follow insertion order (since it is not a fresh insert). If the object has
             * been removed, we fall back to newKeys.
             */
            if (!REMOVED.equals(obj)) {
                @SuppressWarnings("unchecked")
                final var ret = (V)obj;

                cloneArray();
                objects[offset] = value;
                if (ret == null) {
                    modCount++;
                    removed--;
                }

                return ret;
            }
        }

        if (newKeys == null) {
            newKeys = createNewKeys();
        }
        final V ret = newKeys.put(key, value);
        if (ret == null) {
            modCount++;
        }
        return ret;
    }

    @Override
    public final V remove(final Object key) {
        final var offset = offsets.get(key);
        if (offset != null) {
            final var obj = objects[offset];

            /*
             * A previous remove() may have indicated that the objects slot cannot be reused. In that case we need
             * to fall back to checking with newKeys.
             */
            if (!REMOVED.equals(obj)) {
                cloneArray();

                @SuppressWarnings("unchecked")
                final var ret = (V)obj;
                objects[offset] = removedObject();
                if (ret != null) {
                    modCount++;
                    removed++;
                }
                return ret;
            }
        }

        if (newKeys == null) {
            return null;
        }
        final var ret = newKeys.remove(key);
        if (ret != null) {
            modCount++;
        }
        return ret;
    }

    @Override
    public final void clear() {
        if (size() != 0) {
            if (newKeys != null) {
                newKeys.clear();
            }
            cloneArray();
            Arrays.fill(objects, removedObject());
            removed = objects.length;
            modCount++;
        }
    }

    @Override
    public final @NonNull Set<Entry<K, V>> entrySet() {
        return new EntrySet();
    }

    @Override
    public @NonNull Map<K, V> toUnmodifiableMap() {
        if (removed == 0 && noNewKeys()) {
            // Make sure next modification clones the array, as we leak it to the map we return.
            needClone = true;

            // We have ended up with no removed objects, hence this cast is safe
            @SuppressWarnings("unchecked")
            final var values = (V[])objects;

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
        final var keyset = new ArrayList<K>(s);
        if (removed != 0) {
            if (removed != offsets.size()) {
                for (var entry : offsets.entrySet()) {
                    final Object obj = objects[entry.getValue()];
                    if (obj != null && !REMOVED.equals(obj)) {
                        keyset.add(entry.getKey());
                    }
                }
            }
        } else {
            keyset.addAll(offsets.keySet());
        }
        if (newKeys != null) {
            keyset.addAll(newKeys.keySet());
        }

        // Construct the values
        @SuppressWarnings("unchecked")
        final var values = (V[])new Object[keyset.size()];
        int offset = 0;
        if (removed != 0) {
            if (removed != offsets.size()) {
                for (var entry : offsets.entrySet()) {
                    final var obj = objects[entry.getValue()];
                    if (obj != null && !REMOVED.equals(obj)) {
                        @SuppressWarnings("unchecked")
                        final var v = (V) obj;
                        values[offset++] = v;
                    }
                }
            }
        } else {
            System.arraycopy(objects, 0, values, 0, offsets.size());
            offset = offsets.size();
        }
        if (newKeys != null) {
            for (var v : newKeys.values()) {
                values[offset++] = v;
            }
        }

        return modifiedMap(keyset, values);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull MutableOffsetMap<K, V> clone() {
        final MutableOffsetMap<K, V> ret;
        try {
            ret = (MutableOffsetMap<K, V>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("Clone is expected to work", e);
        }

        ret.newKeys = newKeys == null ? null : (HashMap<K, V>) newKeys.clone();
        ret.needClone = true;
        return ret;
    }

    @Override
    public final int hashCode() {
        int result = 0;

        for (var entry : offsets.entrySet()) {
            final Object v = objects[entry.getValue()];
            if (v != null) {
                result += entry.getKey().hashCode() ^ v.hashCode();
            }
        }

        return newKeys != null ? result + newKeys.hashCode() : result;
    }

    @Override
    public final boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Map<?, ?> other)) {
            return false;
        }

        if (other instanceof ImmutableOffsetMap<?, ?> om) {
            if (noNewKeys() && offsets.equals(om.offsets())) {
                return Arrays.deepEquals(objects, om.objects());
            }
        } else if (other instanceof MutableOffsetMap<?, ?> om && offsets.equals(om.offsets)) {
            return Arrays.deepEquals(objects, om.objects) && equalNewKeys(om);
        }

        // Fall back to brute map compare
        return mapEquals((Map<?, ?>)obj);
    }

    private boolean equalNewKeys(final MutableOffsetMap<?, ?> other) {
        return noNewKeys() ? other.noNewKeys() : newKeys.equals(other.newKeys());
    }

    private boolean mapEquals(final Map<?, ?> other) {
        // Size and key sets have to match
        if (size() != other.size() || !keySet().equals(other.keySet())) {
            return false;
        }

        try {
            if (newKeys != null) {
                // Ensure all newKeys are present. Note newKeys is guaranteed to not contain a null value.
                for (var entry : newKeys.entrySet()) {
                    if (!entry.getValue().equals(other.get(entry.getKey()))) {
                        return false;
                    }
                }
            }

            // Ensure all objects are present
            for (var entry : offsets.entrySet()) {
                final var val = objects[entry.getValue()];
                if (val != null && !REMOVED.equals(val) && !val.equals(other.get(entry.getKey()))) {
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
    public final @NonNull Set<K> keySet() {
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
        return newKeys != null ? newKeys : ImmutableMap.of();
    }

    abstract HashMap<K, V> createNewKeys();

    private boolean noNewKeys() {
        return newKeys == null || newKeys.isEmpty();
    }

    private final class EntrySet extends AbstractSet<Entry<K, V>> {
        @Override
        public @NonNull Iterator<Entry<K, V>> iterator() {
            return new AbstractSetIterator<>() {
                @Override
                public Entry<K, V> next() {
                    final var key = nextKey();
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
            final var e = (Entry<K, V>) o;
            if (e.getValue() == null) {
                return false;
            }

            return e.getValue().equals(MutableOffsetMap.this.get(e.getKey()));
        }

        @Override
        @SuppressWarnings("checkstyle:parameterName")
        public boolean add(final Entry<K, V> e) {
            final var v = requireNonNull(e.getValue());
            final var p = MutableOffsetMap.this.put(e.getKey(), v);
            return !v.equals(p);
        }

        @Override
        @SuppressWarnings("checkstyle:parameterName")
        public boolean remove(final Object o) {
            if (!(o instanceof Entry)) {
                return false;
            }

            @SuppressWarnings("unchecked")
            final var e = (Entry<K,V>) o;
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
        public @NonNull Iterator<K> iterator() {
            return new AbstractSetIterator<>() {
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
        private final Iterator<K> newIterator = newKeys == null ? Collections.emptyIterator()
                : newKeys.keySet().iterator();
        private int expectedModCount = modCount;
        private @Nullable K currentKey = null;
        private @Nullable K nextKey;

        AbstractSetIterator() {
            updateNextKey();
        }

        private void updateNextKey() {
            while (oldIterator.hasNext()) {
                final var entry = oldIterator.next();
                final var obj = objects[entry.getValue()];
                if (obj != null && !REMOVED.equals(obj)) {
                    nextKey = entry.getKey();
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
            checkModCount();
            checkState(currentKey != null);
            final var offset = offsets.get(currentKey);
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
