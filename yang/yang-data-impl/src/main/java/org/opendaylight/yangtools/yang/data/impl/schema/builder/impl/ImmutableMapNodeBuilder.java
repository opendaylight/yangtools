/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.util.MapAdaptor;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnorderedMapNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableNormalizedNode;

public class ImmutableMapNodeBuilder implements CollectionNodeBuilder<MapEntryNode, UnorderedMapNode> {
    private static final int DEFAULT_CAPACITY = 4;

    private final Map<NodeIdentifierWithPredicates, MapEntryNode> value;

    @SuppressFBWarnings(value = "NP_STORE_INTO_NONNULL_FIELD", justification = "Non-grok of type annotations")
    private @Nullable NodeIdentifier nodeIdentifier = null;

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

    public static @NonNull CollectionNodeBuilder<MapEntryNode, UnorderedMapNode> create() {
        return new ImmutableMapNodeBuilder();
    }

    public static @NonNull CollectionNodeBuilder<MapEntryNode, UnorderedMapNode> create(final int sizeHint) {
        return new ImmutableMapNodeBuilder(sizeHint);
    }

    public static @NonNull CollectionNodeBuilder<MapEntryNode, UnorderedMapNode> create(final UnorderedMapNode node) {
        if (!(node instanceof ImmutableMapNode)) {
            throw new UnsupportedOperationException(String.format("Cannot initialize from class %s", node.getClass()));
        }

        return new ImmutableMapNodeBuilder((ImmutableMapNode) node);
    }

    @Override
    public ImmutableMapNodeBuilder withChild(final MapEntryNode child) {
        this.value.put(child.getIdentifier(), child);
        return this;
    }

    @Override
    public ImmutableMapNodeBuilder withoutChild(final PathArgument key) {
        this.value.remove(key);
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
        this.nodeIdentifier = withNodeIdentifier;
        return this;
    }

    @Override
    public UnorderedMapNode build() {
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

    protected static final class ImmutableMapNode
            extends AbstractImmutableNormalizedNode<NodeIdentifier, Collection<MapEntryNode>>
            implements UnorderedMapNode {

        private final Map<NodeIdentifierWithPredicates, MapEntryNode> children;

        ImmutableMapNode(final NodeIdentifier nodeIdentifier,
                         final Map<NodeIdentifierWithPredicates, MapEntryNode> children) {
            super(nodeIdentifier);
            this.children = children;
        }

        @Override
        public Optional<MapEntryNode> getChild(final NodeIdentifierWithPredicates child) {
            return Optional.ofNullable(children.get(child));
        }

        @Override
        public Map<NodeIdentifierWithPredicates, MapEntryNode> asMap() {
            // FIXME: we need unmasking here, i.e. UnmodifiableMap
            return children instanceof ImmutableMap || children instanceof Immutable ? children
                : Collections.unmodifiableMap(children);
        }

        @Override
        public int size() {
            return children.size();
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
