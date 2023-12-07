/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ForwardingMap;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.pantheon.triemap.ImmutableTrieMap;
import tech.pantheon.triemap.MutableTrieMap;

/**
 * A read-only facade in front of a TrieMap. This is what we give out from
 * MapAdaptor.optimize(). The idea is that we want our read-only users to
 * share a single snapshot. That snapshot is instantiated lazily either on
 * first access. Since we never leak the TrieMap and track its size as it
 * changes, we can cache it for future reference.
 */
final class ReadOnlyTrieMap<K, V> extends ForwardingMap<K, V> {
    private static final Logger LOG = LoggerFactory.getLogger(ReadOnlyTrieMap.class);
    private static final VarHandle READ_ONLY;

    static {
        try {
            READ_ONLY = MethodHandles.lookup().findVarHandle(ReadOnlyTrieMap.class, "readOnly", ImmutableTrieMap.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final MutableTrieMap<K, V> readWrite;
    private final int size;

    // Used via the varhandle
    @SuppressWarnings("unused")
    @SuppressFBWarnings(value = "UUF_UNUSED_FIELD", justification = "https://github.com/spotbugs/spotbugs/issues/2749")
    private volatile ImmutableTrieMap<K, V> readOnly;

    ReadOnlyTrieMap(final MutableTrieMap<K, V> map, final int size) {
        this.readWrite = requireNonNull(map);
        this.size = size;
    }

    ReadWriteTrieMap<K, V> toReadWrite() {
        final ReadWriteTrieMap<K, V> ret = new ReadWriteTrieMap<>(readWrite.mutableSnapshot(), size);
        LOG.trace("Converted read-only TrieMap {} to read-write {}", this, ret);
        return ret;
    }

    @Override
    protected ImmutableTrieMap<K, V> delegate() {
        final ImmutableTrieMap<K, V> ret = (ImmutableTrieMap<K, V>) READ_ONLY.getAcquire(this);
        return ret != null ? ret : loadReadOnly();
    }

    @Override
    public int size() {
        return size;
    }

    @SuppressWarnings("unchecked")
    private ImmutableTrieMap<K, V> loadReadOnly() {
        final ImmutableTrieMap<K, V> ret = readWrite.immutableSnapshot();
        final Object witness = READ_ONLY.compareAndExchangeRelease(this, null, ret);
        return witness == null ? ret : (ImmutableTrieMap<K, V>) witness;
    }
}
