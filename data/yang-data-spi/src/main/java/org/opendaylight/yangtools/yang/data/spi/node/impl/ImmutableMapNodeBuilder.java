/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.node.impl;

import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.util.MapAdaptor;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.SystemMapNode;

public final class ImmutableMapNodeBuilder implements SystemMapNode.Builder {
    private static final int DEFAULT_CAPACITY = 4;

    private final Map<NodeIdentifierWithPredicates, MapEntryNode> value;

    private @Nullable NodeIdentifier nodeIdentifier = null;

    public ImmutableMapNodeBuilder() {
        value = new HashMap<>(DEFAULT_CAPACITY);
    }

    public ImmutableMapNodeBuilder(final int sizeHint) {
        if (sizeHint >= 0) {
            value = Maps.newHashMapWithExpectedSize(sizeHint);
        } else {
            value = new HashMap<>(DEFAULT_CAPACITY);
        }
    }

    private ImmutableMapNodeBuilder(final SystemMapNode node) {
        nodeIdentifier = node.name();
        value = MapAdaptor.getDefaultInstance().takeSnapshot(accessChildren(node));
    }

    public static SystemMapNode.@NonNull Builder create(final SystemMapNode node) {
        return new ImmutableMapNodeBuilder(node);
    }

    @Override
    public ImmutableMapNodeBuilder withChild(final MapEntryNode child) {
        value.put(child.name(), child);
        return this;
    }

    @Override
    public ImmutableMapNodeBuilder withoutChild(final PathArgument key) {
        value.remove(key);
        return this;
    }

    @Override
    public ImmutableMapNodeBuilder withValue(final Collection<MapEntryNode> withValue) {
        // TODO replace or putAll ?
        for (var mapEntryNode : withValue) {
            withChild(mapEntryNode);
        }

        return this;
    }

    @Override
    public ImmutableMapNodeBuilder withNodeIdentifier(final NodeIdentifier withNodeIdentifier) {
        nodeIdentifier = withNodeIdentifier;
        return this;
    }

    @Override
    public SystemMapNode build() {
        return new ImmutableMapNode(nodeIdentifier, MapAdaptor.getDefaultInstance().optimize(value));
    }

    @Override
    public ImmutableMapNodeBuilder addChild(final MapEntryNode child) {
        return withChild(child);
    }

    @Override
    public ImmutableMapNodeBuilder removeChild(final PathArgument key) {
        return withoutChild(key);
    }

    static @NonNull Map<NodeIdentifierWithPredicates, MapEntryNode> accessChildren(final SystemMapNode node) {
        return node instanceof ImmutableMapNode immutableNode ? immutableNode.children : node.asMap();
    }
}
