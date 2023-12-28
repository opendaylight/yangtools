/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.node;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;

/**
 * Internal equivalent of {@link Collections}' unmodifiable Map. It does not retain
 * keySet/entrySet references, thus lowering the memory overhead.
 */
final class UnmodifiableChildrenMap implements CloneableMap<PathArgument, DataContainerChild>, Serializable {
    private static final long serialVersionUID = 1L;

    /*
     * Do not wrap maps which are smaller than this and instead copy them into an ImmutableMap.
     */
    private static final int WRAP_THRESHOLD = 9;

    @SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "Delegate is expected to be Serializable")
    private final Map<PathArgument, DataContainerChild> delegate;

    private transient Collection<DataContainerChild> values = null;

    private UnmodifiableChildrenMap(final Map<PathArgument, DataContainerChild> delegate) {
        this.delegate = requireNonNull(delegate);
    }

    /**
     * Create an unmodifiable view of a particular map. Does not perform unnecessary
     * encapsulation if the map is known to be already unmodifiable.
     *
     * @param map Backing map
     * @return Unmodifiable view
     */
    static Map<PathArgument, DataContainerChild> create(final Map<PathArgument, DataContainerChild> map) {
        if (map instanceof UnmodifiableChildrenMap || map instanceof ImmutableMap) {
            return map;
        }
        if (map.isEmpty()) {
            return ImmutableMap.of();
        }
        if (map.size() < WRAP_THRESHOLD) {
            return ImmutableMap.copyOf(map);
        }
        return new UnmodifiableChildrenMap(map);
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
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
    public DataContainerChild get(final Object key) {
        return delegate.get(key);
    }

    @Override
    public DataContainerChild put(final PathArgument key, final DataContainerChild value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DataContainerChild remove(final Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public void putAll(final Map<? extends PathArgument, ? extends DataContainerChild> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<PathArgument> keySet() {
        return Collections.unmodifiableSet(delegate.keySet());
    }

    @Override
    public Collection<DataContainerChild> values() {
        if (values == null) {
            values = Collections.unmodifiableCollection(delegate.values());
        }
        return values;
    }

    @Override
    public Set<Entry<PathArgument, DataContainerChild>> entrySet() {
        /*
         * Okay, this is not as efficient as it could be -- we could save ourselves the
         * map instantiation. The cost of that would be re-implementation of a read-only
         * Map.Entry to ensure our delegate is never modified.
         *
         * Let's skip that and use whatever the JRE gives us instead.
         */
        return Collections.unmodifiableMap(delegate).entrySet();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || delegate.equals(obj);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<PathArgument, DataContainerChild> createMutableClone() {
        if (delegate instanceof HashMap<?, ?> hashMap) {
            return (Map<PathArgument, DataContainerChild>) hashMap.clone();
        }
        return new HashMap<>(delegate);
    }
}
