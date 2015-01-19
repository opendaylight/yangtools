/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import org.opendaylight.yangtools.binding.data.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.yangtools.binding.data.codec.api.BindingNormalizedNodeWriterFactory;
import org.opendaylight.yangtools.binding.data.codec.gen.impl.DataObjectSerializerGenerator;
import org.opendaylight.yangtools.concepts.Delegator;
import org.opendaylight.yangtools.sal.binding.generator.util.BindingRuntimeContext;
import org.opendaylight.yangtools.yang.binding.BindingStreamEventWriter;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializer;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializerImplementation;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializerRegistry;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BindingNormalizedNodeCodecRegistry implements DataObjectSerializerRegistry, BindingNormalizedNodeWriterFactory, BindingNormalizedNodeSerializer {
    private static final Logger LOG = LoggerFactory.getLogger(BindingNormalizedNodeCodecRegistry.class);

    private final DataObjectSerializerGenerator generator;
    private final LoadingCache<Class<? extends DataObject>, DataObjectSerializer> serializers;
    private BindingCodecContext codecContext;

    public BindingNormalizedNodeCodecRegistry(final DataObjectSerializerGenerator generator) {
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
        return codecContext.getInstanceIdentifierCodec().serialize(binding);
    }

    @Override
    public InstanceIdentifier<?> fromYangInstanceIdentifier(final YangInstanceIdentifier dom) {
        return codecContext.getInstanceIdentifierCodec().deserialize(dom);
    }

    @Override
    public <T extends DataObject> Entry<YangInstanceIdentifier,NormalizedNode<?,?>> toNormalizedNode(final InstanceIdentifier<T> path, final T data) {
        final NormalizedNodeResult result = new NormalizedNodeResult();
        // We create DOM stream writer which produces normalized nodes
        final NormalizedNodeStreamWriter domWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        // We create Binding Stream Writer which translates from Binding to Normalized Nodes
        final Entry<YangInstanceIdentifier, BindingStreamEventWriter> writeCtx = codecContext.newWriter(path, domWriter);

        // We get serializer which reads binding data and uses Binding To Normalized Node writer to write result
        try {
            getSerializer(path.getTargetType()).serialize(data, writeCtx.getValue());
        } catch (final IOException e) {
            LOG.error("Unexpected failure while serializing path {} data {}", path, data, e);
            throw new IllegalStateException("Failed to create normalized node", e);
        }
        return new SimpleEntry<YangInstanceIdentifier,NormalizedNode<?,?>>(writeCtx.getKey(),result.getResult());
    }

    @Override
    public ContainerNode toNormalizedNodeNotification(final Notification data) {
        final NormalizedNodeResult result = new NormalizedNodeResult();
        // We create DOM stream writer which produces normalized nodes
        final NormalizedNodeStreamWriter domWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        @SuppressWarnings({ "unchecked", "rawtypes" })
        final Class<? extends DataObject> type = (Class) data.getImplementedInterface();
        @SuppressWarnings({ "unchecked", "rawtypes" })
        final BindingStreamEventWriter writer = newNotificationWriter((Class) type, domWriter);
        try {
            // FIXME: Should be cast to DataObject necessary?
            getSerializer(type).serialize((DataObject) data, writer);
        } catch (final IOException e) {
            LOG.error("Unexpected failure while serializing data {}", data, e);
            throw new IllegalStateException("Failed to create normalized node", e);
        }
        return (ContainerNode) result.getResult();

    }

    @Override
    public ContainerNode toNormalizedNodeRpcData(final DataContainer data) {
        final NormalizedNodeResult result = new NormalizedNodeResult();
        // We create DOM stream writer which produces normalized nodes
        final NormalizedNodeStreamWriter domWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        @SuppressWarnings({ "unchecked", "rawtypes" })
        final Class<? extends DataObject> type = (Class) data.getImplementedInterface();
        final BindingStreamEventWriter writer = newRpcWriter(type, domWriter);
        try {
            // FIXME: Should be cast to DataObject necessary?
            getSerializer(type).serialize((DataObject) data, writer);
        } catch (final IOException e) {
            LOG.error("Unexpected failure while serializing data {}", data, e);
            throw new IllegalStateException("Failed to create normalized node", e);
        }
        return (ContainerNode) result.getResult();
    }

    private static boolean isBindingRepresentable(final NormalizedNode<?, ?> data) {
        if (data instanceof ChoiceNode) {
            return false;
        }
        if (data instanceof LeafNode<?>) {
            return false;
        }
        if (data instanceof LeafSetNode) {
            return false;
        }
        if( data instanceof LeafSetEntryNode<?>) {
            return false;
        }
        if (data instanceof MapNode) {
            return false;
        }
        if (data instanceof UnkeyedListNode) {
            return false;
        }

        return true;
    }

    @Override
    public Entry<InstanceIdentifier<?>, DataObject> fromNormalizedNode(final YangInstanceIdentifier path, final NormalizedNode<?, ?> data) {
        if (!isBindingRepresentable(data)) {
            return null;
        }

        final List<PathArgument> builder = new ArrayList<>();
        final NodeCodecContext codec = codecContext.getCodecContextNode(path, builder);
        if (codec == null) {
            if (data != null) {
                LOG.warn("Path {} does not have a binding equivalent, should have been caught earlier ({})", path, data.getClass());
            }
            return null;
        }

        final DataObject lazyObj = (DataObject) codec.dataFromNormalizedNode(data);
        final InstanceIdentifier<?> bindingPath = InstanceIdentifier.create(builder);
        return new SimpleEntry<InstanceIdentifier<?>, DataObject>(bindingPath, lazyObj);
    }

    @Override
    public Notification fromNormalizedNodeNotification(final SchemaPath path, final ContainerNode data) {
        final NotificationCodecContext codec = codecContext.getNotificationContext(path);
        return (Notification) codec.dataFromNormalizedNode(data);
    }

    @Override
    public DataObject fromNormalizedNodeRpcData(final SchemaPath path, final ContainerNode data) {
        final ContainerNodeCodecContext codec = codecContext.getRpcDataContext(path);
        return (DataObject) codec.dataFromNormalizedNode(data);
    }

   @Override
    public Entry<YangInstanceIdentifier, BindingStreamEventWriter> newWriterAndIdentifier(final InstanceIdentifier<?> path, final NormalizedNodeStreamWriter domWriter) {
        return codecContext.newWriter(path, domWriter);
    }

    @Override
    public BindingStreamEventWriter newWriter(final InstanceIdentifier<?> path, final NormalizedNodeStreamWriter domWriter) {
        return codecContext.newWriterWithoutIdentifier(path, domWriter);
    }

    @Override
    public BindingStreamEventWriter newNotificationWriter(final Class<? extends Notification> notification,
            final NormalizedNodeStreamWriter streamWriter) {
        return codecContext.newNotificationWriter(notification, streamWriter);
    }


    @Override
    public BindingStreamEventWriter newRpcWriter(final Class<? extends DataContainer> rpcInputOrOutput,
            final NormalizedNodeStreamWriter streamWriter) {
        return codecContext.newRpcWriter(rpcInputOrOutput,streamWriter);
    }


    public <T extends DataObject> Function<Optional<NormalizedNode<?, ?>>, Optional<T>>  deserializeFunction(final InstanceIdentifier<T> path) {
        final DataObjectCodecContext<?> ctx = (DataObjectCodecContext<?>) codecContext.getCodecContextNode(path, null);
        return new DeserializeFunction<T>(ctx);
    }


    private static class DeserializeFunction<T> implements Function<Optional<NormalizedNode<?, ?>>, Optional<T>> {
        private final DataObjectCodecContext<?> ctx;

        public DeserializeFunction(final DataObjectCodecContext<?> ctx) {
            super();
            this.ctx = ctx;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Optional<T> apply(final Optional<NormalizedNode<?, ?>> input) {
            if(input.isPresent()) {
                return Optional.of((T) ctx.dataFromNormalizedNode(input.get()));
            }
            return Optional.absent();
        }
    }

    private class GeneratorLoader extends CacheLoader<Class<? extends DataContainer>, DataObjectSerializer> {

        @Override
        public DataObjectSerializer load(final Class<? extends DataContainer> key) throws Exception {
            final DataObjectSerializerImplementation prototype = generator.getSerializer(key);
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
        public void serialize(final DataObject obj, final BindingStreamEventWriter stream) throws IOException {
            delegate.serialize(BindingNormalizedNodeCodecRegistry.this, obj, stream);
        }
    }

}
