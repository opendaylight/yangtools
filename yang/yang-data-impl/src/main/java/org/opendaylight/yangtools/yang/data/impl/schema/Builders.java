/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.OrderedMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeAttrBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.ListNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeAttrBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableAugmentationNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableAugmentationNodeSchemaAwareBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableChoiceNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableChoiceNodeSchemaAwareBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeSchemaAwareBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafNodeSchemaAwareBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafSetEntryNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafSetEntryNodeSchemaAwareBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafSetNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafSetNodeSchemaAwareBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapEntryNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapEntryNodeSchemaAwareBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapNodeSchemaAwareBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableOrderedLeafSetNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableOrderedMapNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableUnkeyedListEntryNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableUnkeyedListNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

public final class Builders {

    private Builders() {
        throw new UnsupportedOperationException("Utilities class should not be instantiated");
    }

    public static <T> NormalizedNodeAttrBuilder<InstanceIdentifier.NodeIdentifier, T, LeafNode<T>> leafBuilder() {
        return ImmutableLeafNodeBuilder.create();
    }

    public static <T> NormalizedNodeAttrBuilder<InstanceIdentifier.NodeIdentifier, T, LeafNode<T>> leafBuilder(
            final LeafSchemaNode schema) {
        return ImmutableLeafNodeSchemaAwareBuilder.create(schema);
    }

    public static <T> NormalizedNodeAttrBuilder<InstanceIdentifier.NodeWithValue, T, LeafSetEntryNode<T>> leafSetEntryBuilder() {
        return ImmutableLeafSetEntryNodeBuilder.create();
    }

    public static <T> NormalizedNodeAttrBuilder<InstanceIdentifier.NodeWithValue, T, LeafSetEntryNode<T>> leafSetEntryBuilder(
            final LeafListSchemaNode schema) {
        return ImmutableLeafSetEntryNodeSchemaAwareBuilder.create(schema);
    }

    public static <T> ListNodeBuilder<T,LeafSetEntryNode<T>> leafSetBuilder() {
        return ImmutableLeafSetNodeBuilder.create();
    }

    public static <T> ListNodeBuilder<T,LeafSetEntryNode<T>>  orderedLeafSetBuilder() {
        return ImmutableOrderedLeafSetNodeBuilder.create();
    }

    public static <T> ListNodeBuilder<T,LeafSetEntryNode<T>> leafSetBuilder(final LeafSetNode<T> node) {
        return ImmutableLeafSetNodeBuilder.create(node);
    }

    public static <T> ListNodeBuilder<T,LeafSetEntryNode<T>> leafSetBuilder(final LeafListSchemaNode schema) {
        return ImmutableLeafSetNodeSchemaAwareBuilder.<T>create(schema);
    }

    public static <T> ListNodeBuilder<T,LeafSetEntryNode<T>> leafSetBuilder(final LeafListSchemaNode schema, final LeafSetNode<T> node) {
        return ImmutableLeafSetNodeSchemaAwareBuilder.<T>create(schema, node);
    }

    public static DataContainerNodeAttrBuilder<InstanceIdentifier.NodeIdentifier, ContainerNode> containerBuilder() {
        return ImmutableContainerNodeBuilder.create();
    }

    public static DataContainerNodeAttrBuilder<InstanceIdentifier.NodeIdentifier, ContainerNode> containerBuilder(final ContainerNode node) {
        return ImmutableContainerNodeBuilder.create(node);
    }

    public static DataContainerNodeAttrBuilder<InstanceIdentifier.NodeIdentifier, ContainerNode> containerBuilder(
            final ContainerSchemaNode schema) {
        return ImmutableContainerNodeSchemaAwareBuilder.create(schema);
    }

    public static DataContainerNodeAttrBuilder<InstanceIdentifier.NodeIdentifier, ContainerNode> containerBuilder(
            final ContainerSchemaNode schema, final ContainerNode node) {
        return ImmutableContainerNodeSchemaAwareBuilder.create(schema, node);
    }

    public static DataContainerNodeAttrBuilder<InstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> mapEntryBuilder() {
        return ImmutableMapEntryNodeBuilder.create();
    }

    public static DataContainerNodeAttrBuilder<InstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> mapEntryBuilder(
            final ListSchemaNode schema) {
        return ImmutableMapEntryNodeSchemaAwareBuilder.create(schema);
    }

    public static CollectionNodeBuilder<MapEntryNode, MapNode> mapBuilder() {
        return ImmutableMapNodeBuilder.create();
    }

    public static CollectionNodeBuilder<MapEntryNode, OrderedMapNode> orderedMapBuilder() {
        return ImmutableOrderedMapNodeBuilder.create();
    }

    public static CollectionNodeBuilder<UnkeyedListEntryNode, UnkeyedListNode> unkeyedListBuilder() {
        return ImmutableUnkeyedListNodeBuilder.create();
    }

    public static CollectionNodeBuilder<MapEntryNode, MapNode> mapBuilder(final MapNode node) {
        return ImmutableMapNodeBuilder.create(node);
    }

    public static CollectionNodeBuilder<MapEntryNode, MapNode> mapBuilder(final ListSchemaNode schema) {
        return ImmutableMapNodeSchemaAwareBuilder.create(schema);
    }

    public static CollectionNodeBuilder<MapEntryNode, MapNode> mapBuilder(final ListSchemaNode schema, final MapNode node) {
        return ImmutableMapNodeSchemaAwareBuilder.create(schema, node);
    }

    public static DataContainerNodeBuilder<InstanceIdentifier.AugmentationIdentifier, AugmentationNode> augmentationBuilder() {
        return ImmutableAugmentationNodeBuilder.create();
    }

    public static DataContainerNodeBuilder<InstanceIdentifier.AugmentationIdentifier, AugmentationNode> augmentationBuilder(final AugmentationSchema schema) {
        return ImmutableAugmentationNodeSchemaAwareBuilder.create(schema);
    }

    public static DataContainerNodeBuilder<InstanceIdentifier.NodeIdentifier, ChoiceNode> choiceBuilder() {
        return ImmutableChoiceNodeBuilder.create();
    }

    public static DataContainerNodeBuilder<InstanceIdentifier.NodeIdentifier, ChoiceNode> choiceBuilder(final org.opendaylight.yangtools.yang.model.api.ChoiceNode schema) {
        return ImmutableChoiceNodeSchemaAwareBuilder.create(schema);
    }

    public static DataContainerNodeAttrBuilder<InstanceIdentifier.NodeIdentifier, UnkeyedListEntryNode> unkeyedListEntryBuilder() {
        return ImmutableUnkeyedListEntryNodeBuilder.create();
    }

}
