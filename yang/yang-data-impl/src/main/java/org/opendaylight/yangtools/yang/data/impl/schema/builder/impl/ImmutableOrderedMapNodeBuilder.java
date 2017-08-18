/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.util.UnmodifiableCollection;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.OrderedMapNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeContainerBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableNormalizedNode;

public class ImmutableOrderedMapNodeBuilder implements CollectionNodeBuilder<MapEntryNode, OrderedMapNode> {
    private static final int DEFAULT_CAPACITY = 4;
    private Map<NodeIdentifierWithPredicates, MapEntryNode> value;
    private NodeIdentifier nodeIdentifier;
    private boolean dirty;

    protected ImmutableOrderedMapNodeBuilder() {
        this.value = new LinkedHashMap<>(DEFAULT_CAPACITY);
        this.dirty = false;
    }

    protected ImmutableOrderedMapNodeBuilder(final int sizeHint) {
        if (sizeHint >= 0) {
            this.value = new LinkedHashMap<>(sizeHint + sizeHint / 3);
        } else {
            this.value = new LinkedHashMap<>(DEFAULT_CAPACITY);
        }
        this.dirty = false;
    }

    protected ImmutableOrderedMapNodeBuilder(final ImmutableOrderedMapNode node) {
        this.nodeIdentifier = node.getIdentifier();
        this.value = node.children;
        this.dirty = true;
    }

    public static CollectionNodeBuilder<MapEntryNode, OrderedMapNode> create() {
        return new ImmutableOrderedMapNodeBuilder();
    }

    public static CollectionNodeBuilder<MapEntryNode, OrderedMapNode> create(final int sizeHint) {
        return new ImmutableOrderedMapNodeBuilder(sizeHint);
    }

    public static CollectionNodeBuilder<MapEntryNode, OrderedMapNode> create(final MapNode node) {
        if (!(node instanceof ImmutableOrderedMapNode)) {
            throw new UnsupportedOperationException(String.format("Cannot initialize from class %s", node.getClass()));
        }

        return new ImmutableOrderedMapNodeBuilder((ImmutableOrderedMapNode) node);
    }

    private void checkDirty() {
        if (dirty) {
            value = new LinkedHashMap<>(value);
            dirty = false;
        }
    }

    @Override
    public CollectionNodeBuilder<MapEntryNode, OrderedMapNode> withChild(final MapEntryNode child) {
        checkDirty();
        this.value.put(child.getIdentifier(), child);
        return this;
    }

    @Override
    public CollectionNodeBuilder<MapEntryNode, OrderedMapNode> withoutChild(final PathArgument key) {
        checkDirty();
        this.value.remove(key);
        return this;
    }

    @Override
    public CollectionNodeBuilder<MapEntryNode, OrderedMapNode> withValue(final Collection<MapEntryNode> value) {
        // TODO replace or putAll ?
        for (final MapEntryNode mapEntryNode : value) {
            withChild(mapEntryNode);
        }

        return this;
    }

    @Override
    public CollectionNodeBuilder<MapEntryNode, OrderedMapNode> withNodeIdentifier(final NodeIdentifier nodeIdentifier) {
        this.nodeIdentifier = nodeIdentifier;
        return this;
    }

    @Override
    public OrderedMapNode build() {
        dirty = true;
        return new ImmutableOrderedMapNode(nodeIdentifier, value);
    }

    @Override
    public CollectionNodeBuilder<MapEntryNode, OrderedMapNode> addChild(
            final MapEntryNode child) {
        return withChild(child);
    }


    @Override
    public NormalizedNodeContainerBuilder<NodeIdentifier, PathArgument, MapEntryNode, OrderedMapNode> removeChild(
            final PathArgument key) {
        return withoutChild(key);
    }

    protected static final class ImmutableOrderedMapNode extends AbstractImmutableNormalizedNode<NodeIdentifier, Collection<MapEntryNode>> implements Immutable, OrderedMapNode {

        private final Map<NodeIdentifierWithPredicates, MapEntryNode> children;

        ImmutableOrderedMapNode(final NodeIdentifier nodeIdentifier,
                         final Map<NodeIdentifierWithPredicates, MapEntryNode> children) {
            super(nodeIdentifier);
            this.children = children;
        }

        @Override
        public Optional<MapEntryNode> getChild(final NodeIdentifierWithPredicates child) {
            return Optional.ofNullable(children.get(child));
        }

        @Override
        protected int valueHashCode() {
            return children.hashCode();
        }

        @Override
        protected boolean valueEquals(final AbstractImmutableNormalizedNode<?, ?> other) {
            return children.equals(((ImmutableOrderedMapNode) other).children);
        }

        @Override
        public MapEntryNode getChild(final int position) {
            return Iterables.get(children.values(), position);
        }

        @Override
        public int getSize() {
            return children.size();
        }

        @Override
        public Collection<MapEntryNode> getValue() {
            return UnmodifiableCollection.create(children.values());
        }
    }
}
