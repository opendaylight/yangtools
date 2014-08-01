package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import org.opendaylight.yangtools.binding.data.codec.api.BindingNormalizedNodeWriterFactory;
import org.opendaylight.yangtools.binding.data.codec.gen.impl.AbstractStreamWriterGenerator;
import org.opendaylight.yangtools.concepts.Delegator;
import org.opendaylight.yangtools.sal.binding.generator.util.BindingRuntimeContext;
import org.opendaylight.yangtools.yang.binding.BindingStreamEventWriter;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializer;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializerImplementation;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializerRegistry;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.codec.CodecRegistry;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;

public class GeneratedStreamEmitterRegistry implements DataObjectSerializerRegistry, BindingNormalizedNodeWriterFactory {

    private final AbstractStreamWriterGenerator generator;
    private final CodecRegistry codecRegistry;

    private final LoadingCache<Class<? extends DataObject>, DataObjectSerializer> serializers;
    private BindingCodecContext currentContext;

    public GeneratedStreamEmitterRegistry(final AbstractStreamWriterGenerator generator, final CodecRegistry registry) {
        this.generator = Preconditions.checkNotNull(generator);
        this.codecRegistry = Preconditions.checkNotNull(registry);
        this.serializers = CacheBuilder.newBuilder().weakKeys().build(new GeneratorLoader());
    }

    @Override
    public DataObjectSerializer getSerializer(final Class<? extends DataObject> type) {
        return serializers.getUnchecked(type);
    }

    public CodecRegistry getCodecRegistry() {
        return codecRegistry;
    }

    public void onBindingRuntimeContextUpdated(final BindingRuntimeContext context) {
        currentContext = new BindingCodecContext(context,codecRegistry);
        generator.onBindingRuntimeContextUpdated(context);
    }


    public <T extends DataObject> Entry<YangInstanceIdentifier,NormalizedNode<?,?>> toNormalizedNode(final InstanceIdentifier<T> path, final T data) {
        NormalizedNodeResult result = new NormalizedNodeResult();
        // We create dom stream writer which produces normalized nodes
        NormalizedNodeStreamWriter domWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        // We create Binding Stream Writer wchich translates from Binding to Normalized Nodes
        Entry<YangInstanceIdentifier, BindingStreamEventWriter> writeCtx = currentContext.newWriter(path, domWriter);

        // We get serializer which reads binding data and uses Binding To NOrmalized Node writer to write result
        getSerializer(path.getTargetType()).serialize(data, writeCtx.getValue());
        return new SimpleEntry<YangInstanceIdentifier,NormalizedNode<?,?>>(writeCtx.getKey(),result.getResult());
    }


    @Override
    public Entry<YangInstanceIdentifier, BindingStreamEventWriter> newWriter(final InstanceIdentifier<?> path, final NormalizedNodeStreamWriter domWriter) {
        return currentContext.newWriter(path, domWriter);
    }

    @Override
    public BindingStreamEventWriter newWriterWithoutIdentifier(final InstanceIdentifier<?> path, final NormalizedNodeStreamWriter domWriter) {
        return currentContext.newWriterWithoutIdentifier(path, domWriter);
    }

    private class GeneratorLoader extends CacheLoader<Class<? extends DataObject>, DataObjectSerializer> {

        @Override
        public DataObjectSerializer load(final Class<? extends DataObject> key) throws Exception {
            DataObjectSerializerImplementation prototype = generator.getSerializer(key);
            return new DataObjectSerializerProxy(prototype);
        }
    }

    private class DataObjectSerializerProxy implements DataObjectSerializer,
            Delegator<DataObjectSerializerImplementation> {

        private final DataObjectSerializerImplementation delegate;

        DataObjectSerializerProxy(final DataObjectSerializerImplementation delegate) {
            this.delegate = delegate;
        }

        @Override
        public DataObjectSerializerImplementation getDelegate() {
            return delegate;
        }

        @Override
        public void serialize(final DataObject obj, final BindingStreamEventWriter stream) {
            delegate.serialize(GeneratedStreamEmitterRegistry.this, obj, stream);
        }

    }
}
