/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.Set;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 *
 * Abstract Holder of Binding to Normalized Node caches indexed by {@link DataContainerCodecContext}
 * to which cache is associated.
 *
 */
abstract class AbstractBindingNormalizedNodeCacheHolder {

    private final Set<Class<? extends DataObject>> cachedValues;
    private final LoadingCache<DataContainerCodecContext<?, ?>, BindingNormalizedNodeCache> caches = CacheBuilder
            .newBuilder().build(new CacheLoader<DataContainerCodecContext<?, ?>, BindingNormalizedNodeCache>() {

                @Override
                public BindingNormalizedNodeCache load(final DataContainerCodecContext<?, ?> key) throws Exception {
                    return new BindingNormalizedNodeCache(AbstractBindingNormalizedNodeCacheHolder.this, key);
                }

            });

    protected AbstractBindingNormalizedNodeCacheHolder(final Set<Class<? extends DataObject>> cacheSpec) {
        cachedValues = Preconditions.checkNotNull(cacheSpec);
    }

    BindingNormalizedNodeCache getCachingSerializer(final DataContainerCodecContext<?, ?> childCtx) {
        if (isCached(childCtx.getBindingClass())) {
            return caches.getUnchecked(childCtx);
        }
        return null;
    }

    boolean isCached(final Class<?> type) {
        return cachedValues.contains(type);
    }
}
