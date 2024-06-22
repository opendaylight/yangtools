/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.time.Instant;
import java.util.Map.Entry;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.Action;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.BaseNotification;
import org.opendaylight.yangtools.binding.BindingInstanceIdentifier;
import org.opendaylight.yangtools.binding.DataContainer;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.binding.Notification;
import org.opendaylight.yangtools.binding.RpcInput;
import org.opendaylight.yangtools.binding.RpcOutput;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

/**
 * Serialization service, which provides two-way serialization between Java Binding Data representation and
 * NormalizedNode representation.
 */
public interface BindingNormalizedNodeSerializer {
    /**
     * Result of a {@link BindingNormalizedNodeSerializer#toNormalizedNode(DataObjectReference, DataObject)}. Since the
     * Binding {@link Augmentation} does not have an exact equivalent, there are two specializations of this class:
     * {@link NodeResult} and {@link AugmentationResult}.
     */
    sealed interface NormalizedResult {
        /**
         * Return the {@link YangInstanceIdentifier} path of this result.
         *
         * @return A {@link YangInstanceIdentifier}
         */
        @NonNull YangInstanceIdentifier path();
    }

    /**
     * A {@link NormalizedResult} for an {@link Augmentation}.
     *
     * @param path A YangInstanceIdentifier identifying the parent of this augmentation
     * @param possibleChildren {@link NodeIdentifier}s of each possible child
     * @param children Augmentation children
     */
    record AugmentationResult(
            @NonNull YangInstanceIdentifier path,
            @NonNull ImmutableSet<NodeIdentifier> possibleChildren,
            @NonNull ImmutableList<DataContainerChild> children) implements NormalizedResult {
        public AugmentationResult {
            requireNonNull(path);
            requireNonNull(possibleChildren);
            requireNonNull(children);
        }
    }

    record NodeResult(@NonNull YangInstanceIdentifier path, @NonNull NormalizedNode node) implements NormalizedResult {
        public NodeResult {
            requireNonNull(path);
            requireNonNull(node);
        }
    }

    /**
     * Translates supplied Binding Instance Identifier into NormalizedNode instance identifier.
     *
     * @param binding Binding Instance Identifier
     * @return DOM Instance Identifier
     * @throws IllegalArgumentException If supplied Instance Identifier is not valid.
     */
    // FIXME: MDSAL-525: reconcile this with BindingInstanceIdentifierCodec
    @NonNull YangInstanceIdentifier toYangInstanceIdentifier(@NonNull BindingInstanceIdentifier binding);

    /**
     * Translates supplied YANG Instance Identifier into Binding instance identifier.
     *
     * @param dom YANG Instance Identifier
     * @return Binding Instance Identifier, or null if the instance identifier is not representable.
     */
    // FIXME: MDSAL-525: reconcile this with BindingInstanceIdentifierCodec
    <T extends DataObject> @Nullable DataObjectReference<T> fromYangInstanceIdentifier(
            @NonNull YangInstanceIdentifier dom);

    /**
     * Translates supplied Binding Instance Identifier and data into NormalizedNode representation.
     *
     * @param path Binding Instance Identifier pointing to data
     * @param data Data object representing data
     * @return {@link NormalizedResult} representation
     * @throws IllegalArgumentException If supplied Instance Identifier is not valid.
     */
    <T extends DataObject> @NonNull NormalizedResult toNormalizedNode(DataObjectReference<T> path, T data);

    /**
     * Translates supplied Binding Instance Identifier and data into NormalizedNode representation.
     *
     * @param path Binding Instance Identifier pointing to data
     * @param data Data object representing data
     * @return {@link NormalizedResult} representation
     * @throws IllegalArgumentException If supplied Instance Identifier is not valid.
     */
    <A extends Augmentation<?>> @NonNull AugmentationResult toNormalizedAugmentation(DataObjectReference<A> path,
        A data);

    /**
     * Translates supplied Binding Instance Identifier and data into NormalizedNode representation.
     *
     * @param path Binding Instance Identifier pointing to data
     * @param data Data object representing data
     * @return {@link NormalizedResult} representation
     * @throws IllegalArgumentException If supplied Instance Identifier is not valid.
     */
    <T extends DataObject> @NonNull NodeResult toNormalizedDataObject(DataObjectReference<T> path, T data);

    /**
     * Translates supplied YANG Instance Identifier and NormalizedNode into Binding data.
     *
     * @param path Binding Instance Identifier
     * @param data NormalizedNode representing data
     * @return DOM Instance Identifier
     */
    @Nullable Entry<DataObjectReference<?>, DataObject> fromNormalizedNode(@NonNull YangInstanceIdentifier path,
            NormalizedNode data);

    /**
     * Translates supplied NormalizedNode Notification into Binding data.
     *
     * @param path Schema Path of Notification, schema path is absolute, and consists of Notification QName.
     * @param data NormalizedNode representing data
     * @return Binding representation of Notification
     */
    @NonNull BaseNotification fromNormalizedNodeNotification(@NonNull Absolute path, @NonNull ContainerNode data);

    /**
     * Translates supplied NormalizedNode Notification into Binding data, optionally taking an instant
     * when the notification was generated.
     *
     * @param path Schema Path of Notification, schema path is absolute, and consists of Notification QName.
     * @param data NormalizedNode representing data
     * @param eventInstant optional instant when the event was generated
     * @return Binding representation of Notification
     */
    @Beta
    @NonNull BaseNotification fromNormalizedNodeNotification(@NonNull Absolute path, @NonNull ContainerNode data,
            @Nullable Instant eventInstant);

    /**
     * Translates supplied NormalizedNode RPC input or output into Binding data.
     *
     * @param containerPath Container path (RPC type + input/output)
     * @param data NormalizedNode representing data
     * @return Binding representation of RPC data
     */
    @Nullable DataObject fromNormalizedNodeRpcData(@NonNull Absolute containerPath, @NonNull ContainerNode data);

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
     * @param data {@link Notification} representing notification data
     * @return NormalizedNode representation of notification
     */
    @NonNull ContainerNode toNormalizedNodeNotification(@NonNull Notification<?> data);

    /**
     * Translates supplied Binding Notification or output into NormalizedNode notification.
     *
     * @param path schema node identifier of the notification
     * @param data {@link BaseNotification} representing notification data
     * @return NormalizedNode representation of notification
     */
    @NonNull ContainerNode toNormalizedNodeNotification(@NonNull Absolute path, @NonNull BaseNotification data);

    /**
     * Translates supplied Binding RPC input or output into NormalizedNode data.
     *
     * @param data NormalizedNode representing rpc data
     * @return NormalizedNode representation of rpc data
     */
    @NonNull ContainerNode toNormalizedNodeRpcData(@NonNull DataContainer data);

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
    @Beta
    @NonNull BindingLazyContainerNode<RpcInput> toLazyNormalizedNodeActionInput(
        @NonNull Class<? extends Action<?, ?, ?>> action, @NonNull RpcInput input);

    /**
     * Translates supplied Binding action input into NormalizedNode data.
     *
     * @param action Binding action class
     * @param input Binding action input
     * @return NormalizedNode representation of action input
     * @throws NullPointerException if any of the arguments is null
     */
    @Beta
    @NonNull ContainerNode toNormalizedNodeActionInput(@NonNull Class<? extends Action<?, ?, ?>> action,
        @NonNull RpcInput input);

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
    @Beta
    @NonNull BindingLazyContainerNode<RpcOutput> toLazyNormalizedNodeActionOutput(
            @NonNull Class<? extends Action<?, ?, ?>> action, @NonNull RpcOutput output);

    /**
     * Translates supplied Binding action output into NormalizedNode data.
     *
     * @param output Binding action output
     * @return NormalizedNode representation of action output
     */
    @Beta
    @NonNull ContainerNode toNormalizedNodeActionOutput(@NonNull Class<? extends Action<?, ?, ?>> action,
        @NonNull RpcOutput output);
}
