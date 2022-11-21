/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import javax.xml.transform.dom.DOMSource;
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
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.SystemLeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.SystemMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.api.schema.UserLeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.UserMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.builder.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.data.api.schema.builder.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.api.schema.builder.ListNodeBuilder;
import org.opendaylight.yangtools.yang.data.api.schema.builder.NormalizedNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableAnyXmlNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableAnydataNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableAugmentationNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableChoiceNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafSetEntryNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafSetNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapEntryNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableUnkeyedListEntryNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableUnkeyedListNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableUserLeafSetNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableUserMapNodeBuilder;

public final class Builders {
    private Builders() {
        // Hidden on purpose
    }

    public static <T> NormalizedNodeBuilder<NodeIdentifier, T, LeafNode<T>> leafBuilder() {
        return ImmutableLeafNodeBuilder.create();
    }

    // FIXME: 7.0.0: add generic arguments
    public static <T> NormalizedNodeBuilder<NodeWithValue, T, LeafSetEntryNode<T>> leafSetEntryBuilder() {
        return ImmutableLeafSetEntryNodeBuilder.create();
    }

    public static NormalizedNodeBuilder<NodeIdentifier, DOMSource, DOMSourceAnyxmlNode> anyXmlBuilder() {
        return ImmutableAnyXmlNodeBuilder.create();
    }

    public static <T> NormalizedNodeBuilder<NodeIdentifier, T, AnydataNode<T>> anydataBuilder(
            final Class<T> objectModel) {
        return ImmutableAnydataNodeBuilder.create(objectModel);
    }

    public static <T> ListNodeBuilder<T, UserLeafSetNode<T>> orderedLeafSetBuilder() {
        return ImmutableUserLeafSetNodeBuilder.create();
    }

    public static <T> ListNodeBuilder<T, UserLeafSetNode<T>> orderedLeafSetBuilder(final UserLeafSetNode<T> node) {
        return ImmutableUserLeafSetNodeBuilder.create(node);
    }

    public static <T> ListNodeBuilder<T, SystemLeafSetNode<T>> leafSetBuilder() {
        return ImmutableLeafSetNodeBuilder.create();
    }

    public static <T> ListNodeBuilder<T, SystemLeafSetNode<T>> leafSetBuilder(final SystemLeafSetNode<T> node) {
        return ImmutableLeafSetNodeBuilder.create(node);
    }

    public static <T> ListNodeBuilder<T, SystemLeafSetNode<T>> leafSetBuilder(final int sizeHint) {
        return ImmutableLeafSetNodeBuilder.create(sizeHint);
    }

    public static DataContainerNodeBuilder<NodeIdentifier, ContainerNode> containerBuilder() {
        return ImmutableContainerNodeBuilder.create();
    }

    public static DataContainerNodeBuilder<NodeIdentifier, ContainerNode> containerBuilder(
            final ContainerNode node) {
        return ImmutableContainerNodeBuilder.create(node);
    }

    public static DataContainerNodeBuilder<NodeIdentifier, ContainerNode> containerBuilder(final int sizeHint) {
        return ImmutableContainerNodeBuilder.create(sizeHint);
    }

    public static DataContainerNodeBuilder<NodeIdentifierWithPredicates, MapEntryNode> mapEntryBuilder() {
        return ImmutableMapEntryNodeBuilder.create();
    }

    public static DataContainerNodeBuilder<NodeIdentifierWithPredicates, MapEntryNode> mapEntryBuilder(
            final MapEntryNode mapEntryNode) {
        return ImmutableMapEntryNodeBuilder.create(mapEntryNode);
    }

    public static DataContainerNodeBuilder<NodeIdentifierWithPredicates, MapEntryNode> mapEntryBuilder(
            final int sizeHint) {
        return ImmutableMapEntryNodeBuilder.create(sizeHint);
    }

    public static CollectionNodeBuilder<MapEntryNode, UserMapNode> orderedMapBuilder() {
        return ImmutableUserMapNodeBuilder.create();
    }

    public static CollectionNodeBuilder<MapEntryNode, UserMapNode> orderedMapBuilder(final int sizeHint) {
        return ImmutableUserMapNodeBuilder.create(sizeHint);
    }

    public static CollectionNodeBuilder<UnkeyedListEntryNode, UnkeyedListNode> unkeyedListBuilder() {
        return ImmutableUnkeyedListNodeBuilder.create();
    }

    public static CollectionNodeBuilder<UnkeyedListEntryNode, UnkeyedListNode> unkeyedListBuilder(final int sizeHint) {
        return ImmutableUnkeyedListNodeBuilder.create(sizeHint);
    }

    public static CollectionNodeBuilder<MapEntryNode, SystemMapNode> mapBuilder() {
        return ImmutableMapNodeBuilder.create();
    }

    public static CollectionNodeBuilder<MapEntryNode, SystemMapNode> mapBuilder(final SystemMapNode node) {
        return ImmutableMapNodeBuilder.create(node);
    }

    public static CollectionNodeBuilder<MapEntryNode, SystemMapNode> mapBuilder(final int sizeHint) {
        return ImmutableMapNodeBuilder.create(sizeHint);
    }

    public static DataContainerNodeBuilder<AugmentationIdentifier, AugmentationNode> augmentationBuilder() {
        return ImmutableAugmentationNodeBuilder.create();
    }

    public static DataContainerNodeBuilder<AugmentationIdentifier, AugmentationNode> augmentationBuilder(
            final int sizeHint) {
        return ImmutableAugmentationNodeBuilder.create(sizeHint);
    }

    public static DataContainerNodeBuilder<NodeIdentifier, ChoiceNode> choiceBuilder() {
        return ImmutableChoiceNodeBuilder.create();
    }

    public static DataContainerNodeBuilder<NodeIdentifier, ChoiceNode> choiceBuilder(final int sizeHint) {
        return ImmutableChoiceNodeBuilder.create(sizeHint);
    }

    public static DataContainerNodeBuilder<NodeIdentifier, UnkeyedListEntryNode> unkeyedListEntryBuilder() {
        return ImmutableUnkeyedListEntryNodeBuilder.create();
    }

    public static DataContainerNodeBuilder<NodeIdentifier, UnkeyedListEntryNode> unkeyedListEntryBuilder(
            final int sizeHint) {
        return ImmutableUnkeyedListEntryNodeBuilder.create(sizeHint);
    }
}
