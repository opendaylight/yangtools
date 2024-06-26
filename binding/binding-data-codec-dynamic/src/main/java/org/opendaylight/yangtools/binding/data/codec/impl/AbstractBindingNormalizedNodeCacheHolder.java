/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import org.opendaylight.yangtools.binding.BindingObject;
import org.opendaylight.yangtools.binding.data.codec.api.BindingObjectCodecTreeNode;

/**
 * Abstract Holder of Binding to Normalized Node caches indexed by {@link DataContainerCodecContext} to which cache is
 * associated.
 */
abstract class AbstractBindingNormalizedNodeCacheHolder {
    @SuppressWarnings("rawtypes")
    private final LoadingCache<CodecContext, AbstractBindingNormalizedNodeCache> caches =
        CacheBuilder.newBuilder().build(new CacheLoader<>() {
            @Override
            public AbstractBindingNormalizedNodeCache load(final CodecContext key) {
                // FIXME: Use a switch expression once we have https://openjdk.org/jeps/441
                if (key instanceof DataContainerCodecContext<?, ?, ?> dataContainer) {
                    return new DataObjectNormalizedNodeCache(AbstractBindingNormalizedNodeCacheHolder.this,
                        dataContainer);
                }
                if (key instanceof LeafNodeCodecContext.OfTypeObject typeObject) {
                    return new TypeObjectNormalizedNodeCache<>(typeObject);
                }
                throw new IllegalStateException("Unhandled context " + key);
            }
        });

    private final ImmutableSet<Class<?>> cacheSpec;

    AbstractBindingNormalizedNodeCacheHolder(final ImmutableSet<Class<?>> cacheSpec) {
        this.cacheSpec = requireNonNull(cacheSpec);
    }

    @SuppressWarnings("unchecked")
    <T extends BindingObject, C extends CodecContext & BindingObjectCodecTreeNode>
            AbstractBindingNormalizedNodeCache<T, C> getCachingSerializer(final C childCtx) {
        return isCached(childCtx.getBindingClass()) ? caches.getUnchecked(childCtx) : null;
    }

    final boolean isCached(final Class<?> type) {
        return cacheSpec.contains(type);
    }
}
