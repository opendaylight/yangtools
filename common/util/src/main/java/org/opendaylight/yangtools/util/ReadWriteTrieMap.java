/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import com.google.common.base.Preconditions;
import com.romix.scala.collection.concurrent.TrieMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A TrieMap facade tracking modifications. Since we change structures based on
 * their size, and determining the size of a TrieMap is expensive, we make sure
 * to update it as we go.
 *
 * <p>FIXME: this map does not support modification view the keySet()/values()/entrySet()
 *        methods.
 *
 * @param <K> Key type
 * @param <V> Value type
 */
final class ReadWriteTrieMap<K, V> implements Map<K, V> {
    private static final Logger LOG = LoggerFactory.getLogger(ReadOnlyTrieMap.class);
    private final TrieMap<K, V> delegate;
    private int size;

    ReadWriteTrieMap() {
        this.delegate = new TrieMap<>();
        this.size = 0;
    }

    ReadWriteTrieMap(final TrieMap<K, V> delegate, final int size) {
        this.delegate = Preconditions.checkNotNull(delegate);
        this.size = size;
    }

    Map<K, V> toReadOnly() {
        final Map<K, V> ret = new ReadOnlyTrieMap<>(delegate, size);
        LOG.trace("Converted read-write TrieMap {} to read-only {}", this, ret);
        return ret;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean containsKey(final Object key) {
        return delegate.containsKey(key);
    }

    @Override
    public boolean containsValue(final Object value) {
        return delegate.containsValue(value);
    }

    @Override
    public V get(final Object key) {
        return delegate.get(key);
    }

    @Override
    public V put(final K key, final V value) {
        final V ret = delegate.put(key, value);
        if (ret == null) {
            size++;
        }
        return ret;
    }

    @Override
    public V remove(final Object key) {
        final V ret = delegate.remove(key);
        if (ret != null) {
            size--;
        }
        return ret;
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public void putAll(@Nonnull final Map<? extends K, ? extends V> m) {
        for (Entry<? extends K, ? extends V> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    @Override
    public void clear() {
        delegate.clear();
        size = 0;
    }

    @Override
    public Set<K> keySet() {
        return Collections.unmodifiableSet(delegate.keySet());
    }

    @Override
    public Collection<V> values() {
        return Collections.unmodifiableCollection(delegate.values());
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return Collections.unmodifiableSet(delegate.entrySet());
    }

    @Override
    public boolean equals(final Object obj) {
        return delegate.equals(obj);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }
}
