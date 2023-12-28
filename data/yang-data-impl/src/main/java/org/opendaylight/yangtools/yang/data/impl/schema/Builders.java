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
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;

/**
 * Utility methods producing immutable implementations of various {@link NormalizedNode}s via builders.
 *
 * @deprecated Use {@link BuilderFactory} provided by {@link ImmutableNodes#builderFactory()} instead.
 */
@Deprecated(since = "12.0.0", forRemoval = true)
public final class Builders {
    private static final BuilderFactory BUILDER_FACTORY = ImmutableNodes.builderFactory();

    private Builders() {
        // Hidden on purpose
    }

    public static <T> LeafNode.@NonNull Builder<T> leafBuilder() {
        return BUILDER_FACTORY.newLeafBuilder();
    }

    public static <T> LeafSetEntryNode.@NonNull Builder<T> leafSetEntryBuilder() {
        return BUILDER_FACTORY.newLeafSetEntryBuilder();
    }

    public static AnyxmlNode.@NonNull Builder<DOMSource, AnyxmlNode<DOMSource>> anyXmlBuilder() {
        return BUILDER_FACTORY.newAnyxmlBuilder(DOMSource.class);
    }

    public static <T> AnydataNode.@NonNull Builder<T> anydataBuilder(final Class<T> objectModel) {
        return BUILDER_FACTORY.newAnydataBuilder(objectModel);
    }

    public static <T> UserLeafSetNode.@NonNull Builder<T> orderedLeafSetBuilder() {
        return BUILDER_FACTORY.newUserLeafSetBuilder();
    }

    public static <T> UserLeafSetNode.@NonNull Builder<T> orderedLeafSetBuilder(final UserLeafSetNode<T> node) {
        return BUILDER_FACTORY.newUserLeafSetBuilder(node);
    }

    public static <T> SystemLeafSetNode.@NonNull Builder<T> leafSetBuilder() {
        return BUILDER_FACTORY.newSystemLeafSetBuilder();
    }

    public static <T> SystemLeafSetNode.@NonNull Builder<T> leafSetBuilder(final int sizeHint) {
        return BUILDER_FACTORY.newSystemLeafSetBuilder(sizeHint);
    }

    public static <T> SystemLeafSetNode.@NonNull Builder<T> leafSetBuilder(final SystemLeafSetNode<T> node) {
        return BUILDER_FACTORY.newSystemLeafSetBuilder(node);
    }

    public static ContainerNode.@NonNull Builder containerBuilder() {
        return BUILDER_FACTORY.newContainerBuilder();
    }

    public static ContainerNode.@NonNull Builder containerBuilder(final int sizeHint) {
        return BUILDER_FACTORY.newContainerBuilder(sizeHint);
    }

    public static ContainerNode.@NonNull Builder containerBuilder(final ContainerNode node) {
        return BUILDER_FACTORY.newContainerBuilder(node);
    }

    public static MapEntryNode.@NonNull Builder mapEntryBuilder() {
        return BUILDER_FACTORY.newMapEntryBuilder();
    }

    public static MapEntryNode.@NonNull Builder mapEntryBuilder(final int sizeHint) {
        return BUILDER_FACTORY.newMapEntryBuilder(sizeHint);
    }

    public static MapEntryNode.@NonNull Builder mapEntryBuilder(final MapEntryNode mapEntryNode) {
        return BUILDER_FACTORY.newMapEntryBuilder(mapEntryNode);
    }

    public static UserMapNode.@NonNull Builder orderedMapBuilder() {
        return BUILDER_FACTORY.newUserMapBuilder();
    }

    public static UserMapNode.@NonNull Builder orderedMapBuilder(final int sizeHint) {
        return BUILDER_FACTORY.newUserMapBuilder(sizeHint);
    }

    public static UnkeyedListNode.@NonNull Builder unkeyedListBuilder() {
        return BUILDER_FACTORY.newUnkeyedListBuilder();
    }

    public static UnkeyedListNode.@NonNull Builder unkeyedListBuilder(final int sizeHint) {
        return BUILDER_FACTORY.newUnkeyedListBuilder(sizeHint);
    }

    public static SystemMapNode.@NonNull Builder mapBuilder() {
        return BUILDER_FACTORY.newSystemMapBuilder();
    }

    public static SystemMapNode.@NonNull Builder mapBuilder(final int sizeHint) {
        return BUILDER_FACTORY.newSystemMapBuilder(sizeHint);
    }

    public static SystemMapNode.@NonNull Builder mapBuilder(final SystemMapNode node) {
        return BUILDER_FACTORY.newSystemMapBuilder(node);
    }

    public static ChoiceNode.@NonNull Builder choiceBuilder() {
        return BUILDER_FACTORY.newChoiceBuilder();
    }

    public static ChoiceNode.@NonNull Builder choiceBuilder(final int sizeHint) {
        return BUILDER_FACTORY.newChoiceBuilder(sizeHint);
    }

    public static UnkeyedListEntryNode.@NonNull Builder unkeyedListEntryBuilder() {
        return BUILDER_FACTORY.newUnkeyedListEntryBuilder();
    }

    public static UnkeyedListEntryNode.@NonNull Builder unkeyedListEntryBuilder(final int sizeHint) {
        return BUILDER_FACTORY.newUnkeyedListEntryBuilder(sizeHint);
    }
}
