package org.opendaylight.yangtools.binding.data.codec.impl;

import java.util.Set;
import org.opendaylight.yangtools.binding.data.codec.api.BindingNormalizedNodeCachingCodec;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

class CachingNormalizedNodeCodecImpl<D extends DataObject> implements BindingNormalizedNodeCachingCodec<D>{

    private final Set<Class<? extends DataObject>> cachedValues;
    private final DataContainerCodecContext<D, ?> context;

    CachingNormalizedNodeCodecImpl(DataContainerCodecContext<D, ?> parent, Set<Class<? extends DataObject>> cacheSpec) {
        this.context = parent;
        this.cachedValues = cacheSpec;
    }

    @Override
    public D deserialize(NormalizedNode<?, ?> data) {
        return context.deserialize(data);
    }

    @Override
    public NormalizedNode<?, ?> serialize(D data) {
        // FIXME: Add real-class based serialization.
        return context.serialize(data);
    }

    @Override
    public void close() {
        // NOOP as of now.
    }

}
