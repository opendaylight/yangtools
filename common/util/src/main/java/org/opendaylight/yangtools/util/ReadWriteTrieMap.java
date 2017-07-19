/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.ForwardingMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.triemap.MutableTrieMap;
import org.opendaylight.yangtools.triemap.TrieMap;
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
final class ReadWriteTrieMap<K, V> extends ForwardingMap<K, V> {
    private static final Logger LOG = LoggerFactory.getLogger(ReadOnlyTrieMap.class);
    private final MutableTrieMap<K, V> delegate;
    private int size;

    ReadWriteTrieMap() {
        this.delegate = TrieMap.create();
        this.size = 0;
    }

    ReadWriteTrieMap(final MutableTrieMap<K, V> delegate, final int size) {
        this.delegate = Preconditions.checkNotNull(delegate);
        this.size = size;
    }

    @Override
    protected Map<K, V> delegate() {
        return delegate();
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
}
