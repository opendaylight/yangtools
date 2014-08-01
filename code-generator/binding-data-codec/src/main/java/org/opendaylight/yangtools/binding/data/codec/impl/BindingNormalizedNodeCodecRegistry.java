/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.AbstractMap.SimpleEntry;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.opendaylight.yangtools.binding.data.codec.api.BindingNormalizedNodeSerializer;
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
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;

public class BindingNormalizedNodeCodecRegistry implements DataObjectSerializerRegistry, BindingNormalizedNodeWriterFactory, BindingNormalizedNodeSerializer {

    private final AbstractStreamWriterGenerator generator;
    private final LoadingCache<Class<? extends DataObject>, DataObjectSerializer> serializers;
    private BindingCodecContext codecContext;

    public BindingNormalizedNodeCodecRegistry(final AbstractStreamWriterGenerator generator) {
        this.generator = Preconditions.checkNotNull(generator);
        this.serializers = CacheBuilder.newBuilder().weakKeys().build(new GeneratorLoader());
    }

    @Override
    public DataObjectSerializer getSerializer(final Class<? extends DataObject> type) {
        return serializers.getUnchecked(type);
    }

    public BindingCodecContext getCodecContext() {
        return codecContext;
    }

    public void onBindingRuntimeContextUpdated(final BindingRuntimeContext context) {
        codecContext = new BindingCodecContext(context);
        generator.onBindingRuntimeContextUpdated(context);
    }


    @Override
    public YangInstanceIdentifier toYangInstanceIdentifier(final InstanceIdentifier<?> binding) {
        List<YangInstanceIdentifier.PathArgument> builder = new LinkedList<>();
        codecContext.getCodecContextNode(binding, builder);
        return codecContext.getInstanceIdentifierCodec().serialize(binding);
    }

    @Override
    public InstanceIdentifier<?> fromYangInstanceIdentifier(final YangInstanceIdentifier dom) {
        return codecContext.getInstanceIdentifierCodec().deserialize(dom);
   }

    @Override
    public <T extends DataObject> Entry<YangInstanceIdentifier,NormalizedNode<?,?>> toNormalizedNode(final InstanceIdentifier<T> path, final T data) {
        NormalizedNodeResult result = new NormalizedNodeResult();
        // We create dom stream writer which produces normalized nodes
        NormalizedNodeStreamWriter domWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        // We create Binding Stream Writer wchich translates from Binding to Normalized Nodes
        Entry<YangInstanceIdentifier, BindingStreamEventWriter> writeCtx = codecContext.newWriter(path, domWriter);

        // We get serializer which reads binding data and uses Binding To NOrmalized Node writer to write result
        getSerializer(path.getTargetType()).serialize(data, writeCtx.getValue());
        return new SimpleEntry<YangInstanceIdentifier,NormalizedNode<?,?>>(writeCtx.getKey(),result.getResult());
    }

    @Override
    public Entry<InstanceIdentifier<?>, DataObject> fromNormalizedNode(final YangInstanceIdentifier path,
            final NormalizedNode<?, ?> data) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Map<InstanceIdentifier<?>, DataObject> fromNormalizedNodes(
            final Map<YangInstanceIdentifier, NormalizedNode<?, ?>> dom) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Entry<YangInstanceIdentifier, BindingStreamEventWriter> newWriterAndIdentifier(final InstanceIdentifier<?> path, final NormalizedNodeStreamWriter domWriter) {
        return codecContext.newWriter(path, domWriter);
    }

    @Override
    public BindingStreamEventWriter newWriter(final InstanceIdentifier<?> path, final NormalizedNodeStreamWriter domWriter) {
        return codecContext.newWriterWithoutIdentifier(path, domWriter);
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
            delegate.serialize(BindingNormalizedNodeCodecRegistry.this, obj, stream);
        }
    }

}
