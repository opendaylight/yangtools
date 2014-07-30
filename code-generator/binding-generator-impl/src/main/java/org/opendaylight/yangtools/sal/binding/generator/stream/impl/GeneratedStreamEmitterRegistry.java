package org.opendaylight.yangtools.sal.binding.generator.stream.impl;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.opendaylight.yangtools.concepts.Delegator;
import org.opendaylight.yangtools.sal.binding.generator.impl.BindingGeneratorImpl;
import org.opendaylight.yangtools.yang.binding.BindingStreamEventWriter;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializer;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializerImplementation;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializerRegistry;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.codec.CodecRegistry;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextListener;

public class GeneratedStreamEmitterRegistry implements DataObjectSerializerRegistry, SchemaContextListener {

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

    @Override
    public void onGlobalContextUpdated(final SchemaContext context) {
        BindingGeneratorImpl apiGen = new BindingGeneratorImpl();
        apiGen.generateTypes(context);
        currentContext = new BindingCodecContext(context, apiGen.getModuleContexts().values(),codecRegistry);
    }



    public BindingStreamEventWriter newWriter(final InstanceIdentifier<?> path, final NormalizedNodeStreamWriter domWriter) {
        return currentContext.newWriter(path,domWriter);
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
