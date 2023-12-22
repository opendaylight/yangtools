/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import java.io.IOException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode.BuilderFactory;
import org.opendaylight.yangtools.yang.data.api.schema.SystemMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.api.schema.UserMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.YangInstanceIdentifierWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public final class ImmutableNodes {
    private static final @NonNull ImmutableBuilderFactory BUILDER_FACTORY =
        new ImmutableBuilderFactory();

    // FIXME: YANGTOOLS-1074: we do not want this name
    private static final NodeIdentifier SCHEMACONTEXT_NAME = NodeIdentifier.create(SchemaContext.NAME);

    private ImmutableNodes() {
        // Hidden on purpose
    }

    public static @NonNull BuilderFactory builderFactory() {
        return BUILDER_FACTORY;
    }

    public static SystemMapNode.@NonNull Builder mapNodeBuilder() {
        return BUILDER_FACTORY.newSystemMapBuilder();
    }

    public static SystemMapNode.@NonNull Builder mapNodeBuilder(final QName name) {
        return mapNodeBuilder(NodeIdentifier.create(name));
    }

    public static SystemMapNode.@NonNull Builder mapNodeBuilder(final NodeIdentifier name) {
        final var ret = mapNodeBuilder();
        // FIXME: use fluent once we have specialized enough
        ret.withNodeIdentifier(name);
        return ret;
    }

    /**
     * Create an immutable map node.
     *
     * @param name QName which will be used as node identifier
     * @return An unordered Map node
     */
    public static @NonNull SystemMapNode mapNode(final QName name) {
        return mapNode(NodeIdentifier.create(name));
    }

    /**
     * Create an immutable map node.
     *
     * @param name QName which will be used as node identifier
     * @return An unordered Map node
     */
    public static @NonNull SystemMapNode mapNode(final NodeIdentifier name) {
        return mapNodeBuilder(name).build();
    }

    /**
     * Create immutable ordered map node.
     *
     * @param name QName which will be used as node identifier
     * @return An ordered Map node
     */
    public static @NonNull UserMapNode orderedMapNode(final QName name) {
        return orderedMapNode(NodeIdentifier.create(name));
    }

    /**
     * Create immutable ordered map node.
     *
     * @param name Node identifier
     * @return An ordered Map node
     */
    public static @NonNull UserMapNode orderedMapNode(final NodeIdentifier name) {
        return BUILDER_FACTORY.newUserMapBuilder().withNodeIdentifier(name).build();
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
        return ImmutableLeafNodeBuilder.createNode(name, value);
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

    public static MapEntryNode.@NonNull Builder mapEntryBuilder(final QName nodeName, final QName keyName,
            final Object keyValue) {
        final var ret = mapEntryBuilder();
        // FIXME: use fluent once we have specialized enough
        ret.withNodeIdentifier(NodeIdentifierWithPredicates.of(nodeName, keyName, keyValue))
            .withChild(leafNode(keyName, keyValue));
        return ret;
    }

    public static MapEntryNode.@NonNull Builder mapEntryBuilder() {
        return BUILDER_FACTORY.newMapEntryBuilder();
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
        return BUILDER_FACTORY.newContainerBuilder().withNodeIdentifier(name).build();
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
        return BUILDER_FACTORY.newChoiceBuilder().withNodeIdentifier(name).build();
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
        return BUILDER_FACTORY.newUnkeyedListBuilder().withNodeIdentifier(name).build();
    }

    /**
     * Convert YangInstanceIdentifier into a normalized node structure.
     *
     * @param ctx schema context to used during serialization
     * @param id instance identifier to convert to node structure starting from root
     * @return serialized normalized node for provided instance Id
     * @throws NullPointerException if any argument is null
     * @throws IllegalArgumentException if the identifier cannot be converted
     */
    public static @NonNull NormalizedNode fromInstanceId(final EffectiveModelContext ctx,
            final YangInstanceIdentifier id) {
        if (id.isEmpty()) {
            return containerNode(SCHEMACONTEXT_NAME);
        }

        final var result = new NormalizationResultHolder();
        try (var writer = ImmutableNormalizedNodeStreamWriter.from(result)) {
            try (var iidWriter = YangInstanceIdentifierWriter.open(writer, ctx, id)) {
                // leaf-list entry nodes are special: they require a value and we can derive it from our instance
                // identitifier
                final var lastArg = id.getLastPathArgument();
                if (lastArg instanceof NodeWithValue<?> withValue) {
                    writer.scalarValue(withValue.getValue());
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to convert " + id, e);
        }
        return result.getResult().data();
    }
}
