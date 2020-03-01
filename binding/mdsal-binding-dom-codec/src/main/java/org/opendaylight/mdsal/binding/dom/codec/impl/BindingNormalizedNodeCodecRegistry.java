/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static com.google.common.base.Preconditions.checkState;

import java.time.Instant;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTree;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeWriterFactory;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingStreamEventWriter;
import org.opendaylight.mdsal.binding.dom.codec.spi.AbstractBindingNormalizedNodeSerializer;
import org.opendaylight.yangtools.yang.binding.Action;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.binding.RpcInput;
import org.opendaylight.yangtools.yang.binding.RpcOutput;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BindingNormalizedNodeCodecRegistry extends AbstractBindingNormalizedNodeSerializer
        implements BindingNormalizedNodeWriterFactory {
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
        return codecContext().toYangInstanceIdentifier(binding);
    }

    @Override
    public <T extends DataObject> InstanceIdentifier<T> fromYangInstanceIdentifier(final YangInstanceIdentifier dom) {
        return codecContext().getInstanceIdentifierCodec().toBinding(dom);
    }

    @Override
    public <T extends DataObject> Entry<YangInstanceIdentifier, NormalizedNode<?,?>> toNormalizedNode(
            final InstanceIdentifier<T> path, final T data) {
        return codecContext().toNormalizedNode(path, data);
    }

    @Override
    public ContainerNode toNormalizedNodeNotification(final Notification data) {
        return codecContext().toNormalizedNodeNotification(data);
    }

    @Override
    public ContainerNode toNormalizedNodeRpcData(final DataContainer data) {
        return codecContext().toNormalizedNodeRpcData(data);
    }

    @Override
    public ContainerNode toNormalizedNodeActionInput(final Class<? extends Action<?, ?, ?>> action,
            final RpcInput input) {
        return codecContext().toNormalizedNodeActionInput(action, input);
    }

    @Override
    public ContainerNode toNormalizedNodeActionOutput(final Class<? extends Action<?, ?, ?>> action,
            final RpcOutput output) {
        return codecContext().toNormalizedNodeActionOutput(action, output);
    }

    @Override
    public Entry<InstanceIdentifier<?>, DataObject> fromNormalizedNode(final YangInstanceIdentifier path,
            final NormalizedNode<?, ?> data) {
        return codecContext().fromNormalizedNode(path, data);
    }

    @Override
    public Notification fromNormalizedNodeNotification(final SchemaPath path, final ContainerNode data) {
        return codecContext().fromNormalizedNodeNotification(path, data);
    }

    @Override
    public Notification fromNormalizedNodeNotification(final SchemaPath path, final ContainerNode data,
            final Instant eventInstant) {
        return codecContext().fromNormalizedNodeNotification(path, data, eventInstant);
    }

    @Override
    public DataObject fromNormalizedNodeRpcData(final SchemaPath path, final ContainerNode data) {
        return codecContext().fromNormalizedNodeRpcData(path, data);
    }

    @Override
    public <T extends RpcInput> T fromNormalizedNodeActionInput(final Class<? extends Action<?, ?, ?>> action,
            final ContainerNode input) {
        return codecContext().fromNormalizedNodeActionInput(action, input);
    }

    @Override
    public <T extends RpcOutput> T fromNormalizedNodeActionOutput(final Class<? extends Action<?, ?, ?>> action,
            final ContainerNode output) {
        return codecContext().fromNormalizedNodeActionOutput(action, output);
    }

    @Override
    public Entry<YangInstanceIdentifier, BindingStreamEventWriter> newWriterAndIdentifier(
            final InstanceIdentifier<?> path, final NormalizedNodeStreamWriter domWriter) {
        return codecContext().newWriterAndIdentifier(path, domWriter);
    }

    @Override
    public BindingStreamEventWriter newWriter(final InstanceIdentifier<?> path,
            final NormalizedNodeStreamWriter domWriter) {
        return codecContext().newWriter(path, domWriter);
    }

    @Override
    public BindingStreamEventWriter newNotificationWriter(final Class<? extends Notification> notification,
            final NormalizedNodeStreamWriter streamWriter) {
        return codecContext().newNotificationWriter(notification, streamWriter);
    }

    @Override
    public BindingStreamEventWriter newActionInputWriter(final Class<? extends Action<?, ?, ?>> action,
            final NormalizedNodeStreamWriter domWriter) {
        return codecContext().newActionInputWriter(action, domWriter);
    }

    @Override
    public BindingStreamEventWriter newActionOutputWriter(final Class<? extends Action<?, ?, ?>> action,
            final NormalizedNodeStreamWriter domWriter) {
        return codecContext().newActionOutputWriter(action, domWriter);
    }

    @Override
    public BindingStreamEventWriter newRpcWriter(final Class<? extends DataContainer> rpcInputOrOutput,
            final NormalizedNodeStreamWriter streamWriter) {
        return codecContext().newRpcWriter(rpcInputOrOutput,streamWriter);
    }

    public <T extends DataObject> Function<Optional<NormalizedNode<?, ?>>, Optional<T>>  deserializeFunction(
            final InstanceIdentifier<T> path) {
        final DataObjectCodecContext<?,?> ctx = (DataObjectCodecContext<?,?>) codecContext().getCodecContextNode(path,
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
