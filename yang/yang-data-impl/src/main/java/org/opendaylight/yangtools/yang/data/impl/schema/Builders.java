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
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.ListNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.*;
import org.opendaylight.yangtools.yang.model.api.*;

public class Builders {

    public static <T> NormalizedNodeBuilder<InstanceIdentifier.NodeIdentifier, T, LeafNode<T>> leafBuilder() {
        return ImmutableLeafNodeBuilder.get();
    }

    public static <T> NormalizedNodeBuilder<InstanceIdentifier.NodeIdentifier, T, LeafNode<T>> leafBuilder(
            LeafSchemaNode schema) {
        return ImmutableLeafNodeSchemaAwareBuilder.get(schema);
    }

    public static <T> NormalizedNodeBuilder<InstanceIdentifier.NodeWithValue, T, LeafSetEntryNode<T>> leafSetEntryBuilder() {
        return ImmutableLeafSetEntryNodeBuilder.get();
    }

    public static <T> NormalizedNodeBuilder<InstanceIdentifier.NodeWithValue, T, LeafSetEntryNode<T>> leafSetEntryBuilder(
            LeafListSchemaNode schema) {
        return ImmutableLeafSetEntryNodeSchemaAwareBuilder.get(schema);
    }

    public static <T> ListNodeBuilder<T, LeafSetEntryNode<T>> leafSetBuilder() {
        return ImmutableLeafSetNodeBuilder.get();
    }

    public static <T> ListNodeBuilder<T, LeafSetEntryNode<T>> leafSetBuilder(LeafListSchemaNode schema) {
        return ImmutableLeafSetNodeSchemaAwareBuilder.get(schema);
    }

    public static DataContainerNodeBuilder<InstanceIdentifier.NodeIdentifier, ContainerNode> containerBuilder() {
        return ImmutableContainerNodeBuilder.get();
    }

    public static DataContainerNodeBuilder<InstanceIdentifier.NodeIdentifier, ContainerNode> containerBuilder(
            ContainerSchemaNode schema) {
        return ImmutableContainerNodeSchemaAwareBuilder.get(schema);
    }

    public static DataContainerNodeBuilder<InstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> mapEntryBuilder() {
        return ImmutableMapEntryNodeBuilder.get();
    }

    public static DataContainerNodeBuilder<InstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> mapEntryBuilder(
            ListSchemaNode schema) {
        return ImmutableMapEntryNodeSchemaAwareBuilder.get(schema);
    }

    public static CollectionNodeBuilder<MapEntryNode, MapNode> mapBuilder() {
        return ImmutableMapNodeBuilder.get();
    }

    public static CollectionNodeBuilder<MapEntryNode, MapNode> mapBuilder(ListSchemaNode schema) {
        return ImmutableMapNodeSchemaAwareBuilder.get(schema);
    }

    public static DataContainerNodeBuilder<InstanceIdentifier.AugmentationIdentifier, AugmentationNode> augmentationBuilder() {
        return ImmutableAugmentationNodeBuilder.get();
    }

    public static DataContainerNodeBuilder<InstanceIdentifier.AugmentationIdentifier, AugmentationNode> augmentationBuilder(AugmentationSchema schema) {
        return ImmutableAugmentationNodeSchemaAwareBuilder.get(schema);
    }

    public static DataContainerNodeBuilder<InstanceIdentifier.NodeIdentifier, ChoiceNode> choiceBuilder() {
        return ImmutableChoiceNodeBuilder.get();
    }

    public static DataContainerNodeBuilder<InstanceIdentifier.NodeIdentifier, ChoiceNode> choiceBuilder(org.opendaylight.yangtools.yang.model.api.ChoiceNode schema) {
        return ImmutableChoiceNodeSchemaAwareBuilder.get(schema);
    }

}
