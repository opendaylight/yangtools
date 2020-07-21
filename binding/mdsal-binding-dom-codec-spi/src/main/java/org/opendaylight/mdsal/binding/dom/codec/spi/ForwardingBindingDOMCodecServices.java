/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.spi;

import com.google.common.annotations.Beta;
import com.google.common.collect.ForwardingObject;
import java.time.Instant;
import java.util.Map.Entry;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTreeNode;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingIdentityCodec;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingInstanceIdentifierCodec;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingLazyContainerNode;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingStreamEventWriter;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.yangtools.yang.binding.Action;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.binding.RpcInput;
import org.opendaylight.yangtools.yang.binding.RpcOutput;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

@Beta
public abstract class ForwardingBindingDOMCodecServices extends ForwardingObject implements BindingDOMCodecServices {
    @Override
    protected abstract @NonNull BindingDOMCodecServices delegate();

    @Override
    public BindingLazyContainerNode<RpcInput> toLazyNormalizedNodeActionInput(
            final Class<? extends Action<?, ?, ?>> action, final NodeIdentifier identifier, final RpcInput input) {
        return delegate().toLazyNormalizedNodeActionInput(action, identifier, input);
    }

    @Override
    public BindingLazyContainerNode<RpcOutput> toLazyNormalizedNodeActionOutput(
            final Class<? extends Action<?, ?, ?>> action, final NodeIdentifier identifier, final RpcOutput output) {
        return delegate().toLazyNormalizedNodeActionOutput(action, identifier, output);
    }

    @Override
    public YangInstanceIdentifier toYangInstanceIdentifier(final InstanceIdentifier<?> binding) {
        return delegate().toYangInstanceIdentifier(binding);
    }

    @Override
    public <T extends DataObject> InstanceIdentifier<T> fromYangInstanceIdentifier(final YangInstanceIdentifier dom) {
        return delegate().fromYangInstanceIdentifier(dom);
    }

    @Override
    public <T extends DataObject> Entry<YangInstanceIdentifier, NormalizedNode<?,?>> toNormalizedNode(
            final InstanceIdentifier<T> path, final T data) {
        return delegate().toNormalizedNode(path, data);
    }

    @Override
    public ContainerNode toNormalizedNodeNotification(final Notification data) {
        return delegate().toNormalizedNodeNotification(data);
    }

    @Override
    public ContainerNode toNormalizedNodeRpcData(final DataContainer data) {
        return delegate().toNormalizedNodeRpcData(data);
    }

    @Override
    public ContainerNode toNormalizedNodeActionInput(final Class<? extends Action<?, ?, ?>> action,
            final RpcInput input) {
        return delegate().toNormalizedNodeActionInput(action, input);
    }

    @Override
    public ContainerNode toNormalizedNodeActionOutput(final Class<? extends Action<?, ?, ?>> action,
            final RpcOutput output) {
        return delegate().toNormalizedNodeActionOutput(action, output);
    }

    @Override
    public Entry<InstanceIdentifier<?>, DataObject> fromNormalizedNode(final YangInstanceIdentifier path,
            final NormalizedNode<?, ?> data) {
        return delegate().fromNormalizedNode(path, data);
    }

    @Override
    public Notification fromNormalizedNodeNotification(final Absolute path, final ContainerNode data) {
        return delegate().fromNormalizedNodeNotification(path, data);
    }

    @Override
    public Notification fromNormalizedNodeNotification(final Absolute path, final ContainerNode data,
            final Instant eventInstant) {
        return delegate().fromNormalizedNodeNotification(path, data, eventInstant);
    }

    @Override
    public DataObject fromNormalizedNodeRpcData(final Absolute containerPath, final ContainerNode data) {
        return delegate().fromNormalizedNodeRpcData(containerPath, data);
    }

    @Override
    public <T extends RpcInput> T fromNormalizedNodeActionInput(final Class<? extends Action<?, ?, ?>> action,
            final ContainerNode input) {
        return delegate().fromNormalizedNodeActionInput(action, input);
    }

    @Override
    public <T extends RpcOutput> T fromNormalizedNodeActionOutput(final Class<? extends Action<?, ?, ?>> action,
            final ContainerNode output) {
        return delegate().fromNormalizedNodeActionOutput(action, output);
    }

    @Override
    public Entry<YangInstanceIdentifier, BindingStreamEventWriter> newWriterAndIdentifier(
            final InstanceIdentifier<?> path, final NormalizedNodeStreamWriter domWriter) {
        return delegate().newWriterAndIdentifier(path, domWriter);
    }

    @Override
    public BindingStreamEventWriter newWriter(final InstanceIdentifier<?> path,
            final NormalizedNodeStreamWriter domWriter) {
        return delegate().newWriter(path, domWriter);
    }

    @Override
    public BindingStreamEventWriter newNotificationWriter(final Class<? extends Notification> notification,
            final NormalizedNodeStreamWriter streamWriter) {
        return delegate().newNotificationWriter(notification, streamWriter);
    }

    @Override
    public BindingStreamEventWriter newActionInputWriter(final Class<? extends Action<?, ?, ?>> action,
            final NormalizedNodeStreamWriter domWriter) {
        return delegate().newActionInputWriter(action, domWriter);
    }

    @Override
    public BindingStreamEventWriter newActionOutputWriter(final Class<? extends Action<?, ?, ?>> action,
            final NormalizedNodeStreamWriter domWriter) {
        return delegate().newActionOutputWriter(action, domWriter);
    }

    @Override
    public BindingStreamEventWriter newRpcWriter(final Class<? extends DataContainer> rpcInputOrOutput,
            final NormalizedNodeStreamWriter streamWriter) {
        return delegate().newRpcWriter(rpcInputOrOutput,streamWriter);
    }

    @Override
    public <T extends DataObject> BindingDataObjectCodecTreeNode<T> getSubtreeCodec(final InstanceIdentifier<T> path) {
        return delegate().getSubtreeCodec(path);
    }

    @Override
    public BindingCodecTreeNode getSubtreeCodec(final YangInstanceIdentifier path) {
        return delegate().getSubtreeCodec(path);
    }

    @Override
    public BindingCodecTreeNode getSubtreeCodec(final Absolute path) {
        return delegate().getSubtreeCodec(path);
    }

    @Override
    public BindingIdentityCodec getIdentityCodec() {
        return delegate().getIdentityCodec();
    }

    @Override
    public BindingInstanceIdentifierCodec getInstanceIdentifierCodec() {
        return delegate().getInstanceIdentifierCodec();
    }

    @Override
    public BindingRuntimeContext getRuntimeContext() {
        return delegate().getRuntimeContext();
    }
}
