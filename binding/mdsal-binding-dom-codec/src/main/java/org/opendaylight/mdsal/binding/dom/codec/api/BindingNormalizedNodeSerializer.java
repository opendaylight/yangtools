/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.api;

import com.google.common.annotations.Beta;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.Action;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.binding.RpcInput;
import org.opendaylight.yangtools.yang.binding.RpcOutput;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * Serialization service, which provides two-way serialization between Java
 * Binding Data representation and NormalizedNode representation.
 */
public interface BindingNormalizedNodeSerializer {
    /**
     * Translates supplied Binding Instance Identifier into NormalizedNode instance identifier.
     *
     * @param binding Binding Instance Identifier
     * @return DOM Instance Identifier
     * @throws IllegalArgumentException If supplied Instance Identifier is not valid.
     */
    YangInstanceIdentifier toYangInstanceIdentifier(@Nonnull InstanceIdentifier<?> binding);

    /**
     * Translates supplied YANG Instance Identifier into Binding instance identifier.
     *
     * @param dom YANG Instance Identifier
     * @return Binding Instance Identifier, or null if the instance identifier is not representable.
     */
    @Nullable
    <T extends DataObject> InstanceIdentifier<T> fromYangInstanceIdentifier(@Nonnull YangInstanceIdentifier dom);

    /**
     * Translates supplied Binding Instance Identifier and data into NormalizedNode representation.
     *
     * @param path Binding Instance Identifier pointing to data
     * @param data Data object representing data
     * @return NormalizedNode representation
     * @throws IllegalArgumentException If supplied Instance Identifier is not valid.
     */
    <T extends DataObject> Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> toNormalizedNode(
            InstanceIdentifier<T> path, T data);

    /**
     * Translates supplied YANG Instance Identifier and NormalizedNode into Binding data.
     *
     * @param path Binding Instance Identifier
     * @param data NormalizedNode representing data
     * @return DOM Instance Identifier
     */
    @Nullable
    Entry<InstanceIdentifier<?>, DataObject> fromNormalizedNode(@Nonnull YangInstanceIdentifier path,
            NormalizedNode<?, ?> data);

    /**
     * Translates supplied NormalizedNode Notification into Binding data.
     *
     * @param path Schema Path of Notification, schema path is absolute, and consists of Notification QName.
     * @param data NormalizedNode representing data
     * @return Binding representation of Notification
     */
    @Nullable Notification fromNormalizedNodeNotification(@Nonnull SchemaPath path,@Nonnull ContainerNode data);

    /**
     * Translates supplied NormalizedNode RPC input or output into Binding data.
     *
     * @param path Schema path of RPC data, Schema path consists of rpc QName and input / output QName.
     * @param data NormalizedNode representing data
     * @return Binding representation of RPC data
     */
    @Nullable DataObject fromNormalizedNodeRpcData(@Nonnull SchemaPath path,@Nonnull ContainerNode data);

    /**
     * Translates supplied ContainerNode action input.
     *
     * @param action Binding action class
     * @param input ContainerNode representing data
     * @return Binding representation of action input
     * @throws NullPointerException if any of the arguments is null
     */
    @Beta
    <T extends RpcInput> @NonNull T fromNormalizedNodeActionInput(
            @NonNull Class<? extends Action<?, ?, ?>> action, @NonNull ContainerNode input);

    /**
     * Translates supplied ContainerNode action output.
     *
     * @param action Binding action class
     * @param output ContainerNode representing data
     * @return Binding representation of action output
     * @throws NullPointerException if any of the arguments is null
     */
    @Beta
    <T extends RpcOutput> @NonNull T fromNormalizedNodeActionOutput(
            @NonNull Class<? extends Action<?, ?, ?>> action, @NonNull ContainerNode output);

    /**
     * Translates supplied Binding Notification or output into NormalizedNode notification.
     *
     * @param data NormalizedNode representing notification data
     * @return NormalizedNode representation of notification
     */
    @Nonnull ContainerNode toNormalizedNodeNotification(@Nonnull Notification data);

    /**
     * Translates supplied Binding RPC input or output into NormalizedNode data.
     *
     * @param data NormalizedNode representing rpc data
     * @return NormalizedNode representation of rpc data
     */
    @Nonnull ContainerNode toNormalizedNodeRpcData(@Nonnull DataContainer data);

    /**
     * Lazily translates supplied Binding action input into NormalizedNode data.
     *
     * @param action Binding action class
     * @param input Binding action input
     * @return NormalizedNode representation of action input
     * @throws NullPointerException if any of the arguments is null
     */
    @Beta
    @NonNull BindingLazyContainerNode<RpcInput> toLazyNormalizedNodeActionInput(
            @NonNull Class<? extends Action<?, ?, ?>> action, @NonNull NodeIdentifier identifier,
                    @NonNull RpcInput input);

    /**
     * Lazily translates supplied Binding action input into NormalizedNode data.
     *
     * @param action Binding action class
     * @param input Binding action input
     * @return NormalizedNode representation of action input
     * @throws NullPointerException if any of the arguments is null
     */
    @Beta default @NonNull BindingLazyContainerNode<RpcInput> toLazyNormalizedNodeActionInput(
            @NonNull final Class<? extends Action<?, ?, ?>> action, @NonNull final RpcInput input) {
        return toLazyNormalizedNodeActionInput(action,
            new NodeIdentifier(YangConstants.operationInputQName(BindingReflections.getQNameModule(action))), input);
    }

    /**
     * Translates supplied Binding action input into NormalizedNode data.
     *
     * @param action Binding action class
     * @param input Binding action input
     * @return NormalizedNode representation of action input
     * @throws NullPointerException if any of the arguments is null
     */
    @Beta default @NonNull ContainerNode toNormalizedNodeActionInput(
            @NonNull final Class<? extends Action<?, ?, ?>> action, @NonNull final RpcInput input) {
        return toLazyNormalizedNodeActionInput(action,
            new NodeIdentifier(YangConstants.operationInputQName(BindingReflections.getQNameModule(action))), input)
                .getDelegate();
    }

    /**
     * Lazily translates supplied Binding action output into NormalizedNode data.
     *
     * @param action Binding action class
     * @param output Binding action output
     * @return NormalizedNode representation of action output
     */
    @Beta
    @NonNull BindingLazyContainerNode<RpcOutput> toLazyNormalizedNodeActionOutput(
            @NonNull Class<? extends Action<?, ?, ?>> action, @NonNull NodeIdentifier identifier,
                    @NonNull RpcOutput output);

    /**
     * Lazily translates supplied Binding action output into NormalizedNode data.
     *
     * @param action Binding action class
     * @param output Binding action output
     * @return NormalizedNode representation of action output
     */
    @Beta default @NonNull BindingLazyContainerNode<RpcOutput> toLazyNormalizedNodeActionOutput(
            @NonNull final Class<? extends Action<?, ?, ?>> action, @NonNull final RpcOutput output) {
        return toLazyNormalizedNodeActionOutput(action,
            new NodeIdentifier(YangConstants.operationInputQName(BindingReflections.getQNameModule(action))), output);
    }

    /**
     * Translates supplied Binding action output into NormalizedNode data.
     *
     * @param output Binding action output
     * @return NormalizedNode representation of action output
     */
    @Beta default @NonNull ContainerNode toNormalizedNodeActionOutput(
            @NonNull final Class<? extends Action<?, ?, ?>> action, @NonNull final RpcOutput output) {
        return toLazyNormalizedNodeActionOutput(action,
            new NodeIdentifier(YangConstants.operationInputQName(BindingReflections.getQNameModule(action))), output)
                .getDelegate();
    }
}
