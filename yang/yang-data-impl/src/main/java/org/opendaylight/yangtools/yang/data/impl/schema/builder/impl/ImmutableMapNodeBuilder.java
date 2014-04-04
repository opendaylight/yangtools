/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeContainerBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableNormalizedNode;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;

public class ImmutableMapNodeBuilder
        implements CollectionNodeBuilder<MapEntryNode, MapNode> {

    private Map<InstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> value;
    private InstanceIdentifier.NodeIdentifier nodeIdentifier;
    private boolean dirty = false;

    protected ImmutableMapNodeBuilder() {
        this.value = new LinkedHashMap<>();
        this.dirty = false;
    }

    protected ImmutableMapNodeBuilder(final ImmutableMapNode node) {
        this.value = node.children;
        this.dirty = true;
    }

    public static CollectionNodeBuilder<MapEntryNode, MapNode> create() {
        return new ImmutableMapNodeBuilder();
    }

    public static CollectionNodeBuilder<MapEntryNode, MapNode> create(final MapNode node) {
        if (!(node instanceof ImmutableMapNode)) {
            throw new UnsupportedOperationException(String.format("Cannot initialize from class %s", node.getClass()));
        }

        return new ImmutableMapNodeBuilder((ImmutableMapNode) node);
    }

    private void checkDirty() {
        if (dirty) {
            value = new LinkedHashMap<>(value);
            dirty = false;
        }
    }

    @Override
    public CollectionNodeBuilder<MapEntryNode, MapNode> withChild(final MapEntryNode child) {
        checkDirty();
        this.value.put(child.getIdentifier(), child);
        return this;
    }

    @Override
    public CollectionNodeBuilder<MapEntryNode, MapNode> withoutChild(final InstanceIdentifier.PathArgument key) {
        checkDirty();
        this.value.remove(key);
        return this;
    }

    @Override
    public CollectionNodeBuilder<MapEntryNode, MapNode> withValue(final List<MapEntryNode> value) {
        // TODO replace or putAll ?
        for (final MapEntryNode mapEntryNode : value) {
            withChild(mapEntryNode);
        }

        return this;
    }

    @Override
    public CollectionNodeBuilder<MapEntryNode, MapNode> withNodeIdentifier(final InstanceIdentifier.NodeIdentifier nodeIdentifier) {
        this.nodeIdentifier = nodeIdentifier;
        return this;
    }

    @Override
    public MapNode build() {
        dirty = true;
        return new ImmutableMapNode(nodeIdentifier, value);
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

    protected static final class ImmutableMapNode extends AbstractImmutableNormalizedNode<InstanceIdentifier.NodeIdentifier, Iterable<MapEntryNode>> implements Immutable,MapNode {

        private final Map<InstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> children;

        ImmutableMapNode(final InstanceIdentifier.NodeIdentifier nodeIdentifier,
                         final Map<InstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> children) {
            super(nodeIdentifier, Iterables.unmodifiableIterable(children.values()));
            this.children = children;
        }

        @Override
        public Optional<MapEntryNode> getChild(final InstanceIdentifier.NodeIdentifierWithPredicates child) {
            return Optional.fromNullable(children.get(child));
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
