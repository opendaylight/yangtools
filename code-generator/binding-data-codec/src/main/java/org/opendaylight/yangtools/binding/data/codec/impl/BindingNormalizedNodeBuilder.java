package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.base.Throwables;
import java.io.IOException;
import org.opendaylight.yangtools.yang.binding.BindingSerializer;
import org.opendaylight.yangtools.yang.binding.BindingStreamEventWriter;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializer;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;

class BindingNormalizedNodeBuilder extends ForwardingBindingStreamEventWriter implements BindingSerializer<Object, DataObject> {

    private final NormalizedNodeResult domResult;
    private final NormalizedNodeWriterWithAddChild domWriter;
    private final BindingToNormalizedStreamWriter delegate;
    private final CachingNormalizedNodeCodecImpl<?> cacheHolder;

    BindingNormalizedNodeBuilder(final CachingNormalizedNodeCodecImpl<?> cacheHolder, final DataContainerCodecContext<?,?> subtreeRoot) {
        this.cacheHolder = cacheHolder;
        this.domResult = new NormalizedNodeResult();
        this.domWriter = new NormalizedNodeWriterWithAddChild(domResult);
        this.delegate = new BindingToNormalizedStreamWriter(subtreeRoot, domWriter);
    }

    @Override
    protected BindingStreamEventWriter delegate() {
        return delegate;
    }

    NormalizedNode<?,?> build() {

        return domResult.getResult();
    }

    /**
     * Note that this optional is serialization of child node invoked from
     * {@link DataObjectSerializer}.
     */
    @Override
    public NormalizedNode<?,?> serialize(final DataObject input) {
        final BindingNormalizedNodeCache cachingSerializer = getCacheSerializer(input.getImplementedInterface());
        if(cachingSerializer != null) {
            final NormalizedNode<?,?> domData = cachingSerializer.get(input);
            domWriter.addChild(domData);
            return domData;
        }
        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private BindingNormalizedNodeCache getCacheSerializer(final Class type) {
        if(cacheHolder.isCached(type)) {
            final DataContainerCodecContext<?, ?> currentCtx = (DataContainerCodecContext<?, ?>)delegate.current();
            if(type.equals(currentCtx.getBindingClass())) {
                return cacheHolder.getCachingSerializer(currentCtx);
            }
            final DataContainerCodecContext<?, ?> childCtx = (DataContainerCodecContext<?, ?>) currentCtx.streamChild(type);
            return cacheHolder.getCachingSerializer(childCtx);
        }
        return null;
    }

    public static NormalizedNode<?, ?> serialize(final CachingNormalizedNodeCodecImpl<?> cacheHolder,
            final DataContainerCodecContext<?, ?> subtreeRoot, final DataObject key) {

        final BindingNormalizedNodeBuilder writer = new BindingNormalizedNodeBuilder(cacheHolder, subtreeRoot);
        try {
            subtreeRoot.eventStreamSerializer().serialize(key, writer);
            return writer.build();
        } catch (final IOException e) {
            throw Throwables.propagate(e);
        }
    }
}