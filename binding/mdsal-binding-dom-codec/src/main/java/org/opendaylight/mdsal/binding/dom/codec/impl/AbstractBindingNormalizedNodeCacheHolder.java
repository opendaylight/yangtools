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
import com.google.common.collect.ImmutableSet;
import org.opendaylight.yangtools.yang.binding.BindingObject;

/**
 * Abstract Holder of Binding to Normalized Node caches indexed by {@link DataContainerCodecContext} to which cache is
 * associated.
 */
abstract class AbstractBindingNormalizedNodeCacheHolder {
    private final LoadingCache<DataContainerCodecContext<?, ?>, DataObjectNormalizedNodeCache> caches = CacheBuilder
            .newBuilder().build(new CacheLoader<DataContainerCodecContext<?, ?>, DataObjectNormalizedNodeCache>() {
                @Override
                public DataObjectNormalizedNodeCache load(final DataContainerCodecContext<?, ?> key) {
                    return new DataObjectNormalizedNodeCache(AbstractBindingNormalizedNodeCacheHolder.this, key);
                }
            });
    private final ImmutableSet<Class<? extends BindingObject>> cacheSpec;

    AbstractBindingNormalizedNodeCacheHolder(final ImmutableSet<Class<? extends BindingObject>> cacheSpec) {
        this.cacheSpec = requireNonNull(cacheSpec);
    }

    DataObjectNormalizedNodeCache getCachingSerializer(final DataContainerCodecContext<?, ?> childCtx) {
        if (isCached(childCtx.getBindingClass())) {
            return caches.getUnchecked(childCtx);
        }
        return null;
    }

    final boolean isCached(final Class<? extends BindingObject> type) {
        return cacheSpec.contains(type);
    }
}
