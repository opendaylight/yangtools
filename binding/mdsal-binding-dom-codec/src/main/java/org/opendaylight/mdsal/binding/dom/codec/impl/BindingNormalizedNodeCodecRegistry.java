/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.time.Instant;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTree;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingLazyContainerNode;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeWriterFactory;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingStreamEventWriter;
import org.opendaylight.mdsal.binding.dom.codec.spi.LazyActionInputContainerNode;
import org.opendaylight.mdsal.binding.dom.codec.spi.LazyActionOutputContainerNode;
import org.opendaylight.yangtools.yang.binding.Action;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.binding.RpcInput;
import org.opendaylight.yangtools.yang.binding.RpcOutput;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.api.schema.ValueNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BindingNormalizedNodeCodecRegistry
        implements BindingNormalizedNodeWriterFactory, BindingNormalizedNodeSerializer {
    private static final Logger LOG = LoggerFactory.getLogger(BindingNormalizedNodeCodecRegistry.class);

    private static final AtomicReferenceFieldUpdater<BindingNormalizedNodeCodecRegistry, BindingCodecContext> UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(BindingNormalizedNodeCodecRegistry.class, BindingCodecContext.class,
                "codecContext");
    private volatile BindingCodecContext codecContext;

    public BindingNormalizedNodeCodecRegistry() {

    }

    public BindingNormalizedNodeCodecRegistry(final BindingRuntimeContext codecContext) {
        this();
        onBindingRuntimeContextUpdated(codecContext);
    }

    public BindingCodecTree getCodecContext() {
        return codecContext;
    }

    public void onBindingRuntimeContextUpdated(final BindingRuntimeContext context) {
        // BindingCodecContext is a costly resource. Let us not ditch it unless we have to
        final BindingCodecContext current = codecContext;
        if (current != null && context.equals(current.getRuntimeContext())) {
            LOG.debug("Skipping update of runtime context {}", context);
            return;
        }

        final BindingCodecContext updated = new BindingCodecContext(context);
        if (!UPDATER.compareAndSet(this, current, updated)) {
            LOG.warn("Concurrent update of runtime context (expected={} current={}) detected at ", current,
                codecContext, new Throwable());
        }
    }

    final @NonNull BindingCodecContext codecContext() {
        final BindingCodecContext local = codecContext;
        checkState(local != null, "No context available yet");
        return local;
    }

    @Override
    public YangInstanceIdentifier toYangInstanceIdentifier(final InstanceIdentifier<?> binding) {
        return codecContext.getInstanceIdentifierCodec().fromBinding(binding);
    }

    @Override
    public <T extends DataObject> InstanceIdentifier<T> fromYangInstanceIdentifier(final YangInstanceIdentifier dom) {
        return codecContext.getInstanceIdentifierCodec().toBinding(dom);
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
            codecContext.getSerializer(path.getTargetType()).serialize(data, writeCtx.getValue());
        } catch (final IOException e) {
            LOG.error("Unexpected failure while serializing path {} data {}", path, data, e);
            throw new IllegalStateException("Failed to create normalized node", e);
        }
        return new SimpleEntry<>(writeCtx.getKey(),result.getResult());
    }

    @Override
    @SuppressFBWarnings("BC_UNCONFIRMED_CAST")
    public ContainerNode toNormalizedNodeNotification(final Notification data) {
        // FIXME: Should the cast to DataObject be necessary?
        return serializeDataObject((DataObject) data,
            // javac does not like a methodhandle here
            (iface, domWriter) -> newNotificationWriter(iface.asSubclass(Notification.class), domWriter));
    }

    @Override
    @SuppressFBWarnings("BC_UNCONFIRMED_CAST")
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
        final Class<? extends DataObject> type = data.implementedInterface();
        @SuppressWarnings("unchecked")
        final BindingStreamEventWriter writer = newWriter.apply((Class<T>)type, domWriter);
        try {
            codecContext.getSerializer(type).serialize(data, writer);
        } catch (final IOException e) {
            LOG.error("Unexpected failure while serializing data {}", data, e);
            throw new IllegalStateException("Failed to create normalized node", e);
        }
        return (ContainerNode) result.getResult();
    }

    private static boolean notBindingRepresentable(final NormalizedNode<?, ?> data) {
        // ValueNode covers LeafNode and LeafSetEntryNode
        return data instanceof ValueNode
                || data instanceof MapNode || data instanceof UnkeyedListNode
                || data instanceof ChoiceNode
                || data instanceof LeafSetNode;
    }

    @Override
    public Entry<InstanceIdentifier<?>, DataObject> fromNormalizedNode(final YangInstanceIdentifier path,
            final NormalizedNode<?, ?> data) {
        if (notBindingRepresentable(data)) {
            return null;
        }

        final List<PathArgument> builder = new ArrayList<>();
        final BindingDataObjectCodecTreeNode<?> codec = codecContext.getCodecContextNode(path, builder);
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
    public Notification fromNormalizedNodeNotification(final SchemaPath path, final ContainerNode data,
            final Instant eventInstant) {
        if (eventInstant == null) {
            return fromNormalizedNodeNotification(path, data);
        }

        final NotificationCodecContext<?> codec = codecContext.getNotificationContext(path);
        return codec.deserialize(data, eventInstant);
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

    private static final class DeserializeFunction<T> implements Function<Optional<NormalizedNode<?, ?>>, Optional<T>> {
        private final DataObjectCodecContext<?,?> ctx;

        DeserializeFunction(final DataObjectCodecContext<?,?> ctx) {
            this.ctx = ctx;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Optional<T> apply(final Optional<NormalizedNode<?, ?>> input) {
            return input.map(data -> (T) ctx.deserialize(data));
        }
    }
}
