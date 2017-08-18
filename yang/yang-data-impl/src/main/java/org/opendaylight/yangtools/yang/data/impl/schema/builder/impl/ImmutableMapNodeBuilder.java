/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.util.MapAdaptor;
import org.opendaylight.yangtools.util.UnmodifiableCollection;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeContainerBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableNormalizedNode;

public class ImmutableMapNodeBuilder implements CollectionNodeBuilder<MapEntryNode, MapNode> {
    private static final int DEFAULT_CAPACITY = 4;
    private final Map<NodeIdentifierWithPredicates, MapEntryNode> value;
    private NodeIdentifier nodeIdentifier;

    protected ImmutableMapNodeBuilder() {
        this.value = new HashMap<>(DEFAULT_CAPACITY);
    }

    protected ImmutableMapNodeBuilder(final int sizeHint) {
        if (sizeHint >= 0) {
            this.value = Maps.newHashMapWithExpectedSize(sizeHint);
        } else {
            this.value = new HashMap<>(DEFAULT_CAPACITY);
        }
    }

    protected ImmutableMapNodeBuilder(final ImmutableMapNode node) {
        this.nodeIdentifier = node.getIdentifier();
        this.value = MapAdaptor.getDefaultInstance().takeSnapshot(node.children);
    }

    public static CollectionNodeBuilder<MapEntryNode, MapNode> create() {
        return new ImmutableMapNodeBuilder();
    }

    public static CollectionNodeBuilder<MapEntryNode, MapNode> create(final int sizeHint) {
        return new ImmutableMapNodeBuilder(sizeHint);
    }

    public static CollectionNodeBuilder<MapEntryNode, MapNode> create(final MapNode node) {
        if (!(node instanceof ImmutableMapNode)) {
            throw new UnsupportedOperationException(String.format("Cannot initialize from class %s", node.getClass()));
        }

        return new ImmutableMapNodeBuilder((ImmutableMapNode) node);
    }

    @Override
    public CollectionNodeBuilder<MapEntryNode, MapNode> withChild(final MapEntryNode child) {
        this.value.put(child.getIdentifier(), child);
        return this;
    }

    @Override
    public CollectionNodeBuilder<MapEntryNode, MapNode> withoutChild(final PathArgument key) {
        this.value.remove(key);
        return this;
    }

    @Override
    public CollectionNodeBuilder<MapEntryNode, MapNode> withValue(final Collection<MapEntryNode> value) {
        // TODO replace or putAll ?
        for (final MapEntryNode mapEntryNode : value) {
            withChild(mapEntryNode);
        }

        return this;
    }

    @Override
    public CollectionNodeBuilder<MapEntryNode, MapNode> withNodeIdentifier(final NodeIdentifier nodeIdentifier) {
        this.nodeIdentifier = nodeIdentifier;
        return this;
    }

    @Override
    public MapNode build() {
        return new ImmutableMapNode(nodeIdentifier, MapAdaptor.getDefaultInstance().optimize(value));
    }

    @Override
    public CollectionNodeBuilder<MapEntryNode, MapNode> addChild(
            final MapEntryNode child) {
        return withChild(child);
    }


    @Override
    public NormalizedNodeContainerBuilder<NodeIdentifier, PathArgument, MapEntryNode, MapNode> removeChild(
            final PathArgument key) {
        return withoutChild(key);
    }

    protected static final class ImmutableMapNode extends AbstractImmutableNormalizedNode<YangInstanceIdentifier.NodeIdentifier, Collection<MapEntryNode>> implements Immutable,MapNode {

        private final Map<YangInstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> children;

        ImmutableMapNode(final YangInstanceIdentifier.NodeIdentifier nodeIdentifier,
                         final Map<YangInstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> children) {
            super(nodeIdentifier);
            this.children = children;
        }

        @Override
        public Optional<MapEntryNode> getChild(final YangInstanceIdentifier.NodeIdentifierWithPredicates child) {
            return Optional.ofNullable(children.get(child));
        }

        @Override
        public Collection<MapEntryNode> getValue() {
            return UnmodifiableCollection.create(children.values());
        }

        @Override
        protected int valueHashCode() {
            return children.hashCode();
        }

        @Override
        protected boolean valueEquals(final AbstractImmutableNormalizedNode<?, ?> other) {
            return children.equals(((ImmutableMapNode) other).children);
        }
    }
}
