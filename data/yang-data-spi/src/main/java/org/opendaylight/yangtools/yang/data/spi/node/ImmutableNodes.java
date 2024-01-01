/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.node;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.AnydataNode;
import org.opendaylight.yangtools.yang.data.api.schema.AnyxmlNode;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode.BuilderFactory;
import org.opendaylight.yangtools.yang.data.api.schema.SystemLeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.SystemMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.api.schema.UserLeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.UserMapNode;
import org.opendaylight.yangtools.yang.data.spi.node.impl.ImmutableBuilderFactory;
import org.opendaylight.yangtools.yang.data.spi.node.impl.ImmutableLeafNode;
import org.opendaylight.yangtools.yang.data.spi.node.impl.ImmutableLeafSetEntryNode;

/**
 * Utilities for creating immutable implementations of various {@link NormalizedNode}s.
 */
public final class ImmutableNodes {
    private static final @NonNull ImmutableBuilderFactory BUILDER_FACTORY = new ImmutableBuilderFactory();

    private ImmutableNodes() {
        // Hidden on purpose
    }

    public static @NonNull BuilderFactory builderFactory() {
        return BUILDER_FACTORY;
    }

    /**
     * Construct an immutable {@link LeafNode}.
     *
     * @param <T> Type of leaf node value
     * @param name Name of leaf node
     * @param value Value of leaf node
     * @return Leaf node with supplied name and value
     * @throws NullPointerException if any argument is {@code null}
     */
    public static <T> @NonNull LeafNode<T> leafNode(final NodeIdentifier name, final T value) {
        return ImmutableLeafNode.of(name, value);
    }

    /**
     * Construct an immutable {@link LeafNode}.
     *
     * @param <T> Type of leaf node value
     * @param name Name of leaf node
     * @param value Value of leaf node
     * @return Leaf node with supplied name and value
     * @throws NullPointerException if any argument is {@code null}
     */
    public static <T> @NonNull LeafNode<T> leafNode(final QName name, final T value) {
        return leafNode(NodeIdentifier.create(name), value);
    }

    public static @NonNull MapEntryNode mapEntry(final NodeIdentifierWithPredicates name) {
        return BUILDER_FACTORY.newMapEntryBuilder(name.size()).withNodeIdentifier(name).build();
    }

    public static @NonNull MapEntryNode mapEntry(final QName listName, final QName keyName, final Object keyValue) {
        return mapEntry(NodeIdentifierWithPredicates.of(listName, keyName, keyValue));
    }

    public static <T> @NonNull LeafSetEntryNode<T> leafSetEntry(final NodeWithValue<T> name) {
        return ImmutableLeafSetEntryNode.of(name);
    }

    public static <T> @NonNull LeafSetEntryNode<T> leafSetEntry(final QName name, final T value) {
        return leafSetEntry(new NodeWithValue<>(name, value));
    }

    public static <T> AnydataNode.@NonNull Builder<T> newAnydataBuilder(final Class<T> objectModel) {
        return BUILDER_FACTORY.newAnydataBuilder(objectModel);
    }

    public static <T> AnyxmlNode.@NonNull Builder<T, AnyxmlNode<T>> newAnyxmlBuilder(final Class<T> objectModel) {
        return BUILDER_FACTORY.newAnyxmlBuilder(objectModel);
    }

    public static ChoiceNode.@NonNull Builder newChoiceBuilder() {
        return BUILDER_FACTORY.newChoiceBuilder();
    }

    public static ContainerNode.@NonNull Builder newContainerBuilder() {
        return BUILDER_FACTORY.newContainerBuilder();
    }

    public static MapEntryNode.@NonNull Builder newMapEntryBuilder() {
        return BUILDER_FACTORY.newMapEntryBuilder();
    }

    public static SystemMapNode.@NonNull Builder newSystemMapBuilder() {
        return BUILDER_FACTORY.newSystemMapBuilder();
    }

    public static UserMapNode.@NonNull Builder newUserMapBuilder() {
        return BUILDER_FACTORY.newUserMapBuilder();
    }

    public static UnkeyedListEntryNode.@NonNull Builder newUnkeyedListEntryBuilder() {
        return BUILDER_FACTORY.newUnkeyedListEntryBuilder();
    }

    public static UnkeyedListNode.@NonNull Builder newUnkeyedListBuilder() {
        return BUILDER_FACTORY.newUnkeyedListBuilder();
    }

    public static <T> LeafNode.@NonNull Builder<T> newLeafBuilder() {
        return BUILDER_FACTORY.newLeafBuilder();
    }

    public static <T> LeafSetEntryNode.@NonNull Builder<T> newLeafSetEntryBuilder() {
        return BUILDER_FACTORY.newLeafSetEntryBuilder();
    }

    public static <T> SystemLeafSetNode.@NonNull Builder<T> newSystemLeafSetBuilder() {
        return BUILDER_FACTORY.newSystemLeafSetBuilder();
    }

    public static <T> UserLeafSetNode.@NonNull Builder<T> newUserLeafSetBuilder() {
        return BUILDER_FACTORY.newUserLeafSetBuilder();
    }
}
