/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

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
import java.util.function.BiFunction;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTree;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTreeFactory;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingLazyContainerNode;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeWriterFactory;
import org.opendaylight.mdsal.binding.dom.codec.gen.impl.DataObjectSerializerGenerator;
import org.opendaylight.mdsal.binding.dom.codec.util.AbstractBindingLazyContainerNode;
import org.opendaylight.mdsal.binding.generator.impl.ModuleInfoBackedContext;
import org.opendaylight.mdsal.binding.generator.util.BindingRuntimeContext;
import org.opendaylight.yangtools.concepts.Delegator;
import org.opendaylight.yangtools.yang.binding.Action;
import org.opendaylight.yangtools.yang.binding.BindingStreamEventWriter;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializer;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializerImplementation;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializerRegistry;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.binding.RpcInput;
import org.opendaylight.yangtools.yang.binding.RpcOutput;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
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
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BindingNormalizedNodeCodecRegistry implements DataObjectSerializerRegistry,
        BindingCodecTreeFactory, BindingNormalizedNodeWriterFactory,
        BindingNormalizedNodeSerializer {
    private static final Logger LOG = LoggerFactory.getLogger(BindingNormalizedNodeCodecRegistry.class);

    private final DataObjectSerializerGenerator generator;
    private final LoadingCache<Class<? extends DataObject>, DataObjectSerializer> serializers;
    private volatile BindingCodecContext codecContext;

    public BindingNormalizedNodeCodecRegistry(final DataObjectSerializerGenerator generator) {
        this.generator = Preconditions.checkNotNull(generator);
        this.serializers = CacheBuilder.newBuilder().weakKeys().build(new GeneratorLoader());
    }

    @Override
    public DataObjectSerializer getSerializer(final Class<? extends DataObject> type) {
        return serializers.getUnchecked(type);
    }

    public BindingCodecTree getCodecContext() {
        return codecContext;
    }

    public void onBindingRuntimeContextUpdated(final BindingRuntimeContext context) {
        codecContext = new BindingCodecContext(context, this);
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
    public <T extends DataObject> Entry<YangInstanceIdentifier, NormalizedNode<?,?>> toNormalizedNode(
            final InstanceIdentifier<T> path, final T data) {
        final NormalizedNodeResult result = new NormalizedNodeResult();
        // We create DOM stream writer which produces normalized nodes
        final NormalizedNodeStreamWriter domWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        // We create Binding Stream Writer which translates from Binding to Normalized Nodes
        final Entry<YangInstanceIdentifier, BindingStreamEventWriter> writeCtx = codecContext.newWriter(path,
            domWriter);

        // We get serializer which reads binding data and uses Binding To Normalized Node writer to write result
        try {
            getSerializer(path.getTargetType()).serialize(data, writeCtx.getValue());
        } catch (final IOException e) {
            LOG.error("Unexpected failure while serializing path {} data {}", path, data, e);
            throw new IllegalStateException("Failed to create normalized node", e);
        }
        return new SimpleEntry<>(writeCtx.getKey(),result.getResult());
    }

    @Override
    public ContainerNode toNormalizedNodeNotification(final Notification data) {
        // FIXME: Should the cast to DataObject be necessary?
        return serializeDataObject((DataObject) data,
            // javac does not like a methodhandle here
            (iface, domWriter) -> newNotificationWriter(iface.asSubclass(Notification.class), domWriter));
    }

    @Override
    public ContainerNode toNormalizedNodeRpcData(final DataContainer data) {
        // FIXME: Should the cast to DataObject be necessary?
        return serializeDataObject((DataObject) data, this::newRpcWriter);
    }

    @Override
    public ContainerNode toNormalizedNodeActionInput(final Class<? extends Action<?, ?, ?>> action,
            final RpcInput input) {
        return serializeDataObject(input, (iface, domWriter) -> newActionInputWriter(action, domWriter));
    }

    @Override
    public ContainerNode toNormalizedNodeActionOutput(final Class<? extends Action<?, ?, ?>> action,
            final RpcOutput output) {
        return serializeDataObject(output, (iface, domWriter) -> newActionOutputWriter(action, domWriter));
    }

    @Override
    public BindingLazyContainerNode<RpcInput> toLazyNormalizedNodeActionInput(
            final Class<? extends Action<?, ?, ?>> action, final NodeIdentifier identifier, final RpcInput input) {
        return new LazyActionInputContainerNode(identifier, input, this, action);
    }

    @Override
    public BindingLazyContainerNode<RpcOutput> toLazyNormalizedNodeActionOutput(
            final Class<? extends Action<?, ?, ?>> action, final NodeIdentifier identifier, final RpcOutput output) {
        return new LazyActionOutputContainerNode(identifier, output, this, action);
    }

    private <T extends DataContainer> ContainerNode serializeDataObject(final DataObject data,
            final BiFunction<Class<? extends T>, NormalizedNodeStreamWriter, BindingStreamEventWriter> newWriter) {
        final NormalizedNodeResult result = new NormalizedNodeResult();
        // We create DOM stream writer which produces normalized nodes
        final NormalizedNodeStreamWriter domWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        @SuppressWarnings("unchecked")
        final Class<? extends DataObject> type = (Class<? extends DataObject>) data.getImplementedInterface();
        final BindingStreamEventWriter writer = newWriter.apply((Class<T>)type, domWriter);
        try {
            getSerializer(type).serialize(data, writer);
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
        if (data instanceof LeafSetEntryNode<?>) {
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
    public Entry<InstanceIdentifier<?>, DataObject> fromNormalizedNode(final YangInstanceIdentifier path,
            final NormalizedNode<?, ?> data) {
        if (!isBindingRepresentable(data)) {
            return null;
        }

        final List<PathArgument> builder = new ArrayList<>();
        final NodeCodecContext<?> codec = codecContext.getCodecContextNode(path, builder);
        if (codec == null) {
            if (data != null) {
                LOG.warn("Path {} does not have a binding equivalent, should have been caught earlier ({})", path,
                    data.getClass());
            }
            return null;
        }

        final DataObject lazyObj = codec.deserialize(data);
        final InstanceIdentifier<?> bindingPath = InstanceIdentifier.create(builder);
        return new SimpleEntry<>(bindingPath, lazyObj);
    }

    @Override
    public Notification fromNormalizedNodeNotification(final SchemaPath path, final ContainerNode data) {
        final NotificationCodecContext<?> codec = codecContext.getNotificationContext(path);
        return codec.deserialize(data);
    }

    @Override
    public DataObject fromNormalizedNodeRpcData(final SchemaPath path, final ContainerNode data) {
        final RpcInputCodec<?> codec = codecContext.getRpcInputCodec(path);
        return codec.deserialize(data);
    }

    @Override
    public <T extends RpcInput> T fromNormalizedNodeActionInput(final Class<? extends Action<?, ?, ?>> action,
            final ContainerNode input) {
        return (T) requireNonNull(codecContext.getActionCodec(action).input().deserialize(requireNonNull(input)));
    }

    @Override
    public <T extends RpcOutput> T fromNormalizedNodeActionOutput(final Class<? extends Action<?, ?, ?>> action,
            final ContainerNode output) {
        return (T) requireNonNull(codecContext.getActionCodec(action).output().deserialize(requireNonNull(output)));
    }

    @Override
    public Entry<YangInstanceIdentifier, BindingStreamEventWriter> newWriterAndIdentifier(
            final InstanceIdentifier<?> path, final NormalizedNodeStreamWriter domWriter) {
        return codecContext.newWriter(path, domWriter);
    }

    @Override
    public BindingStreamEventWriter newWriter(final InstanceIdentifier<?> path,
            final NormalizedNodeStreamWriter domWriter) {
        return codecContext.newWriterWithoutIdentifier(path, domWriter);
    }

    @Override
    public BindingStreamEventWriter newNotificationWriter(final Class<? extends Notification> notification,
            final NormalizedNodeStreamWriter streamWriter) {
        return codecContext.newNotificationWriter(notification, streamWriter);
    }

    @Override
    public BindingStreamEventWriter newActionInputWriter(final Class<? extends Action<?, ?, ?>> action,
            final NormalizedNodeStreamWriter domWriter) {
        return codecContext.getActionCodec(action).input().createWriter(domWriter);
    }

    @Override
    public BindingStreamEventWriter newActionOutputWriter(final Class<? extends Action<?, ?, ?>> action,
            final NormalizedNodeStreamWriter domWriter) {
        return codecContext.getActionCodec(action).output().createWriter(domWriter);
    }

    @Override
    public BindingStreamEventWriter newRpcWriter(final Class<? extends DataContainer> rpcInputOrOutput,
            final NormalizedNodeStreamWriter streamWriter) {
        return codecContext.newRpcWriter(rpcInputOrOutput,streamWriter);
    }

    public <T extends DataObject> Function<Optional<NormalizedNode<?, ?>>, Optional<T>>  deserializeFunction(
            final InstanceIdentifier<T> path) {
        final DataObjectCodecContext<?,?> ctx = (DataObjectCodecContext<?,?>) codecContext.getCodecContextNode(path,
            null);
        return new DeserializeFunction<>(ctx);
    }

    @Override
    public BindingCodecTree create(final BindingRuntimeContext context) {
        return new BindingCodecContext(context, this);
    }

    @Override
    @SuppressWarnings("checkstyle:illegalCatch")
    public BindingCodecTree create(final SchemaContext context, final Class<?>... bindingClasses) {
        final ModuleInfoBackedContext strategy = ModuleInfoBackedContext.create();
        for (final Class<?> bindingCls : bindingClasses) {
            try {
                strategy.registerModuleInfo(BindingReflections.getModuleInfo(bindingCls));
            } catch (final Exception e) {
                throw new IllegalStateException(
                        "Could not create BindingRuntimeContext from class " + bindingCls.getName(), e);
            }
        }
        final BindingRuntimeContext runtimeCtx = BindingRuntimeContext.create(strategy, context);
        return create(runtimeCtx);
    }


    private static final class DeserializeFunction<T> implements Function<Optional<NormalizedNode<?, ?>>, Optional<T>> {
        private final DataObjectCodecContext<?,?> ctx;

        DeserializeFunction(final DataObjectCodecContext<?,?> ctx) {
            this.ctx = ctx;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Optional<T> apply(final Optional<NormalizedNode<?, ?>> input) {
            return input.transform(data -> (T) ctx.deserialize(data));
        }
    }

    private final class GeneratorLoader extends CacheLoader<Class<? extends DataContainer>, DataObjectSerializer> {
        @Override
        public DataObjectSerializer load(final Class<? extends DataContainer> key) {
            final DataObjectSerializerImplementation prototype = generator.getSerializer(key);
            return new DataObjectSerializerProxy(prototype);
        }
    }

    private final class DataObjectSerializerProxy
            implements DataObjectSerializer, Delegator<DataObjectSerializerImplementation> {
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

    @NonNullByDefault
    private abstract static class AbstractLazyActionContainerNode<T extends DataObject>
            extends AbstractBindingLazyContainerNode<T, BindingNormalizedNodeCodecRegistry> {
        protected final Class<? extends Action<?, ?, ?>> action;

        AbstractLazyActionContainerNode(final NodeIdentifier identifier, final T bindingData,
            final BindingNormalizedNodeCodecRegistry context, final Class<? extends Action<?, ?, ?>> action) {
            super(identifier, bindingData, context);
            this.action = requireNonNull(action);
        }
    }

    @NonNullByDefault
    private static final class LazyActionInputContainerNode extends AbstractLazyActionContainerNode<RpcInput> {
        LazyActionInputContainerNode(final NodeIdentifier identifier, final RpcInput bindingData,
                final BindingNormalizedNodeCodecRegistry context, final Class<? extends Action<?, ?, ?>> action) {
            super(identifier, bindingData, context, action);
        }

        @Override
        protected ContainerNode computeContainerNode(final BindingNormalizedNodeCodecRegistry context) {
            return context.toNormalizedNodeActionInput(action, getDataObject());
        }
    }

    @NonNullByDefault
    private static final class LazyActionOutputContainerNode extends AbstractLazyActionContainerNode<RpcOutput> {
        LazyActionOutputContainerNode(final NodeIdentifier identifier, final RpcOutput bindingData,
                final BindingNormalizedNodeCodecRegistry context, final Class<? extends Action<?, ?, ?>> action) {
            super(identifier, bindingData, context, action);
        }

        @Override
        protected ContainerNode computeContainerNode(final BindingNormalizedNodeCodecRegistry context) {
            return context.toNormalizedNodeActionOutput(action, getDataObject());
        }
    }
}
