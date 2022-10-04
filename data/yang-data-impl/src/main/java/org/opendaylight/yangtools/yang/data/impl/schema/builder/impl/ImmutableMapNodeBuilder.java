/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.util.MapAdaptor;
import org.opendaylight.yangtools.util.UnmodifiableMap;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.SystemMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.builder.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.data.spi.node.AbstractNormalizedNode;

public final class ImmutableMapNodeBuilder implements CollectionNodeBuilder<MapEntryNode, SystemMapNode> {
    private static final int DEFAULT_CAPACITY = 4;

    private final Map<NodeIdentifierWithPredicates, MapEntryNode> value;

    private @Nullable NodeIdentifier nodeIdentifier = null;

    ImmutableMapNodeBuilder() {
        value = new HashMap<>(DEFAULT_CAPACITY);
    }

    ImmutableMapNodeBuilder(final int sizeHint) {
        if (sizeHint >= 0) {
            value = Maps.newHashMapWithExpectedSize(sizeHint);
        } else {
            value = new HashMap<>(DEFAULT_CAPACITY);
        }
    }

    ImmutableMapNodeBuilder(final SystemMapNode node) {
        nodeIdentifier = node.getIdentifier();
        value = MapAdaptor.getDefaultInstance().takeSnapshot(
            node instanceof ImmutableMapNode ? ((ImmutableMapNode) node).children : node.asMap());
    }

    public static @NonNull CollectionNodeBuilder<MapEntryNode, SystemMapNode> create() {
        return new ImmutableMapNodeBuilder();
    }

    public static @NonNull CollectionNodeBuilder<MapEntryNode, SystemMapNode> create(final int sizeHint) {
        return new ImmutableMapNodeBuilder(sizeHint);
    }

    public static @NonNull CollectionNodeBuilder<MapEntryNode, SystemMapNode> create(final SystemMapNode node) {
        return new ImmutableMapNodeBuilder(node);
    }

    @Override
    public ImmutableMapNodeBuilder withChild(final MapEntryNode child) {
        value.put(child.getIdentifier(), child);
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
        for (final MapEntryNode mapEntryNode : withValue) {
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

    protected static final class ImmutableMapNode extends AbstractNormalizedNode<NodeIdentifier, SystemMapNode>
            implements SystemMapNode {

        private final @NonNull Map<NodeIdentifierWithPredicates, MapEntryNode> children;

        ImmutableMapNode(final NodeIdentifier nodeIdentifier,
                         final Map<NodeIdentifierWithPredicates, MapEntryNode> children) {
            super(nodeIdentifier);
            this.children = requireNonNull(children);
        }

        @Override
        public MapEntryNode childByArg(final NodeIdentifierWithPredicates child) {
            return children.get(child);
        }

        @Override
        public Map<NodeIdentifierWithPredicates, MapEntryNode> asMap() {
            return UnmodifiableMap.of(children);
        }

        @Override
        public int size() {
            return children.size();
        }

        @Override
        protected Class<SystemMapNode> implementedType() {
            return SystemMapNode.class;
        }

        @Override
        protected int valueHashCode() {
            return children.hashCode();
        }

        @Override
        protected boolean valueEquals(final SystemMapNode other) {
            final Map<NodeIdentifierWithPredicates, MapEntryNode> otherChildren =
                other instanceof ImmutableMapNode ? ((ImmutableMapNode) other).children : other.asMap();
            return children.equals(otherChildren);
        }
    }
}
