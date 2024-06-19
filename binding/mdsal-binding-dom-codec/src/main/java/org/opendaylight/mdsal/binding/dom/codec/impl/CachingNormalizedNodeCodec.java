/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableSet;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeCachingCodec;
import org.opendaylight.yangtools.yang.binding.BindingObject;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

class CachingNormalizedNodeCodec<D extends DataObject> extends AbstractBindingNormalizedNodeCacheHolder implements
        BindingNormalizedNodeCachingCodec<D> {
    private final DataContainerCodecContext<D, ?> context;

    CachingNormalizedNodeCodec(final DataContainerCodecContext<D, ?> subtreeRoot,
            final ImmutableSet<Class<? extends BindingObject>> cacheSpec) {
        super(cacheSpec);
        this.context = requireNonNull(subtreeRoot);
    }

    @Override
    public D deserialize(final NormalizedNode<?, ?> data) {
        return context.deserialize(data);
    }

    @Override
    public NormalizedNode<?, ?> serialize(final D data) {
        // Serialize data using stream writer with child cache enable or using the cache if it is available
        final DataObjectNormalizedNodeCache cache = getCachingSerializer(context);
        return cache == null ? CachingNormalizedNodeSerializer.serializeUsingStreamWriter(this, context, data)
                : cache.get(data);
    }

    @Override
    public void close() {
        // NOOP as of now.
    }
}
