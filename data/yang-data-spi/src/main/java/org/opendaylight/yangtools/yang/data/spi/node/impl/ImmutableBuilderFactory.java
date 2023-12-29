/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.node.impl;

import javax.xml.transform.dom.DOMSource;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.schema.AnydataNode;
import org.opendaylight.yangtools.yang.data.api.schema.AnyxmlNode;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
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

/**
 * A {@link BuilderFactory} producing builders which produce immutable in-memory normalized nodes.
 */
public final class ImmutableBuilderFactory implements BuilderFactory {
    @Override
    public <T> AnydataNode.Builder<T> newAnydataBuilder(final Class<T> objectModel) {
        return new ImmutableAnydataNodeBuilder<>(objectModel);
    }

    @Override
    public <T> AnyxmlNode.Builder<T, AnyxmlNode<T>> newAnyxmlBuilder(final Class<T> objectModel) {
        if (DOMSource.class.equals(objectModel)) {
            return (AnyxmlNode.Builder) new ImmutableAnyxmlNodeBuilder();
        }
        throw new IllegalArgumentException("Unsupported object model " + objectModel);
    }

    @Override
    public ChoiceNode.Builder newChoiceBuilder() {
        return new ImmutableChoiceNodeBuilder();
    }

    @Override
    public ChoiceNode.Builder newChoiceBuilder(final int sizeHint) {
        return new ImmutableChoiceNodeBuilder(sizeHint);
    }

    @Override
    public ChoiceNode.Builder newChoiceBuilder(final ChoiceNode node) {
        return ImmutableChoiceNodeBuilder.create(node);
    }

    @Override
    public ContainerNode.Builder newContainerBuilder() {
        return new ImmutableContainerNodeBuilder();
    }

    @Override
    public ContainerNode.Builder newContainerBuilder(final int sizeHint) {
        return new ImmutableContainerNodeBuilder(sizeHint);
    }

    @Override
    public ContainerNode.Builder newContainerBuilder(final ContainerNode node) {
        return ImmutableContainerNodeBuilder.create(node);
    }

    @Override
    public MapEntryNode.Builder newMapEntryBuilder() {
        return new ImmutableMapEntryNodeBuilder();
    }

    @Override
    public MapEntryNode.Builder newMapEntryBuilder(final int sizeHint) {
        return new ImmutableMapEntryNodeBuilder(sizeHint);
    }

    @Override
    public MapEntryNode.Builder newMapEntryBuilder(final MapEntryNode mapEntryNode) {
        return ImmutableMapEntryNodeBuilder.create(mapEntryNode);
    }

    @Override
    public SystemMapNode.Builder newSystemMapBuilder() {
        return new ImmutableSystemMapNodeBuilder();
    }

    @Override
    public SystemMapNode.Builder newSystemMapBuilder(final int sizeHint) {
        return new ImmutableSystemMapNodeBuilder(sizeHint);
    }

    @Override
    public SystemMapNode.Builder newSystemMapBuilder(final SystemMapNode node) {
        return ImmutableSystemMapNodeBuilder.create(node);
    }

    @Override
    public UserMapNode.Builder newUserMapBuilder() {
        return new ImmutableUserMapNodeBuilder();
    }

    @Override
    public UserMapNode.Builder newUserMapBuilder(final int sizeHint) {
        return new ImmutableUserMapNodeBuilder(sizeHint);
    }

    @Override
    public UserMapNode.Builder newUserMapBuilder(final UserMapNode node) {
        return ImmutableUserMapNodeBuilder.create(node);
    }

    @Override
    public UnkeyedListEntryNode.Builder newUnkeyedListEntryBuilder() {
        return new ImmutableUnkeyedListEntryNodeBuilder();
    }

    @Override
    public UnkeyedListEntryNode.Builder newUnkeyedListEntryBuilder(final int sizeHint) {
        return new ImmutableUnkeyedListEntryNodeBuilder(sizeHint);
    }

    @Override
    public UnkeyedListEntryNode.Builder newUnkeyedListEntryBuilder(final UnkeyedListEntryNode node) {
        return ImmutableUnkeyedListEntryNodeBuilder.create(node);
    }

    @Override
    public UnkeyedListNode.Builder newUnkeyedListBuilder() {
        return new ImmutableUnkeyedListNodeBuilder();
    }

    @Override
    public UnkeyedListNode.Builder newUnkeyedListBuilder(final int sizeHint) {
        return new ImmutableUnkeyedListNodeBuilder(sizeHint);
    }

    @Override
    public UnkeyedListNode.Builder newUnkeyedListBuilder(final UnkeyedListNode node) {
        return ImmutableUnkeyedListNodeBuilder.create(node);
    }

    @Override
    public <T> LeafNode.Builder<T> newLeafBuilder() {
        return new ImmutableLeafNodeBuilder<>();
    }

    @Override
    public <T> LeafSetEntryNode.Builder<T> newLeafSetEntryBuilder() {
        return new ImmutableLeafSetEntryNodeBuilder<>();
    }

    @Override
    public <T> SystemLeafSetNode.Builder<T> newSystemLeafSetBuilder() {
        return new ImmutableSystemLeafSetNodeBuilder<>();
    }

    @Override
    public <T> SystemLeafSetNode.Builder<T> newSystemLeafSetBuilder(final int sizeHint) {
        return new ImmutableSystemLeafSetNodeBuilder<>(sizeHint);
    }

    @Override
    public <T> SystemLeafSetNode.@NonNull Builder<T> newSystemLeafSetBuilder(final SystemLeafSetNode<T> node) {
        return ImmutableSystemLeafSetNodeBuilder.create(node);
    }

    @Override
    public <T> UserLeafSetNode.Builder<T> newUserLeafSetBuilder() {
        return new ImmutableUserLeafSetNodeBuilder<>();
    }

    @Override
    public <T> UserLeafSetNode.Builder<T> newUserLeafSetBuilder(final int sizeHint) {
        return new ImmutableUserLeafSetNodeBuilder<>(sizeHint);
    }

    @Override
    public <T> UserLeafSetNode.@NonNull Builder<T> newUserLeafSetBuilder(final UserLeafSetNode<T> node) {
        return ImmutableUserLeafSetNodeBuilder.create(node);
    }
}
