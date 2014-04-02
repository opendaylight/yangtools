/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import java.util.List;
import java.util.Map;

import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableNormalizedNode;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

public class ImmutableMapNodeBuilder
        implements CollectionNodeBuilder<MapEntryNode, MapNode> {

    protected Map<InstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> value = Maps.newLinkedHashMap();
    protected InstanceIdentifier.NodeIdentifier nodeIdentifier;

    public static CollectionNodeBuilder<MapEntryNode, MapNode> create() {
        return new ImmutableMapNodeBuilder();
    }

    @Override
    public CollectionNodeBuilder<MapEntryNode, MapNode> withChild(final MapEntryNode child) {
        this.value.put(child.getIdentifier(), child);
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
        return new ImmutableMapNode(nodeIdentifier, value);
    }

    @Override
    public CollectionNodeBuilder<MapEntryNode, MapNode> addChild(
            final MapEntryNode child) {
        return withChild(child);
    }

    static final class ImmutableMapNode extends AbstractImmutableNormalizedNode<InstanceIdentifier.NodeIdentifier, Iterable<MapEntryNode>> implements Immutable,MapNode {

        private final Map<InstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> mappedChildren;

        ImmutableMapNode(final InstanceIdentifier.NodeIdentifier nodeIdentifier,
                         final Map<InstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> children) {
            super(nodeIdentifier, ImmutableList.copyOf(children.values()));
            this.mappedChildren = children;
        }

        @Override
        public Optional<MapEntryNode> getChild(final InstanceIdentifier.NodeIdentifierWithPredicates child) {
            return Optional.fromNullable(mappedChildren.get(child));
        }

        @Override
        protected int valueHashCode() {
            return mappedChildren.hashCode();
        }

        @Override
        protected boolean valueEquals(final AbstractImmutableNormalizedNode<?, ?> other) {
            return mappedChildren.equals(((ImmutableMapNode) other).mappedChildren);
        }
    }
}
