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

public final class ImmutableSystemMapNodeBuilder implements SystemMapNode.Builder {
    private static final int DEFAULT_CAPACITY = 4;

    private final Map<NodeIdentifierWithPredicates, MapEntryNode> value;

    private @Nullable NodeIdentifier nodeIdentifier = null;

    public ImmutableSystemMapNodeBuilder() {
        value = new HashMap<>(DEFAULT_CAPACITY);
    }

    public ImmutableSystemMapNodeBuilder(final int sizeHint) {
        if (sizeHint >= 0) {
            value = Maps.newHashMapWithExpectedSize(sizeHint);
        } else {
            value = new HashMap<>(DEFAULT_CAPACITY);
        }
    }

    private ImmutableSystemMapNodeBuilder(final SystemMapNode node) {
        nodeIdentifier = node.name();
        value = MapAdaptor.getDefaultInstance().takeSnapshot(accessChildren(node));
    }

    public static SystemMapNode.@NonNull Builder create(final SystemMapNode node) {
        return new ImmutableSystemMapNodeBuilder(node);
    }

    @Override
    public ImmutableSystemMapNodeBuilder withChild(final MapEntryNode child) {
        value.put(child.name(), child);
        return this;
    }

    @Override
    public ImmutableSystemMapNodeBuilder withoutChild(final PathArgument key) {
        value.remove(key);
        return this;
    }

    @Override
    public ImmutableSystemMapNodeBuilder withValue(final Collection<MapEntryNode> withValue) {
        // TODO replace or putAll ?
        for (var mapEntryNode : withValue) {
            withChild(mapEntryNode);
        }

        return this;
    }

    @Override
    public ImmutableSystemMapNodeBuilder withNodeIdentifier(final NodeIdentifier withNodeIdentifier) {
        nodeIdentifier = withNodeIdentifier;
        return this;
    }

    @Override
    public SystemMapNode build() {
        return new ImmutableSystemMapNode(nodeIdentifier, MapAdaptor.getDefaultInstance().optimize(value));
    }

    @Override
    public ImmutableSystemMapNodeBuilder addChild(final MapEntryNode child) {
        return withChild(child);
    }

    @Override
    public ImmutableSystemMapNodeBuilder removeChild(final PathArgument key) {
        return withoutChild(key);
    }

    static @NonNull Map<NodeIdentifierWithPredicates, MapEntryNode> accessChildren(final SystemMapNode node) {
        return node instanceof ImmutableSystemMapNode immutableNode ? immutableNode.children : node.asMap();
    }
}
