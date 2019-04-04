/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Iterator;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.OrderedMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableChoiceNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapEntryNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableOrderedMapNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableUnkeyedListNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public final class ImmutableNodes {
    private static final NodeIdentifier SCHEMACONTEXT_NAME = NodeIdentifier.create(SchemaContext.NAME);

    private ImmutableNodes() {
        throw new UnsupportedOperationException("Utilities class should not be instantiated");
    }

    public static @NonNull CollectionNodeBuilder<MapEntryNode, MapNode> mapNodeBuilder() {
        return ImmutableMapNodeBuilder.create();
    }

    public static @NonNull CollectionNodeBuilder<MapEntryNode, MapNode> mapNodeBuilder(final QName name) {
        return mapNodeBuilder(NodeIdentifier.create(name));
    }

    public static @NonNull CollectionNodeBuilder<MapEntryNode, MapNode> mapNodeBuilder(final NodeIdentifier name) {
        return ImmutableMapNodeBuilder.create().withNodeIdentifier(name);
    }

    /**
     * Create an immutable map node.
     *
     * @param name QName which will be used as node identifier
     * @return An unordered Map node
     */
    public static @NonNull MapNode mapNode(final QName name) {
        return mapNode(NodeIdentifier.create(name));
    }

    /**
     * Create an immutable map node.
     *
     * @param name QName which will be used as node identifier
     * @return An unordered Map node
     */
    public static @NonNull MapNode mapNode(final NodeIdentifier name) {
        return mapNodeBuilder(name).build();
    }

    /**
     * Create immutable ordered map node.
     *
     * @param name QName which will be used as node identifier
     * @return An ordered Map node
     */
    public static @NonNull OrderedMapNode orderedMapNode(final QName name) {
        return orderedMapNode(NodeIdentifier.create(name));
    }

    /**
     * Create immutable ordered map node.
     *
     * @param name Node identifier
     * @return An ordered Map node
     */
    public static @NonNull OrderedMapNode orderedMapNode(final NodeIdentifier name) {
        return ImmutableOrderedMapNodeBuilder.create().withNodeIdentifier(name).build();
    }

    /**
     * Construct immutable leaf node.
     *
     * @param name Identifier of leaf node
     * @param value Value of leaf node
     * @param <T> Type of leaf node value
     * @return Leaf node with supplied identifier and value
     */
    public static <T> @NonNull LeafNode<T> leafNode(final NodeIdentifier name, final T value) {
        return ImmutableLeafNodeBuilder.<T>create()
                .withNodeIdentifier(name)
                .withValue(value)
                .build();
    }

    /**
     * Construct immutable leaf node.
     *
     * @param name QName which will be used as node identifier
     * @param value Value of leaf node.
     * @param <T> Type of leaf node value
     * @return Leaf node with supplied identifier and value
     */
    public static <T> @NonNull LeafNode<T> leafNode(final QName name, final T value) {
        return leafNode(NodeIdentifier.create(name), value);
    }

    public static @NonNull DataContainerNodeBuilder<NodeIdentifierWithPredicates, MapEntryNode> mapEntryBuilder(
            final QName nodeName, final QName keyName, final Object keyValue) {
        return ImmutableMapEntryNodeBuilder.create()
                .withNodeIdentifier(new NodeIdentifierWithPredicates(nodeName, keyName, keyValue))
                .withChild(leafNode(keyName, keyValue));
    }

    public static @NonNull DataContainerNodeBuilder<NodeIdentifierWithPredicates, MapEntryNode> mapEntryBuilder() {
        return ImmutableMapEntryNodeBuilder.create();
    }

    public static @NonNull MapEntryNode mapEntry(final QName nodeName, final QName keyName, final Object keyValue) {
        return mapEntryBuilder(nodeName, keyName, keyValue).build();
    }

    /**
     * Create an immutable container node.
     *
     * @param name QName which will be used as node identifier
     * @return A container node
     */
    public static @NonNull ContainerNode containerNode(final QName name) {
        return containerNode(NodeIdentifier.create(name));
    }

    /**
     * Create an immutable container node.
     *
     * @param name Node identifier
     * @return A container node
     */
    public static @NonNull ContainerNode containerNode(final NodeIdentifier name) {
        return ImmutableContainerNodeBuilder.create().withNodeIdentifier(name).build();
    }

    /**
     * Create an immutable choice node.
     *
     * @param name QName which will be used as node identifier
     * @return A choice node
     */
    public static @NonNull ChoiceNode choiceNode(final QName name) {
        return choiceNode(NodeIdentifier.create(name));
    }

    /**
     * Create an immutable choice node.
     *
     * @param name Node identifier
     * @return A choice node
     */
    public static @NonNull ChoiceNode choiceNode(final NodeIdentifier name) {
        return ImmutableChoiceNodeBuilder.create().withNodeIdentifier(name).build();
    }

    /**
     * Create an immutable list node.
     *
     * @param name QName which will be used as node identifier
     * @return An unkeyed list node
     */
    public static @NonNull UnkeyedListNode listNode(final QName name) {
        return listNode(NodeIdentifier.create(name));
    }

    /**
     * Create an immutable list node.
     *
     * @param name Node identifier
     * @return An unkeyed list node
     */
    public static @NonNull UnkeyedListNode listNode(final NodeIdentifier name) {
        return ImmutableUnkeyedListNodeBuilder.create().withNodeIdentifier(name).build();
    }

    /**
     * Convert YangInstanceIdentifier into a normalized node structure.
     *
     * @param ctx schema context to used during serialization
     * @param id instance identifier to convert to node structure starting from root
     * @return serialized normalized node for provided instance Id
     */
    public static @NonNull NormalizedNode<?, ?> fromInstanceId(final SchemaContext ctx,
            final YangInstanceIdentifier id) {
        return fromInstanceId(ctx, id, Optional.empty());
    }

    /**
     * Convert YangInstanceIdentifier into a normalized node structure.
     *
     * @param ctx schema context to used during serialization
     * @param id instance identifier to convert to node structure starting from root
     * @param deepestElement pre-built deepest child that will be inserted at the last path argument of provided
     *                       instance identifier
     * @return serialized normalized node for provided instance Id with overridden last child.
     */
    public static @NonNull NormalizedNode<?, ?> fromInstanceId(final SchemaContext ctx, final YangInstanceIdentifier id,
            final NormalizedNode<?, ?> deepestElement) {
        return fromInstanceId(ctx, id, Optional.of(deepestElement));
    }

    /**
     * Convert YangInstanceIdentifier into a normalized node structure.
     *
     * @param ctx schema context to used during serialization
     * @param id instance identifier to convert to node structure starting from root
     * @param deepestElement pre-built deepest child that will be inserted at the last path argument of provided
     *                       instance identifier
     * @return serialized normalized node for provided instance Id with (optionally) overridden last child
     *         and (optionally) marked with specific operation attribute.
     */
    public static @NonNull NormalizedNode<?, ?> fromInstanceId(final SchemaContext ctx, final YangInstanceIdentifier id,
            final Optional<NormalizedNode<?, ?>> deepestElement) {
        final PathArgument topLevelElement;
        final InstanceIdToNodes<?> instanceIdToNodes;
        final Iterator<PathArgument> it = id.getPathArguments().iterator();
        if (it.hasNext()) {
            topLevelElement = it.next();
            final DataSchemaNode dataChildByName = ctx.getDataChildByName(topLevelElement.getNodeType());
            checkNotNull(dataChildByName,
                "Cannot find %s node in schema context. Instance identifier has to start from root", topLevelElement);
            instanceIdToNodes = InstanceIdToNodes.fromSchemaAndQNameChecked(ctx, topLevelElement.getNodeType());
        } else {
            topLevelElement = SCHEMACONTEXT_NAME;
            instanceIdToNodes = InstanceIdToNodes.fromDataSchemaNode(ctx);
        }

        return instanceIdToNodes.create(topLevelElement, it, deepestElement);
    }
}
