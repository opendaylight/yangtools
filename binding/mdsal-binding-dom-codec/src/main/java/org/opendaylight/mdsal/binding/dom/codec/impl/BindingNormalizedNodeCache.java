/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

final class BindingNormalizedNodeCache extends CacheLoader<DataObject, NormalizedNode<?, ?>> {

    private final LoadingCache<DataObject, NormalizedNode<?, ?>> cache = CacheBuilder.newBuilder().weakValues()
            .build(this);
    final DataContainerCodecContext<?, ?> subtreeRoot;
    final AbstractBindingNormalizedNodeCacheHolder cacheHolder;

    BindingNormalizedNodeCache(final AbstractBindingNormalizedNodeCacheHolder cacheHolder,
            final DataContainerCodecContext<?, ?> subtreeRoot) {
        this.cacheHolder = requireNonNull(cacheHolder, "cacheHolder");
        this.subtreeRoot = requireNonNull(subtreeRoot, "subtreeRoot");
    }

    @Override
    public NormalizedNode<?, ?> load(final DataObject key) throws Exception {
        return CachingNormalizedNodeSerializer.serializeUsingStreamWriter(cacheHolder, subtreeRoot, key);
    }

    /**
     * Returns cached NormalizedNode representation of DataObject. If the representation is not cached, serializes
     * DataObject and updates cache with representation.
     *
     * @param obj Binding object to be deserialized
     * @return NormalizedNode representation of binding object.
     */
    NormalizedNode<?, ?> get(final DataObject obj) {
        return cache.getUnchecked(obj);
    }
}
