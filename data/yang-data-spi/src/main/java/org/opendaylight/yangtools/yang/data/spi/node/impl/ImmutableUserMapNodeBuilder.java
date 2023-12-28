/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.node.impl;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UserMapNode;

public final class ImmutableUserMapNodeBuilder implements UserMapNode.Builder {
    private static final int DEFAULT_CAPACITY = 4;

    private Map<NodeIdentifierWithPredicates, MapEntryNode> value;
    private NodeIdentifier nodeIdentifier;
    private boolean dirty;

    public ImmutableUserMapNodeBuilder() {
        value = new LinkedHashMap<>(DEFAULT_CAPACITY);
        dirty = false;
    }

    public ImmutableUserMapNodeBuilder(final int sizeHint) {
        if (sizeHint >= 0) {
            value = new LinkedHashMap<>(sizeHint + sizeHint / 3);
        } else {
            value = new LinkedHashMap<>(DEFAULT_CAPACITY);
        }
        dirty = false;
    }

    private ImmutableUserMapNodeBuilder(final ImmutableUserMapNode node) {
        nodeIdentifier = node.name();
        value = node.children;
        dirty = true;
    }

    public static UserMapNode.@NonNull Builder create(final UserMapNode node) {
        if (node instanceof ImmutableUserMapNode immutableNode) {
            return new ImmutableUserMapNodeBuilder(immutableNode);
        }
        throw new UnsupportedOperationException("Cannot initialize from class " + node.getClass());
    }

    private void checkDirty() {
        if (dirty) {
            value = new LinkedHashMap<>(value);
            dirty = false;
        }
    }

    @Override
    public ImmutableUserMapNodeBuilder withChild(final MapEntryNode child) {
        checkDirty();
        value.put(child.name(), child);
        return this;
    }

    @Override
    public ImmutableUserMapNodeBuilder withoutChild(final PathArgument key) {
        checkDirty();
        value.remove(key);
        return this;
    }

    @Override
    public ImmutableUserMapNodeBuilder withValue(final Collection<MapEntryNode> withValue) {
        // TODO replace or putAll ?
        for (var mapEntryNode : withValue) {
            withChild(mapEntryNode);
        }
        return this;
    }

    @Override
    public ImmutableUserMapNodeBuilder withNodeIdentifier(final NodeIdentifier withNodeIdentifier) {
        nodeIdentifier = withNodeIdentifier;
        return this;
    }

    @Override
    public ImmutableUserMapNodeBuilder addChild(final MapEntryNode child) {
        return withChild(child);
    }

    @Override
    public ImmutableUserMapNodeBuilder removeChild(final PathArgument key) {
        return withoutChild(key);
    }

    @Override
    public UserMapNode build() {
        dirty = true;
        return new ImmutableUserMapNode(nodeIdentifier, value);
    }
}
