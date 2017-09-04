/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.api;

import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * Serialization service, which provides two-way serialization between Java
 * Binding Data representation and NormalizedNode representation.
 */
public interface BindingNormalizedNodeSerializer {

    /**
     * Translates supplied Binding Instance Identifier into NormalizedNode
     * instance identifier.
     *
     * @param binding
     *            Binding Instance Identifier
     * @return DOM Instance Identifier
     * @throws IllegalArgumentException
     *             If supplied Instance Identifier is not valid.
     */
    YangInstanceIdentifier toYangInstanceIdentifier(@Nonnull InstanceIdentifier<?> binding);

    /**
     * Translates supplied YANG Instance Identifier into Binding instance
     * identifier.
     *
     * @param dom
     *            YANG Instance Identifier
     * @return Binding Instance Identifier, or null if the instance identifier
     *         is not representable.
     */
    @Nullable
    InstanceIdentifier<?> fromYangInstanceIdentifier(@Nonnull YangInstanceIdentifier dom);

    /**
     * Translates supplied Binding Instance Identifier and data into
     * NormalizedNode representation.
     *
     * @param path
     *            Binding Instance Identifier pointing to data
     * @param data
     *            Data object representing data
     * @return NormalizedNode representation
     * @throws IllegalArgumentException
     *             If supplied Instance Identifier is not valid.
     */
    <T extends DataObject> Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> toNormalizedNode(
            InstanceIdentifier<T> path, T data);

    /**
     * Translates supplied YANG Instance Identifier and NormalizedNode into
     * Binding data.
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
}
