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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Key;
import org.opendaylight.yangtools.yang.binding.KeyAware;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link LazyBindingMap.State} optimized for iterator access, mainly via {@link Map#values()}.
 *
 * @param <K> key type
 * @param <V> value type
 */
final class LazyBindingMapIterState<K extends Key<V>, V extends DataObject & KeyAware<K>>
        extends LazyBindingMap.State<K, V> {
    private static final Logger LOG = LoggerFactory.getLogger(LazyBindingMapIterState.class);
    private static final VarHandle ENTRY_SET;
    private static final VarHandle KEY_SET;
    private static final VarHandle LOOKUP_MAP;

    static {
        final Lookup lookup = MethodHandles.lookup();
        try {
            ENTRY_SET = lookup.findVarHandle(LazyBindingMapIterState.class, "entrySet", EntrySet.class);
            KEY_SET = lookup.findVarHandle(LazyBindingMapIterState.class, "keySet", KeySet.class);
            LOOKUP_MAP = lookup.findVarHandle(LazyBindingMapIterState.class, "lookupMap", ImmutableMap.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    // Primary storage of transformed nodes. Other views are derived from this.
    private final @NonNull Values<K, V> values;

    // Secondary views derived from values, used via varhandles above
    @SuppressWarnings("unused")
    @SuppressFBWarnings(value = "UUF_UNUSED_FIELD", justification = "https://github.com/spotbugs/spotbugs/issues/2749")
    private volatile KeySet<K, V> keySet;
    @SuppressWarnings("unused")
    @SuppressFBWarnings(value = "UUF_UNUSED_FIELD", justification = "https://github.com/spotbugs/spotbugs/issues/2749")
    private volatile EntrySet<K, V> entrySet;

    // Lookup map, instantiated on demand, used via varhandle above
    @SuppressWarnings("unused")
    @SuppressFBWarnings(value = "URF_UNREAD_FIELD", justification = "https://github.com/spotbugs/spotbugs/issues/2749")
    private volatile ImmutableMap<K, V> lookupMap;

    LazyBindingMapIterState(final LazyBindingMap<K, V> map) {
        values = new Values<>(map);
    }

    @Override
    boolean containsKey(final Object key) {
        return lookupMap().containsKey(key);
    }

    @Override
    V get(final Object key) {
        return lookupMap().get(key);
    }

    @Override
    Values<K, V> values() {
        return values;
    }

    @Override
    EntrySet<K, V> entrySet() {
        final EntrySet<K, V> ret;
        return (ret = (EntrySet<K, V>) ENTRY_SET.getAcquire(this)) != null ? ret : loadEntrySet();
    }

    @Override
    KeySet<K, V> keySet() {
        final KeySet<K, V> ret;
        return (ret = (KeySet<K, V>) KEY_SET.getAcquire(this)) != null ? ret : loadKeySet();
    }

    private @NonNull ImmutableMap<K, V> lookupMap() {
        final ImmutableMap<K, V> ret;
        return (ret = (ImmutableMap<K, V>) LOOKUP_MAP.getAcquire(this)) != null ? ret : loadLookupMap();
    }

    // TODO: this is not exactly efficient, as we are forcing full materialization. We also take a lock here, as we
    //       do not want multiple threads indexing the same thing. Perhaps this should work with the LookupState
    //       somehow, so that we have a shared object view and two indices?
    private synchronized @NonNull ImmutableMap<K, V> loadLookupMap() {
        ImmutableMap<K, V> ret = (ImmutableMap<K, V>) LOOKUP_MAP.getAcquire(this);
        if (ret == null) {
            lookupMap = ret = ImmutableMap.copyOf(entrySet());
            if (LOG.isTraceEnabled()) {
                LOG.trace("Inefficient instantiation of lookup secondary", new Throwable());
            }
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    private @NonNull EntrySet<K, V> loadEntrySet() {
        final EntrySet<K, V> ret = new EntrySet<>(values);
        final Object witness;
        return (witness = ENTRY_SET.compareAndExchangeRelease(this, null, ret)) == null ? ret
                : (EntrySet<K, V>) witness;
    }

    @SuppressWarnings("unchecked")
    private @NonNull KeySet<K, V> loadKeySet() {
        final KeySet<K, V> ret = new KeySet<>(values);
        final Object witness;
        return (witness = KEY_SET.compareAndExchangeRelease(this, null, ret)) == null ? ret : (KeySet<K, V>) witness;
    }

    private static final class EntrySet<K extends Key<V>, V extends DataObject & KeyAware<K>>
            extends AbstractSet<Entry<K, V>> implements Immutable {
        private final Values<K, V> values;

        EntrySet(final Values<K, V> values) {
            this.values = requireNonNull(values);
        }

        @Override
        public int size() {
            return values.size();
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return Iterators.transform(values.iterator(), value -> Map.entry(value.key(), value));
        }

        @Override
        @SuppressWarnings("checkstyle:parameterName")
        public boolean contains(final Object o) {
            // Key/Value are related, asking whether we have a particular Entry is asking whether values contain that
            // the entry's value
            return values.contains(((Entry<?, ?>)o).getValue());
        }
    }

    private static final class KeySet<K extends Key<V>, V extends DataObject & KeyAware<K>>
            extends AbstractSet<K> implements Immutable {
        private final Values<K, V> values;

        KeySet(final Values<K, V> values) {
            this.values = requireNonNull(values);
        }

        @Override
        public int size() {
            return values.size();
        }

        @Override
        public Iterator<K> iterator() {
            return Iterators.transform(values.iterator(), KeyAware::key);
        }

        @Override
        @SuppressWarnings("checkstyle:parameterName")
        public boolean contains(final Object o) {
            return values.map.containsKey(o);
        }
    }

    /*
     * Lazily-populated translation of DOM values to binding values. This class is not completely lazy, as we allocate
     * the array to hold all values upfront and populate it with MapEntry nodes. That allows us to perform lock-free
     * access, as we just end up CASing MapEntryNodes with their Binding replacements.
     */
    private static final class Values<K extends Key<V>, V extends DataObject & KeyAware<K>>
            extends AbstractSet<V> implements Immutable {
        private final LazyBindingMap<K, V> map;
        private final Object[] objects;

        Values(final LazyBindingMap<K, V> map) {
            this.map = requireNonNull(map);
            objects = map.mapNode().body().toArray();
        }

        @Override
        public Iterator<V> iterator() {
            return new AbstractIterator<>() {
                private int nextOffset;

                @Override
                protected V computeNext() {
                    return nextOffset < objects.length ? objectAt(nextOffset++) : endOfData();
                }
            };
        }

        @Override
        @SuppressWarnings("checkstyle:parameterName")
        public boolean contains(final Object o) {
            return map.containsValue(o);
        }

        @Override
        public int size() {
            return map.size();
        }

        @NonNull V objectAt(final int offset) {
            final Object obj = OBJ_AA.getAcquire(objects, offset);
            return obj instanceof MapEntryNode ? loadObjectAt(offset, (MapEntryNode) obj) : (V) obj;
        }

        private @NonNull V loadObjectAt(final int offset, final MapEntryNode obj) {
            final V ret = map.createValue(obj);
            final Object witness;
            return (witness = OBJ_AA.compareAndExchangeRelease(objects, offset, obj, ret)) == obj ? ret : (V) witness;
        }
    }
}
