/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.Set;
import org.opendaylight.yangtools.binding.data.codec.api.BindingNormalizedNodeCachingCodec;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

class CachingNormalizedNodeCodecImpl<D extends DataObject> implements BindingNormalizedNodeCachingCodec<D>{

    private final Set<Class<? extends DataObject>> cachedValues;
    private final DataContainerCodecContext<D, ?> context;
    private final LoadingCache<DataContainerCodecContext<?, ?>, BindingNormalizedNodeCache> caches = CacheBuilder
            .newBuilder().build(new CacheLoader<DataContainerCodecContext<?, ?>, BindingNormalizedNodeCache>() {

                @Override
                public BindingNormalizedNodeCache load(final DataContainerCodecContext<?, ?> key) throws Exception {
                    return new BindingNormalizedNodeCache(CachingNormalizedNodeCodecImpl.this, key);
                }

            });

    CachingNormalizedNodeCodecImpl(final DataContainerCodecContext<D, ?> subtreeRoot, final Set<Class<? extends DataObject>> cacheSpec) {
        this.context = Preconditions.checkNotNull(subtreeRoot);
        this.cachedValues = Preconditions.checkNotNull(cacheSpec);
    }

    @Override
    public D deserialize(final NormalizedNode<?, ?> data) {
        return context.deserialize(data);
    }

    @Override
    public NormalizedNode<?, ?> serialize(final D data) {
        return BindingNormalizedNodeBuilder.serialize(this, context, data);
    }

    @Override
    public void close() {
        // NOOP as of now.
    }

    boolean isCached(final Class<? extends DataObject> type) {
        return cachedValues.contains(type);
    }

    BindingNormalizedNodeCache getCachingSerializer(final DataContainerCodecContext<?, ?> childCtx) {
        return caches.getUnchecked(childCtx);
    }

}
