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
import java.util.Map;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import org.opendaylight.yangtools.triemap.ImmutableTrieMap;
import org.opendaylight.yangtools.triemap.MutableTrieMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A read-only facade in front of a TrieMap. This is what we give out from
 * MapAdaptor.optimize(). The idea is that we want our read-only users to
 * share a single snapshot. That snapshot is instantiated lazily either on
 * first access. Since we never leak the TrieMap and track its size as it
 * changes, we can cache it for future reference.
 */
final class ReadOnlyTrieMap<K, V> extends ForwardingMap<K, V> {
    @SuppressWarnings("rawtypes")
    private static final AtomicReferenceFieldUpdater<ReadOnlyTrieMap, ImmutableTrieMap> UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(ReadOnlyTrieMap.class, ImmutableTrieMap.class, "readOnly");
    private static final Logger LOG = LoggerFactory.getLogger(ReadOnlyTrieMap.class);
    private final MutableTrieMap<K, V> readWrite;
    private final int size;
    private volatile ImmutableTrieMap<K, V> readOnly;

    ReadOnlyTrieMap(final MutableTrieMap<K, V> map, final int size) {
        super();
        this.readWrite = Preconditions.checkNotNull(map);
        this.size = size;
    }

    Map<K, V> toReadWrite() {
        final Map<K, V> ret = new ReadWriteTrieMap<>(readWrite.mutableSnapshot(), size);
        LOG.trace("Converted read-only TrieMap {} to read-write {}", this, ret);
        return ret;
    }

    @Override
    protected Map<K, V> delegate() {
        ImmutableTrieMap<K, V> ret = readOnly;
        if (ret == null) {
            ret = readWrite.immutableSnapshot();
            if (!UPDATER.compareAndSet(this, null, ret)) {
                ret = readOnly;
            }
        }
        return ret;
    }

    @Override
    public int size() {
        return size;
    }
}
