/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import javax.xml.transform.dom.DOMSource;
import org.opendaylight.yangtools.odlext.model.api.YangModeledAnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.AnydataNode;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DOMSourceAnyxmlNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.OrderedMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.api.schema.YangModeledAnyXmlNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.ListNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableAnyXmlNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableAnyXmlNodeSchemaAwareBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableAnydataNodeBuilder;
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
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableOrderedLeafSetNodeSchemaAwareBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableOrderedMapNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableOrderedMapNodeSchemaAwareBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableUnkeyedListEntryNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableUnkeyedListNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableYangModeledAnyXmlNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerLike;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

public final class Builders {
    private Builders() {
        // Hidden on purpose
    }

    public static <T> NormalizedNodeBuilder<NodeIdentifier, T, LeafNode<T>> leafBuilder() {
        return ImmutableLeafNodeBuilder.create();
    }

    public static <T> NormalizedNodeBuilder<NodeIdentifier, T, LeafNode<T>> leafBuilder(
            final LeafSchemaNode schema) {
        return ImmutableLeafNodeSchemaAwareBuilder.create(schema);
    }

    public static <T> NormalizedNodeBuilder<NodeWithValue, T, LeafSetEntryNode<T>> leafSetEntryBuilder() {
        return ImmutableLeafSetEntryNodeBuilder.create();
    }

    public static <T> NormalizedNodeBuilder<NodeWithValue, T, LeafSetEntryNode<T>> leafSetEntryBuilder(
            final LeafListSchemaNode schema) {
        return ImmutableLeafSetEntryNodeSchemaAwareBuilder.create(schema);
    }

    public static NormalizedNodeBuilder<NodeIdentifier, DOMSource, DOMSourceAnyxmlNode> anyXmlBuilder() {
        return ImmutableAnyXmlNodeBuilder.create();
    }

    public static NormalizedNodeBuilder<NodeIdentifier, DOMSource, DOMSourceAnyxmlNode> anyXmlBuilder(
            final AnyxmlSchemaNode schema) {
        return ImmutableAnyXmlNodeSchemaAwareBuilder.create(schema);
    }

    public static DataContainerNodeBuilder<NodeIdentifier, YangModeledAnyXmlNode> yangModeledAnyXmlBuilder(
            final YangModeledAnyxmlSchemaNode schema) {
        return ImmutableYangModeledAnyXmlNodeBuilder.create(schema);
    }

    public static <T> NormalizedNodeBuilder<NodeIdentifier, T, AnydataNode<T>> anydataBuilder(
            final Class<T> objectModel) {
        return ImmutableAnydataNodeBuilder.create(objectModel);
    }

    public static <T> ListNodeBuilder<T, LeafSetEntryNode<T>> orderedLeafSetBuilder() {
        return ImmutableOrderedLeafSetNodeBuilder.create();
    }

    public static <T> ListNodeBuilder<T, LeafSetEntryNode<T>> orderedLeafSetBuilder(final LeafListSchemaNode schema) {
        return ImmutableOrderedLeafSetNodeSchemaAwareBuilder.create(schema);
    }

    public static <T> ListNodeBuilder<T, LeafSetEntryNode<T>> leafSetBuilder() {
        return ImmutableLeafSetNodeBuilder.create();
    }

    public static <T> ListNodeBuilder<T, LeafSetEntryNode<T>> leafSetBuilder(final LeafSetNode<T> node) {
        return ImmutableLeafSetNodeBuilder.create(node);
    }

    public static <T> ListNodeBuilder<T, LeafSetEntryNode<T>> leafSetBuilder(final LeafListSchemaNode schema) {
        return ImmutableLeafSetNodeSchemaAwareBuilder.create(schema);
    }

    public static <T> ListNodeBuilder<T, LeafSetEntryNode<T>> leafSetBuilder(final LeafListSchemaNode schema,
            final LeafSetNode<T> node) {
        return ImmutableLeafSetNodeSchemaAwareBuilder.create(schema, node);
    }

    public static DataContainerNodeBuilder<NodeIdentifier, ContainerNode> containerBuilder() {
        return ImmutableContainerNodeBuilder.create();
    }

    public static DataContainerNodeBuilder<NodeIdentifier, ContainerNode> containerBuilder(
            final ContainerNode node) {
        return ImmutableContainerNodeBuilder.create(node);
    }

    public static DataContainerNodeBuilder<NodeIdentifier, ContainerNode> containerBuilder(final ContainerLike schema) {
        return ImmutableContainerNodeSchemaAwareBuilder.create(schema);
    }

    public static DataContainerNodeBuilder<NodeIdentifier, ContainerNode> containerBuilder(final ContainerLike schema,
            final ContainerNode node) {
        return ImmutableContainerNodeSchemaAwareBuilder.create(schema, node);
    }

    public static DataContainerNodeBuilder<NodeIdentifierWithPredicates, MapEntryNode> mapEntryBuilder() {
        return ImmutableMapEntryNodeBuilder.create();
    }

    public static DataContainerNodeBuilder<NodeIdentifierWithPredicates, MapEntryNode> mapEntryBuilder(
            final MapEntryNode mapEntryNode) {
        return ImmutableMapEntryNodeBuilder.create(mapEntryNode);
    }

    public static DataContainerNodeBuilder<NodeIdentifierWithPredicates, MapEntryNode> mapEntryBuilder(
            final ListSchemaNode schema) {
        return ImmutableMapEntryNodeSchemaAwareBuilder.create(schema);
    }

    public static CollectionNodeBuilder<MapEntryNode, OrderedMapNode> orderedMapBuilder() {
        return ImmutableOrderedMapNodeBuilder.create();
    }

    public static CollectionNodeBuilder<MapEntryNode, OrderedMapNode> orderedMapBuilder(final ListSchemaNode schema) {
        return ImmutableOrderedMapNodeSchemaAwareBuilder.create(schema);
    }

    public static CollectionNodeBuilder<UnkeyedListEntryNode, UnkeyedListNode> unkeyedListBuilder() {
        return ImmutableUnkeyedListNodeBuilder.create();
    }

    public static CollectionNodeBuilder<MapEntryNode, MapNode> mapBuilder() {
        return ImmutableMapNodeBuilder.create();
    }

    public static CollectionNodeBuilder<MapEntryNode, MapNode> mapBuilder(final MapNode node) {
        return ImmutableMapNodeBuilder.create(node);
    }

    public static CollectionNodeBuilder<MapEntryNode, MapNode> mapBuilder(final ListSchemaNode schema) {
        return ImmutableMapNodeSchemaAwareBuilder.create(schema);
    }

    public static CollectionNodeBuilder<MapEntryNode, MapNode> mapBuilder(final ListSchemaNode schema,
            final MapNode node) {
        return ImmutableMapNodeSchemaAwareBuilder.create(schema, node);
    }

    public static DataContainerNodeBuilder<AugmentationIdentifier, AugmentationNode> augmentationBuilder() {
        return ImmutableAugmentationNodeBuilder.create();
    }

    public static DataContainerNodeBuilder<AugmentationIdentifier, AugmentationNode> augmentationBuilder(
            final AugmentationSchemaNode schema) {
        return ImmutableAugmentationNodeSchemaAwareBuilder.create(schema);
    }

    public static DataContainerNodeBuilder<NodeIdentifier, ChoiceNode> choiceBuilder() {
        return ImmutableChoiceNodeBuilder.create();
    }

    public static DataContainerNodeBuilder<NodeIdentifier, ChoiceNode> choiceBuilder(final ChoiceSchemaNode schema) {
        return ImmutableChoiceNodeSchemaAwareBuilder.create(schema);
    }

    public static DataContainerNodeBuilder<NodeIdentifier, UnkeyedListEntryNode> unkeyedListEntryBuilder() {
        return ImmutableUnkeyedListEntryNodeBuilder.create();
    }
}
