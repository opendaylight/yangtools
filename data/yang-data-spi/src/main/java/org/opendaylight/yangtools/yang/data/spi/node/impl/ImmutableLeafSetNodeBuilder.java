/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.node.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.util.MapAdaptor;
import org.opendaylight.yangtools.util.UnmodifiableCollection;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.AbstractSystemLeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.SystemLeafSetNode;

public final class ImmutableLeafSetNodeBuilder<T> implements SystemLeafSetNode.Builder<T> {
    private static final int DEFAULT_CAPACITY = 4;

    private final Map<NodeWithValue<?>, LeafSetEntryNode<T>> value;

    private NodeIdentifier nodeIdentifier;

    public ImmutableLeafSetNodeBuilder() {
        value = new HashMap<>(DEFAULT_CAPACITY);
    }

    public ImmutableLeafSetNodeBuilder(final int sizeHint) {
        if (sizeHint >= 0) {
            value = Maps.newHashMapWithExpectedSize(sizeHint);
        } else {
            value = new HashMap<>(DEFAULT_CAPACITY);
        }
    }

    private ImmutableLeafSetNodeBuilder(final ImmutableLeafSetNode<T> node) {
        nodeIdentifier = node.name();
        value = MapAdaptor.getDefaultInstance().takeSnapshot(node.children);
    }

    public static <T> SystemLeafSetNode.@NonNull Builder<T> create(final SystemLeafSetNode<T> node) {
        if (node instanceof ImmutableLeafSetNode) {
            return new ImmutableLeafSetNodeBuilder<>((ImmutableLeafSetNode<T>) node);
        }
        throw new UnsupportedOperationException("Cannot initialize from class " + node.getClass());
    }

    @Override
    public ImmutableLeafSetNodeBuilder<T> withChild(final LeafSetEntryNode<T> child) {
        value.put(child.name(), child);
        return this;
    }

    @Override
    public ImmutableLeafSetNodeBuilder<T> withoutChild(final PathArgument key) {
        value.remove(key);
        return this;
    }

    @Override
    public SystemLeafSetNode<T> build() {
        return new ImmutableLeafSetNode<>(nodeIdentifier, MapAdaptor.getDefaultInstance().optimize(value));
    }

    @Override
    public ImmutableLeafSetNodeBuilder<T> withNodeIdentifier(final NodeIdentifier withNodeIdentifier) {
        nodeIdentifier = withNodeIdentifier;
        return this;
    }

    @Override
    public ImmutableLeafSetNodeBuilder<T> withValue(final Collection<LeafSetEntryNode<T>> withValue) {
        for (final LeafSetEntryNode<T> leafSetEntry : withValue) {
            withChild(leafSetEntry);
        }
        return this;
    }

    @Override
    public ImmutableLeafSetNodeBuilder<T> withChildValue(final T childValue) {
        return withChild(new ImmutableLeafSetEntryNodeBuilder<T>()
            .withNodeIdentifier(new NodeWithValue<>(nodeIdentifier.getNodeType(), childValue))
            .withValue(childValue).build());
    }


    @Override
    public ImmutableLeafSetNodeBuilder<T> addChild(final LeafSetEntryNode<T> child) {
        return withChild(child);
    }

    @Override
    public ImmutableLeafSetNodeBuilder<T> removeChild(final PathArgument key) {
        return withoutChild(key);
    }

    protected static final class ImmutableLeafSetNode<T> extends AbstractSystemLeafSetNode<T> {
        private final @NonNull NodeIdentifier name;
        private final @NonNull Map<NodeWithValue<?>, LeafSetEntryNode<T>> children;

        ImmutableLeafSetNode(final NodeIdentifier name, final Map<NodeWithValue<?>, LeafSetEntryNode<T>> children) {
            this.name = requireNonNull(name);
            this.children = requireNonNull(children);
        }

        @Override
        public NodeIdentifier name() {
            body();
            return name;
        }

        @Override
        public LeafSetEntryNode<T> childByArg(final NodeWithValue<?> child) {
            return children.get(child);
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
        protected Collection<LeafSetEntryNode<T>> value() {
            return children.values();
        }

        @Override
        protected Collection<LeafSetEntryNode<T>> wrappedValue() {
            return UnmodifiableCollection.create(value());
        }

        @Override
        protected boolean valueEquals(final SystemLeafSetNode<T> other) {
            if (other instanceof ImmutableLeafSetNode<?> otherImmutable) {
                return children.equals(otherImmutable.children);
            }
            if (size() != other.size()) {
                return false;
            }
            for (var child : children.values()) {
                if (!child.equals(other.childByArg(child.name()))) {
                    return false;
                }
            }
            return true;
        }
    }
}
