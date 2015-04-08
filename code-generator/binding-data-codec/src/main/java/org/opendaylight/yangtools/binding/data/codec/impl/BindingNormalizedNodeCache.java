package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

class BindingNormalizedNodeCache extends CacheLoader<DataObject, NormalizedNode<?, ?>> {

    private final LoadingCache<DataObject, NormalizedNode<?, ?>> cache = CacheBuilder.newBuilder().build(this);
    final DataContainerCodecContext<?,?> subtreeRoot;
    final CachingNormalizedNodeCodecImpl<?> cacheHolder;

    public BindingNormalizedNodeCache(final CachingNormalizedNodeCodecImpl<?> cacheHolder,
            final DataContainerCodecContext<?,?> subtreeRoot) {
        this.cacheHolder = cacheHolder;
        this.subtreeRoot = subtreeRoot;
    }

    @Override
    public NormalizedNode<?, ?> load(final DataObject key) throws Exception {
        return BindingNormalizedNodeBuilder.serialize(cacheHolder,subtreeRoot,key);
    }

    NormalizedNode<?,?> get(final DataObject obj)  {
        return cache.getUnchecked(obj);
    }
}
