/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.*;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.*;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.*;
import org.opendaylight.yangtools.yang.model.api.*;

public class Builders {

    public static <T> NormalizedNodeAttrBuilder<InstanceIdentifier.NodeIdentifier, T, LeafNode<T>> leafBuilder() {
        return ImmutableLeafNodeBuilder.create();
    }

    public static <T> NormalizedNodeAttrBuilder<InstanceIdentifier.NodeIdentifier, T, LeafNode<T>> leafBuilder(
            LeafSchemaNode schema) {
        return ImmutableLeafNodeSchemaAwareBuilder.create(schema);
    }

    public static <T> NormalizedNodeAttrBuilder<InstanceIdentifier.NodeWithValue, T, LeafSetEntryNode<T>> leafSetEntryBuilder() {
        return ImmutableLeafSetEntryNodeBuilder.create();
    }

    public static <T> NormalizedNodeAttrBuilder<InstanceIdentifier.NodeWithValue, T, LeafSetEntryNode<T>> leafSetEntryBuilder(
            LeafListSchemaNode schema) {
        return ImmutableLeafSetEntryNodeSchemaAwareBuilder.create(schema);
    }

    public static <T> ListNodeBuilder<T, LeafSetEntryNode<T>> leafSetBuilder() {
        return ImmutableLeafSetNodeBuilder.create();
    }

    public static <T> ListNodeBuilder<T, LeafSetEntryNode<T>> leafSetBuilder(LeafListSchemaNode schema) {
        return ImmutableLeafSetNodeSchemaAwareBuilder.create(schema);
    }

    public static DataContainerNodeAttrBuilder<InstanceIdentifier.NodeIdentifier, ContainerNode> containerBuilder() {
        return ImmutableContainerNodeBuilder.create();
    }

    public static DataContainerNodeAttrBuilder<InstanceIdentifier.NodeIdentifier, ContainerNode> containerBuilder(
            ContainerSchemaNode schema) {
        return ImmutableContainerNodeSchemaAwareBuilder.create(schema);
    }

    public static DataContainerNodeAttrBuilder<InstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> mapEntryBuilder() {
        return ImmutableMapEntryNodeBuilder.create();
    }

    public static DataContainerNodeAttrBuilder<InstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> mapEntryBuilder(
            ListSchemaNode schema) {
        return ImmutableMapEntryNodeSchemaAwareBuilder.create(schema);
    }

    public static CollectionNodeBuilder<MapEntryNode, MapNode> mapBuilder() {
        return ImmutableMapNodeBuilder.create();
    }

    public static CollectionNodeBuilder<MapEntryNode, MapNode> mapBuilder(ListSchemaNode schema) {
        return ImmutableMapNodeSchemaAwareBuilder.create(schema);
    }

    public static DataContainerNodeBuilder<InstanceIdentifier.AugmentationIdentifier, AugmentationNode> augmentationBuilder() {
        return ImmutableAugmentationNodeBuilder.create();
    }

    public static DataContainerNodeBuilder<InstanceIdentifier.AugmentationIdentifier, AugmentationNode> augmentationBuilder(AugmentationSchema schema) {
        return ImmutableAugmentationNodeSchemaAwareBuilder.create(schema);
    }

    public static DataContainerNodeBuilder<InstanceIdentifier.NodeIdentifier, ChoiceNode> choiceBuilder() {
        return ImmutableChoiceNodeBuilder.create();
    }

    public static DataContainerNodeBuilder<InstanceIdentifier.NodeIdentifier, ChoiceNode> choiceBuilder(org.opendaylight.yangtools.yang.model.api.ChoiceNode schema) {
        return ImmutableChoiceNodeSchemaAwareBuilder.create(schema);
    }

}
