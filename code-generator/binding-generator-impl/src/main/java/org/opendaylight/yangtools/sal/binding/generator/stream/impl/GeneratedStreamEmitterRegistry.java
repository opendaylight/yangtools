package org.opendaylight.yangtools.sal.binding.generator.stream.impl;

import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.concepts.Delegator;
import org.opendaylight.yangtools.yang.binding.BindingStreamEventWriter;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializer;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializerImplementation;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializerRegistry;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.codec.CodecRegistry;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

public class GeneratedStreamEmitterRegistry implements DataObjectSerializerRegistry {

    private final AbstractStreamWriterGenerator generator;
    private final CodecRegistry codecRegistry;

    public GeneratedStreamEmitterRegistry(final AbstractStreamWriterGenerator generator, final CodecRegistry registry) {
        this.generator = Preconditions.checkNotNull(generator);
        this.codecRegistry = Preconditions.checkNotNull(registry);
    }

    @Override
    public DataObjectSerializer getSerializer(final Class<? extends DataObject> type) {
        DataObjectSerializerImplementation prototype = generator.getSerializer(type);
        return new DataObjectSerializerProxy(prototype);
    }

    public BindingStreamEventWriter newWriter(final DataSchemaNode schema, final NormalizedNodeStreamWriter domWriter) {
        return new BindingToNormalizedStreamWriter(schema, domWriter, codecRegistry);
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
