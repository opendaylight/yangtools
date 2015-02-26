/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapEntryNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapNodeBuilder;

public final class ImmutableNodes {

    private ImmutableNodes() {
        throw new UnsupportedOperationException("Utilities class should not be instantiated");
    }

    public static CollectionNodeBuilder<MapEntryNode, MapNode> mapNodeBuilder() {
        return ImmutableMapNodeBuilder.create();
    }

    public static CollectionNodeBuilder<MapEntryNode, MapNode> mapNodeBuilder(final QName name) {
        return ImmutableMapNodeBuilder.create().withNodeIdentifier(new NodeIdentifier(name));
    }

    /**
     * Construct immutable leaf node
     *
     * @param name Identifier of leaf node
     * @param value Value of leaf node
     * @return Leaf node with supplied identifier and value
     */
    public static <T> LeafNode<T> leafNode(final NodeIdentifier name,final T value) {
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
     * @return Leaf node with supplied identifier and value
     */
    public static <T> LeafNode<T> leafNode(final QName name,final T value) {
        return leafNode(new NodeIdentifier(name), value);
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
        return ImmutableContainerNodeBuilder.create().withNodeIdentifier(new NodeIdentifier(name)).build();
    }

}
