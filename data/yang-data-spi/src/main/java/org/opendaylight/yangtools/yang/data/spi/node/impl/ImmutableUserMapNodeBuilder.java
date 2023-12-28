/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.node.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.util.UnmodifiableCollection;
import org.opendaylight.yangtools.util.UnmodifiableMap;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.AbstractUserMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UserMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.builder.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.data.api.schema.builder.NormalizedNodeContainerBuilder;

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
    public CollectionNodeBuilder<MapEntryNode, UserMapNode> withChild(final MapEntryNode child) {
        checkDirty();
        value.put(child.name(), child);
        return this;
    }

    @Override
    public CollectionNodeBuilder<MapEntryNode, UserMapNode> withoutChild(final PathArgument key) {
        checkDirty();
        value.remove(key);
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
        nodeIdentifier = withNodeIdentifier;
        return this;
    }

    @Override
    public UserMapNode build() {
        dirty = true;
        return new ImmutableUserMapNode(nodeIdentifier, value);
    }

    @Override
    public CollectionNodeBuilder<MapEntryNode, UserMapNode> addChild(final MapEntryNode child) {
        return withChild(child);
    }


    @Override
    public NormalizedNodeContainerBuilder<NodeIdentifier, PathArgument, MapEntryNode, UserMapNode> removeChild(
            final PathArgument key) {
        return withoutChild(key);
    }

    protected static final class ImmutableUserMapNode extends AbstractUserMapNode {
        private final @NonNull NodeIdentifier name;
        private final @NonNull Map<NodeIdentifierWithPredicates, MapEntryNode> children;

        ImmutableUserMapNode(final NodeIdentifier name,
                final Map<NodeIdentifierWithPredicates, MapEntryNode> children) {
            this.name = requireNonNull(name);
            this.children = requireNonNull(children);
        }

        @Override
        public NodeIdentifier name() {
            return name;
        }

        @Override
        public MapEntryNode childByArg(final NodeIdentifierWithPredicates child) {
            return children.get(child);
        }

        @Override
        public MapEntryNode childAt(final int position) {
            return Iterables.get(children.values(), position);
        }

        @Override
        public int size() {
            return children.size();
        }

        @Override
        public Collection<MapEntryNode> value() {
            return children.values();
        }

        @Override
        public Collection<MapEntryNode> wrappedValue() {
            return UnmodifiableCollection.create(value());
        }

        @Override
        public Map<NodeIdentifierWithPredicates, MapEntryNode> asMap() {
            return UnmodifiableMap.of(children);
        }

        @Override
        protected int valueHashCode() {
            // Order is important
            int hashCode = 1;
            for (var child : children.values()) {
                hashCode = 31 * hashCode + child.hashCode();
            }
            return hashCode;
        }

        @Override
        protected boolean valueEquals(final UserMapNode other) {
            final var otherChildren = other instanceof ImmutableUserMapNode immutable ? immutable.children
                : other.asMap();
            return Iterables.elementsEqual(children.values(), otherChildren.values());
        }
    }
}
