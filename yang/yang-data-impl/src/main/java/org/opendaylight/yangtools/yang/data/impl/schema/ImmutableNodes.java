/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import com.google.common.base.Preconditions;
import java.util.Map;
import java.util.Optional;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.ModifyAction;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableChoiceNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapEntryNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public final class ImmutableNodes {

    private ImmutableNodes() {
        throw new UnsupportedOperationException("Utilities class should not be instantiated");
    }

    public static CollectionNodeBuilder<MapEntryNode, MapNode> mapNodeBuilder() {
        return ImmutableMapNodeBuilder.create();
    }

    public static CollectionNodeBuilder<MapEntryNode, MapNode> mapNodeBuilder(final QName name) {
        return ImmutableMapNodeBuilder.create().withNodeIdentifier(NodeIdentifier.create(name));
    }

    /**
     * Construct immutable leaf node
     *
     * @param name Identifier of leaf node
     * @param value Value of leaf node
     * @param <T> Type of leaf node value
     * @return Leaf node with supplied identifier and value
     */
    public static <T> LeafNode<T> leafNode(final NodeIdentifier name, final T value) {
        return ImmutableLeafNodeBuilder.<T>create()
                .withNodeIdentifier(name)
                .withValue(value)
                .build();
    }

    /**
     * Construct immutable leaf node
     *
     * @param name QName which will be used as node identifier
     * @param value Value of leaf node.
     * @param <T> Type of leaf node value
     * @return Leaf node with supplied identifier and value
     */
    public static <T> LeafNode<T> leafNode(final QName name,final T value) {
        return leafNode(NodeIdentifier.create(name), value);
    }

    public static DataContainerNodeBuilder<NodeIdentifierWithPredicates, MapEntryNode> mapEntryBuilder(final QName nodeName, final QName keyName, final Object keyValue) {
        return ImmutableMapEntryNodeBuilder.create()
                .withNodeIdentifier(new NodeIdentifierWithPredicates(nodeName, keyName, keyValue))
                .withChild(leafNode(keyName, keyValue));
    }

    public static MapEntryNode mapEntry(final QName nodeName,final QName keyName,final Object keyValue) {
        return mapEntryBuilder(nodeName, keyName, keyValue).build();
    }

    public static DataContainerNodeBuilder<NodeIdentifierWithPredicates, MapEntryNode> mapEntryBuilder() {
        return ImmutableMapEntryNodeBuilder.create();
    }

    public static ContainerNode containerNode(final QName name) {
        return ImmutableContainerNodeBuilder.create().withNodeIdentifier(NodeIdentifier.create(name)).build();
    }

    public static ChoiceNode choiceNode(final QName name) {
        return ImmutableChoiceNodeBuilder.create().withNodeIdentifier(NodeIdentifier.create(name)).build();
    }

    /**
     * Convert YangInstanceIdentifier into a normalized node structure
     *
     * @param ctx schema context to used during serialization
     * @param id instance identifier to convert to node structure starting from root
     * @return serialized normalized node for provided instance Id
     */
    public static NormalizedNode<?, ?> fromInstanceId(final SchemaContext ctx, final YangInstanceIdentifier id) {
        return fromInstanceId(ctx, id, Optional.empty(), Optional.empty());
    }

    /**
     * Convert YangInstanceIdentifier into a normalized node structure
     *
     * @param ctx schema context to used during serialization
     * @param id instance identifier to convert to node structure starting from root
     * @param deepestElement pre-built deepest child that will be inserted at the last path argument of provided instance Id
     * @return serialized normalized node for provided instance Id with overridden last child.
     */
    public static NormalizedNode<?, ?> fromInstanceId(final SchemaContext ctx, final YangInstanceIdentifier id, final NormalizedNode<?, ?> deepestElement) {
        return fromInstanceId(ctx, id, Optional.of(deepestElement), Optional.empty());
    }

    /**
     * Convert YangInstanceIdentifier into a normalized node structure
     *
     * @param ctx schema context to used during serialization
     * @param id instance identifier to convert to node structure starting from root
     * @param deepestElement pre-built deepest child that will be inserted at the last path argument of provided instance Id
     * @param operation modify operation attribute to be added to the deepest child. QName is the operation attribute key and ModifyAction is the value.
     * @return serialized normalized node for provided instance Id with (optionally) overridden last child and (optionally) marked with specific operation attribute.
     */
    public static NormalizedNode<?, ?> fromInstanceId(final SchemaContext ctx, final YangInstanceIdentifier id, final Optional<NormalizedNode<?, ?>> deepestElement, final Optional<Map.Entry<QName, ModifyAction>> operation) {
        final YangInstanceIdentifier.PathArgument topLevelElement = id.getPathArguments().get(0);
        final DataSchemaNode dataChildByName = ctx.getDataChildByName(topLevelElement.getNodeType());
        Preconditions.checkNotNull(dataChildByName, "Cannot find %s node in schema context. Instance identifier has to start from root", topLevelElement);
        final InstanceIdToNodes<?> instanceIdToNodes = InstanceIdToNodes.fromSchemaAndQNameChecked(ctx, topLevelElement.getNodeType());
        return instanceIdToNodes.create(id, deepestElement, operation);
    }
}
