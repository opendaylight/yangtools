/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import javax.xml.transform.dom.DOMSource;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.schema.AnydataNode;
import org.opendaylight.yangtools.yang.data.api.schema.AnyxmlNode;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DOMSourceAnyxmlNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode.BuilderFactory;
import org.opendaylight.yangtools.yang.data.api.schema.SystemLeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.SystemMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.api.schema.UserLeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.UserMapNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableAnyXmlNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableAnydataNodeBuilder;
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
    private static final BuilderFactory BUILDER_FACTORY = ImmutableNodes.builderFactory();

    private Builders() {
        // Hidden on purpose
    }

    public static <T> LeafNode.@NonNull Builder<T> leafBuilder() {
        return BUILDER_FACTORY.newLeafBuilder();
    }

    public static <T> LeafSetEntryNode.@NonNull Builder<T> leafSetEntryBuilder() {
        return BUILDER_FACTORY.newLeafSetEntryBuilder()
    }

    public static AnyxmlNode.@NonNull Builder<DOMSource, AnyxmlNode<DOMSource>> anyXmlBuilder() {
        return BUILDER_FACTORY.newAnyxmlBuilder(DOMSource.class);
    }

    public static <T> AnydataNode.@NonNull Builder<T> anydataBuilder(final Class<T> objectModel) {
        return BUILDER_FACTORY.newAnydataBuilder(objectModel);
    }

    public static <T> UserLeafSetNode.@NonNull Builder<T> orderedLeafSetBuilder() {
        return new ImmutableUserLeafSetNodeBuilder<>();
    }

    public static <T> UserLeafSetNode.@NonNull Builder<T> orderedLeafSetBuilder(final UserLeafSetNode<T> node) {
        return ImmutableUserLeafSetNodeBuilder.create(node);
    }

    public static <T> SystemLeafSetNode.@NonNull Builder<T> leafSetBuilder() {
        return new ImmutableLeafSetNodeBuilder<>();
    }

    public static <T> SystemLeafSetNode.@NonNull Builder<T> leafSetBuilder(final int sizeHint) {
        return new ImmutableLeafSetNodeBuilder<>(sizeHint);
    }

    public static <T> SystemLeafSetNode.@NonNull Builder<T> leafSetBuilder(final SystemLeafSetNode<T> node) {
        return ImmutableLeafSetNodeBuilder.create(node);
    }

    public static ContainerNode.@NonNull Builder containerBuilder() {
        return new ImmutableContainerNodeBuilder();
    }

    public static ContainerNode.@NonNull Builder containerBuilder(final int sizeHint) {
        return new ImmutableContainerNodeBuilder(sizeHint);
    }

    public static ContainerNode.@NonNull Builder containerBuilder(final ContainerNode node) {
        return ImmutableContainerNodeBuilder.create(node);
    }

    public static MapEntryNode.@NonNull Builder mapEntryBuilder() {
        return new ImmutableMapEntryNodeBuilder();
    }

    public static MapEntryNode.@NonNull Builder mapEntryBuilder(final int sizeHint) {
        return new ImmutableMapEntryNodeBuilder(sizeHint);
    }

    public static MapEntryNode.@NonNull Builder mapEntryBuilder(final MapEntryNode mapEntryNode) {
        return ImmutableMapEntryNodeBuilder.create(mapEntryNode);
    }

    public static UserMapNode.@NonNull Builder orderedMapBuilder() {
        return new ImmutableUserMapNodeBuilder();
    }

    public static UserMapNode.@NonNull Builder orderedMapBuilder(final int sizeHint) {
        return new ImmutableUserMapNodeBuilder(sizeHint);
    }

    public static UnkeyedListNode.@NonNull Builder unkeyedListBuilder() {
        return new ImmutableUnkeyedListNodeBuilder();
    }

    public static UnkeyedListNode.@NonNull Builder unkeyedListBuilder(final int sizeHint) {
        return new ImmutableUnkeyedListNodeBuilder(sizeHint);
    }

    public static SystemMapNode.@NonNull Builder mapBuilder() {
        return new ImmutableMapNodeBuilder();
    }

    public static SystemMapNode.@NonNull Builder mapBuilder(final int sizeHint) {
        return new ImmutableMapNodeBuilder(sizeHint);
    }

    public static SystemMapNode.@NonNull Builder mapBuilder(final SystemMapNode node) {
        return ImmutableMapNodeBuilder.create(node);
    }

    public static ChoiceNode.@NonNull Builder choiceBuilder() {
        return new ImmutableChoiceNodeBuilder();
    }

    public static ChoiceNode.@NonNull Builder choiceBuilder(final int sizeHint) {
        return new ImmutableChoiceNodeBuilder(sizeHint);
    }

    public static UnkeyedListEntryNode.@NonNull Builder unkeyedListEntryBuilder() {
        return new ImmutableUnkeyedListEntryNodeBuilder();
    }

    public static UnkeyedListEntryNode.@NonNull Builder unkeyedListEntryBuilder(final int sizeHint) {
        return new ImmutableUnkeyedListEntryNodeBuilder(sizeHint);
    }
}
