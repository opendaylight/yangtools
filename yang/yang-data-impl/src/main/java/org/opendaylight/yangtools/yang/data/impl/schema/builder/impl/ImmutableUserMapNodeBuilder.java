/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UserMapNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeContainerBuilder;
import org.opendaylight.yangtools.yang.data.ri.node.impl.ImmutableUserMapNode;

public class ImmutableUserMapNodeBuilder implements CollectionNodeBuilder<MapEntryNode, UserMapNode> {
    private static final int DEFAULT_CAPACITY = 4;

    private Map<NodeIdentifierWithPredicates, MapEntryNode> value;
    private NodeIdentifier nodeIdentifier;
    private boolean dirty;

    protected ImmutableUserMapNodeBuilder() {
        this.value = new LinkedHashMap<>(DEFAULT_CAPACITY);
        this.dirty = false;
    }

    protected ImmutableUserMapNodeBuilder(final int sizeHint) {
        if (sizeHint >= 0) {
            this.value = new LinkedHashMap<>(sizeHint + sizeHint / 3);
        } else {
            this.value = new LinkedHashMap<>(DEFAULT_CAPACITY);
        }
        this.dirty = false;
    }

    protected ImmutableUserMapNodeBuilder(final ImmutableUserMapNode node) {
        this.nodeIdentifier = node.getIdentifier();
        this.value = node.children;
        this.dirty = true;
    }

    public static @NonNull CollectionNodeBuilder<MapEntryNode, UserMapNode> create() {
        return new ImmutableUserMapNodeBuilder();
    }

    public static @NonNull CollectionNodeBuilder<MapEntryNode, UserMapNode> create(final int sizeHint) {
        return new ImmutableUserMapNodeBuilder(sizeHint);
    }

    public static @NonNull CollectionNodeBuilder<MapEntryNode, UserMapNode> create(final UserMapNode node) {
        if (!(node instanceof ImmutableUserMapNode)) {
            throw new UnsupportedOperationException(String.format("Cannot initialize from class %s", node.getClass()));
        }

        return new ImmutableUserMapNodeBuilder((ImmutableUserMapNode) node);
    }

    private void checkDirty() {
        if (dirty) {
            value = new LinkedHashMap<>(value);
            dirty = false;
        }
    }

    @Override
    public CollectionNodeBuilder<MapEntryNode, UserMapNode> withChild(final MapEntryNode child) {
        checkDirty();
        this.value.put(child.getIdentifier(), child);
        return this;
    }

    @Override
    public CollectionNodeBuilder<MapEntryNode, UserMapNode> withoutChild(final PathArgument key) {
        checkDirty();
        this.value.remove(key);
        return this;
    }

    @Override
    public CollectionNodeBuilder<MapEntryNode, UserMapNode> withValue(final Collection<MapEntryNode> withValue) {
        // TODO replace or putAll ?
        for (final MapEntryNode mapEntryNode : withValue) {
            withChild(mapEntryNode);
        }

        return this;
    }

    @Override
    public CollectionNodeBuilder<MapEntryNode, UserMapNode> withNodeIdentifier(
            final NodeIdentifier withNodeIdentifier) {
        this.nodeIdentifier = withNodeIdentifier;
        return this;
    }

    @Override
    public UserMapNode build() {
        dirty = true;
        return new ImmutableUserMapNode(nodeIdentifier, value);
    }

    @Override
    public CollectionNodeBuilder<MapEntryNode, UserMapNode> addChild(
            final MapEntryNode child) {
        return withChild(child);
    }


    @Override
    public NormalizedNodeContainerBuilder<NodeIdentifier, PathArgument, MapEntryNode, UserMapNode> removeChild(
            final PathArgument key) {
        return withoutChild(key);
    }
}
