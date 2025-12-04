/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.spi;

import com.google.common.annotations.Beta;
import com.google.common.collect.ForwardingObject;
import java.time.Instant;
import java.util.Map.Entry;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.Action;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.BaseNotification;
import org.opendaylight.yangtools.binding.DataContainer;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.binding.Notification;
import org.opendaylight.yangtools.binding.RpcInput;
import org.opendaylight.yangtools.binding.RpcOutput;
import org.opendaylight.yangtools.binding.YangData;
import org.opendaylight.yangtools.binding.data.codec.api.BindingAugmentationCodecTreeNode;
import org.opendaylight.yangtools.binding.data.codec.api.BindingCodecTreeNode;
import org.opendaylight.yangtools.binding.data.codec.api.BindingDataContainerCodecTreeNode;
import org.opendaylight.yangtools.binding.data.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.yangtools.binding.data.codec.api.BindingIdentityCodec;
import org.opendaylight.yangtools.binding.data.codec.api.BindingInstanceIdentifierCodec;
import org.opendaylight.yangtools.binding.data.codec.api.BindingLazyContainerNode;
import org.opendaylight.yangtools.binding.data.codec.api.BindingStreamEventWriter;
import org.opendaylight.yangtools.binding.data.codec.api.BindingYangDataCodecTreeNode;
import org.opendaylight.yangtools.binding.data.codec.api.CommonDataObjectCodecTreeNode;
import org.opendaylight.yangtools.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.yangtools.yang.common.YangDataName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

@Beta
@Deprecated(since = "14.0.21", forRemoval = true)
public abstract class ForwardingBindingDOMCodecServices extends ForwardingObject implements BindingDOMCodecServices {
    @Override
    protected abstract @NonNull BindingDOMCodecServices delegate();

    @Override
    public BindingLazyContainerNode<RpcInput> toLazyNormalizedNodeActionInput(
            final Class<? extends Action<?, ?, ?>> action, final NodeIdentifier identifier, final RpcInput input) {
        return delegate().toLazyNormalizedNodeActionInput(action, identifier, input);
    }

    @Override
    public BindingLazyContainerNode<RpcInput> toLazyNormalizedNodeActionInput(
            final Class<? extends Action<?, ?, ?>> action, final RpcInput input) {
        return delegate().toLazyNormalizedNodeActionInput(action, input);
    }

    @Override
    public BindingLazyContainerNode<RpcOutput> toLazyNormalizedNodeActionOutput(
            final Class<? extends Action<?, ?, ?>> action, final NodeIdentifier identifier, final RpcOutput output) {
        return delegate().toLazyNormalizedNodeActionOutput(action, identifier, output);
    }

    @Override
    public BindingLazyContainerNode<RpcOutput> toLazyNormalizedNodeActionOutput(
            final Class<? extends Action<?, ?, ?>> action, final RpcOutput output) {
        return delegate().toLazyNormalizedNodeActionOutput(action, output);
    }

    @Override
    public YangInstanceIdentifier toYangInstanceIdentifier(final DataObjectReference<?> binding) {
        return delegate().toYangInstanceIdentifier(binding);
    }

    @Override
    public <T extends DataObject> DataObjectReference<T> fromYangInstanceIdentifier(final YangInstanceIdentifier dom) {
        return delegate().fromYangInstanceIdentifier(dom);
    }

    @Override
    public <T extends DataObject> NormalizedResult toNormalizedNode(final DataObjectReference<T> path, final T data) {
        return delegate().toNormalizedNode(path, data);
    }

    @Override
    public <A extends Augmentation<?>> @NonNull AugmentationResult toNormalizedAugmentation(
            final DataObjectReference<A> path, final A data) {
        return delegate().toNormalizedAugmentation(path, data);
    }

    @Override
    public <T extends DataObject> @NonNull NodeResult toNormalizedDataObject(final DataObjectReference<T> path,
            final T data) {
        return delegate().toNormalizedDataObject(path, data);
    }

    @Override
    public ContainerNode toNormalizedNodeNotification(final Notification<?> data) {
        return delegate().toNormalizedNodeNotification(data);
    }

    @Override
    public ContainerNode toNormalizedNodeNotification(final Absolute path, final BaseNotification data) {
        return delegate().toNormalizedNodeNotification(path, data);
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
    public Entry<DataObjectReference<?>, DataObject> fromNormalizedNode(final YangInstanceIdentifier path,
            final NormalizedNode data) {
        return delegate().fromNormalizedNode(path, data);
    }

    @Override
    public BaseNotification fromNormalizedNodeNotification(final Absolute path, final ContainerNode data) {
        return delegate().fromNormalizedNodeNotification(path, data);
    }

    @Override
    public BaseNotification fromNormalizedNodeNotification(final Absolute path, final ContainerNode data,
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
            final DataObjectReference<?> path, final NormalizedNodeStreamWriter domWriter) {
        return delegate().newWriterAndIdentifier(path, domWriter);
    }

    @Override
    public BindingStreamEventWriter newWriter(final DataObjectReference<?> path,
            final NormalizedNodeStreamWriter domWriter) {
        return delegate().newWriter(path, domWriter);
    }

    @Override
    public BindingStreamEventWriter newNotificationWriter(final Class<? extends Notification<?>> notification,
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
        return delegate().newRpcWriter(rpcInputOrOutput, streamWriter);
    }

    @Override
    public <T extends DataObject> CodecWithPath<T> getSubtreeCodecWithPath(final DataObjectReference<T> path) {
        return delegate().getSubtreeCodecWithPath(path);
    }

    @Override
    public <A extends Augmentation<?>> BindingAugmentationCodecTreeNode<A> getAugmentationCodec(
            final DataObjectReference<A> path) {
        return delegate().getAugmentationCodec(path);
    }

    @Override
    public <T extends DataObject> BindingDataObjectCodecTreeNode<T> getDataObjectCodec(
            final DataObjectReference<T> path) {
        return delegate().getDataObjectCodec(path);
    }

    @Override
    public <T extends DataObject> CommonDataObjectCodecTreeNode<T> getSubtreeCodec(final DataObjectReference<T> path) {
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
    public <T extends YangData<T>> BindingYangDataCodecTreeNode<T> getYangDataCodec(final Class<T> yangDataClass) {
        return delegate().getYangDataCodec(yangDataClass);
    }

    @Override
    public BindingYangDataCodecTreeNode<?> getYangDataCodec(final YangDataName yangDataName) {
        return delegate().getYangDataCodec(yangDataName);
    }

    @Override
    @Deprecated(since = "14.0.11", forRemoval = true)
    public BindingRuntimeContext getRuntimeContext() {
        return delegate().getRuntimeContext();
    }

    @Override
    public <E extends DataObject> BindingDataContainerCodecTreeNode<E> getStreamChild(final Class<E> childClass) {
        return delegate().getStreamChild(childClass);
    }
}
