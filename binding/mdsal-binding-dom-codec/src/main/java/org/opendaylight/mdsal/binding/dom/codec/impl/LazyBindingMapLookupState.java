/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.mdsal.binding.dom.codec.impl.LazyBindingList.OBJ_AA;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Key;
import org.opendaylight.yangtools.yang.binding.KeyAware;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;

/**
 * {@link LazyBindingMap.State} optimized for lookup access, mainly via {@link Map#values()}. Note that this
 * implementation, while being effectively immutable, does not guarantee stable iteration order.
 *
 * @param <K> key type
 * @param <V> value type
 */
final class LazyBindingMapLookupState<K extends Key<V>, V extends DataObject & KeyAware<K>>
        extends LazyBindingMap.State<K, V> {
    private static final VarHandle VALUES;

    static {
        try {
            VALUES = MethodHandles.lookup().findVarHandle(LazyBindingMapLookupState.class, "values", Values.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    // Primary storage of transformed nodes
    private final ConcurrentHashMap<K, V> objects = new ConcurrentHashMap<>();
    private final LazyBindingMap<K, V> map;

    // Used via the varhandle above
    @SuppressWarnings("unused")
    @SuppressFBWarnings(value = "UUF_UNUSED_FIELD", justification = "https://github.com/spotbugs/spotbugs/issues/2749")
    private volatile Values<K, V> values;

    LazyBindingMapLookupState(final LazyBindingMap<K, V> map) {
        this.map = requireNonNull(map);
    }

    @Override
    boolean containsKey(final Object key) {
        // If we have the value, that is always accurate. Otherwise we have to attempt to load from DOM data and see
        // what the result is.
        return objects.containsKey(key) || loadKey(key) != null;
    }

    @Override
    V get(final Object key) {
        final V existing;
        // Perform a lookup first, otherwise try to load the key from DOM data
        return (existing = objects.get(key)) != null ? existing : loadKey(key);
    }

    @Override
    KeySet<K, V> keySet() {
        return new KeySet<>(values());
    }

    @Override
    Values<K, V> values() {
        final Values<K, V> ret;
        return (ret = (Values<K, V>) VALUES.getAcquire(this)) != null ? ret : loadValues();
    }

    @Override
    EntrySet<K, V> entrySet() {
        return new EntrySet<>(values());
    }

    private @Nullable V loadKey(final @NonNull Object key) {
        final Optional<V> opt = map.lookupValue(key);
        if (opt.isEmpty()) {
            return null;
        }

        // Here is a conundrum: which key do we use?
        //
        // We have the lookup key readily available, which means faster insert at the expense of having duplicates once
        // the looked-up object is accessed. On the other hand we could use ret.key(), which forces population of key
        // properties and the key itself.
        //
        // Opt for the provided key, as it is likely the user will end up looking at other properties of the returned
        // object.
        //
        // This leaves the option for lookupValue() to use the key to initialize object's cache fields, so that we end
        // up reflecting the same key instance as well as reuse key's components as object values. The case for that
        // needs to be justified, though, as it ends up doing more work upfront unrelated to what the caller is about
        // to do.
        return putIfAbsent((K) key, opt.orElseThrow());
    }

    private @NonNull V putIfAbsent(final @NonNull K key, final @NonNull V value) {
        final V existing = objects.putIfAbsent(key, value);
        return existing == null ? value : existing;
    }

    @SuppressWarnings("unchecked")
    private @NonNull Values<K, V> loadValues() {
        final Values<K, V> ret = new Values<>(this);
        final Object witness;
        return (witness = VALUES.compareAndExchangeRelease(this, null, ret)) == null ? ret : (Values<K, V>) witness;
    }

    private static final class EntrySet<K extends Key<V>, V extends DataObject & KeyAware<K>>
            extends AbstractSet<Entry<K, V>> implements Immutable {
        private final Values<K, V> values;

        EntrySet(final Values<K, V> values) {
            this.values = requireNonNull(values);
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return values.entryIterator();
        }

        @Override
        public int size() {
            return values.size();
        }

        @Override
        @SuppressWarnings("checkstyle:parameterName")
        public boolean contains(final Object o) {
            // Key/Value are related, asking whether we have a particular Entry is asking whether values contain that
            // the entry's value
            return values.contains(((Entry<?, ?>)o).getValue());
        }

        @Override
        @SuppressWarnings("checkstyle:equalsHashCode")
        public boolean equals(final Object obj) {
            // Fast check due to us not wasting a field for this value
            return obj instanceof EntrySet && values == ((EntrySet<?, ?>) obj).values || super.equals(obj);
        }
    }

    private static final class KeySet<K extends Key<V>, V extends DataObject & KeyAware<K>>
            extends AbstractSet<K> implements Immutable {
        private final Values<K, V> values;

        KeySet(final Values<K, V> values) {
            this.values = requireNonNull(values);
        }

        @Override
        public Iterator<K> iterator() {
            return values.keyIterator();
        }

        @Override
        public int size() {
            return values.size();
        }

        @Override
        @SuppressWarnings("checkstyle:parameterName")
        public boolean contains(final Object o) {
            return values.map().containsKey(o);
        }

        @Override
        @SuppressWarnings("checkstyle:equalsHashCode")
        public boolean equals(final Object obj) {
            // Fast check due to us not wasting a field for this value
            return obj instanceof KeySet && values == ((KeySet<?, ?>) obj).values || super.equals(obj);
        }
    }

    private static final class Values<K extends Key<V>, V extends DataObject & KeyAware<K>>
            extends AbstractSet<V> implements Immutable {
        private final LazyBindingMapLookupState<K, V> state;

        // Temporary storage for translated objects. We first initialize this to DOM values, so that we know what
        // objects we need to visit. As we iterate through them, we pick the source from here, then run them through
        // decode, then reconcile with the lookup map and finally store them back here.
        //
        // Once at least one iterator has gone through the entire entire array, we throw it away, as at that point
        // the primary storage is guaranteed to have all the same objects. We then free this array and switch to using
        // views on top of primary storage instead.
        //
        // This has the side-effect of changing iteration order once one of the iterators has made a complete pass.
        // While it may be counter-intuitive, we opt for the added memory efficiency and squirm just right to say
        // this is okay for Immutable contract :)
        @SuppressFBWarnings(value = "VO_VOLATILE_REFERENCE_TO_ARRAY",
                justification = "The ref should really be volatile. This class does not access elements directly.")
        private volatile Object[] objects;

        Values(final LazyBindingMapLookupState<K, V> state) {
            this.state = requireNonNull(state);
            objects = map().mapNode().body().toArray();
        }

        @Override
        public Iterator<V> iterator() {
            final Object[] local = objects;
            // When we have null objects it means we have everyone in state.objects
            return local == null ? Iterators.unmodifiableIterator(state.objects.values().iterator())
                    : new ValuesIter<>(this, local);
        }

        @Override
        @SuppressWarnings("checkstyle:parameterName")
        public boolean contains(final Object o) {
            return map().containsValue(o);
        }

        @Override
        public int size() {
            return map().size();
        }

        Iterator<Entry<K, V>> entryIterator() {
            final Object[] local = objects;
            // When we have null objects it means we have everyone in state.objects
            return local == null ? Iterators.unmodifiableIterator(state.objects.entrySet().iterator())
                    : Iterators.transform(new ValuesIter<>(this, local), value -> Map.entry(value.key(), value));
        }

        Iterator<K> keyIterator() {
            final Object[] local = objects;
            // When we have null objects it means we have everyone in state.objects
            return local == null ? Iterators.unmodifiableIterator(state.objects.keySet().iterator())
                    : Iterators.transform(new ValuesIter<>(this, local), KeyAware::key);
        }

        LazyBindingMap<K, V> map() {
            return state.map;
        }

        void fullyIterated() {
            // We have iterated over all objects. Clear them to indicate further access should go through state.objects
            objects = null;
        }
    }

    private static final class ValuesIter<K extends Key<V>, V extends DataObject & KeyAware<K>>
            extends AbstractIterator<V> {
        private final Values<K, V> values;
        private final Object[] objects;
        private int nextOffset;

        ValuesIter(final Values<K, V> values, final Object[] objects) {
            this.values = requireNonNull(values);
            this.objects = requireNonNull(objects);
        }

        @Override
        protected V computeNext() {
            if (nextOffset < objects.length) {
                return objectAt(nextOffset++);
            }
            values.fullyIterated();
            return endOfData();
        }

        private @NonNull V objectAt(final int offset) {
            final Object obj = OBJ_AA.getAcquire(objects, offset);
            return obj instanceof MapEntryNode ? loadObjectAt(offset, (MapEntryNode) obj) : (V) obj;
        }

        private @NonNull V loadObjectAt(final int offset, final MapEntryNode obj) {
            final LazyBindingMapLookupState<K, V> local = values.state;
            final V created = local.map.createValue(obj);
            final V ret = local.putIfAbsent(created.key(), created);
            final Object witness;
            return (witness = OBJ_AA.compareAndExchangeRelease(objects, offset, obj, ret)) == obj ? ret : (V) witness;
        }
    }
}
