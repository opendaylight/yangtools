/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
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
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DOMSourceAnyxmlNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.SystemLeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.SystemMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.UserLeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.builder.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.data.api.schema.builder.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.api.schema.builder.ListNodeBuilder;
import org.opendaylight.yangtools.yang.data.api.schema.builder.NormalizedNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableAnyXmlNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableAugmentationNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableChoiceNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafSetEntryNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafSetNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapEntryNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableUserLeafSetNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerLike;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

@Deprecated(forRemoval = true)
public final class SchemaAwareBuilders {
    private SchemaAwareBuilders() {
        // Hidden on purpose
    }

    public static NormalizedNodeBuilder<NodeIdentifier, DOMSource, DOMSourceAnyxmlNode> anyXmlBuilder(
            final AnyxmlSchemaNode schema) {
        return ImmutableAnyXmlNodeBuilder.create(schema);
    }

    public static <T> NormalizedNodeBuilder<NodeIdentifier, T, LeafNode<T>> leafBuilder(
            final LeafSchemaNode schema) {
        return ImmutableLeafNodeBuilder.create(schema);
    }

    public static <T> NormalizedNodeBuilder<NodeWithValue, T, LeafSetEntryNode<T>> leafSetEntryBuilder(
            final LeafListSchemaNode schema) {
        return ImmutableLeafSetEntryNodeBuilder.create(schema);
    }

    public static <T> ListNodeBuilder<T, UserLeafSetNode<T>> orderedLeafSetBuilder(final LeafListSchemaNode schema) {
        return ImmutableUserLeafSetNodeBuilder.create(schema);
    }

    public static <T> ListNodeBuilder<T, SystemLeafSetNode<T>> leafSetBuilder(final LeafListSchemaNode schema) {
        return ImmutableLeafSetNodeBuilder.create(schema);
    }

    public static <T> ListNodeBuilder<T, SystemLeafSetNode<T>> leafSetBuilder(final LeafListSchemaNode schema,
            final LeafSetNode<T> node) {
        return ImmutableLeafSetNodeBuilder.create(schema, node);
    }

    public static DataContainerNodeBuilder<NodeIdentifier, ContainerNode> containerBuilder(final ContainerLike schema) {
        return ImmutableContainerNodeBuilder.create(schema);
    }

    public static DataContainerNodeBuilder<NodeIdentifier, ContainerNode> containerBuilder(final ContainerLike schema,
            final ContainerNode node) {
        return ImmutableContainerNodeBuilder.create(schema, node);
    }

    public static DataContainerNodeBuilder<NodeIdentifierWithPredicates, MapEntryNode> mapEntryBuilder(
            final ListSchemaNode schema) {
        return ImmutableMapEntryNodeBuilder.create(schema);
    }

    public static CollectionNodeBuilder<MapEntryNode, SystemMapNode> mapBuilder(final ListSchemaNode schema) {
        return ImmutableMapNodeBuilder.create(schema);
    }

    public static CollectionNodeBuilder<MapEntryNode, SystemMapNode> mapBuilder(final ListSchemaNode schema,
            final MapNode node) {
        return ImmutableMapNodeBuilder.create(schema, node);
    }

    public static DataContainerNodeBuilder<AugmentationIdentifier, AugmentationNode> augmentationBuilder(
            final AugmentationSchemaNode schema) {
        return ImmutableAugmentationNodeBuilder.create(schema);
    }

    public static DataContainerNodeBuilder<NodeIdentifier, ChoiceNode> choiceBuilder(final ChoiceSchemaNode schema) {
        return ImmutableChoiceNodeBuilder.create(schema);
    }
}
