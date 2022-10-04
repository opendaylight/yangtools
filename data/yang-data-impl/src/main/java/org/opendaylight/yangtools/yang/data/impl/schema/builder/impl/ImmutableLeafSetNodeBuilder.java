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
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.util.MapAdaptor;
import org.opendaylight.yangtools.util.UnmodifiableCollection;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.SystemLeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.builder.ListNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableNormalizedValueNode;

public class ImmutableLeafSetNodeBuilder<T> implements ListNodeBuilder<T, SystemLeafSetNode<T>> {
    private static final int DEFAULT_CAPACITY = 4;

    private final Map<NodeWithValue<?>, LeafSetEntryNode<T>> value;

    private NodeIdentifier nodeIdentifier;

    protected ImmutableLeafSetNodeBuilder() {
        value = new HashMap<>(DEFAULT_CAPACITY);
    }

    protected ImmutableLeafSetNodeBuilder(final int sizeHint) {
        if (sizeHint >= 0) {
            value = Maps.newHashMapWithExpectedSize(sizeHint);
        } else {
            value = new HashMap<>(DEFAULT_CAPACITY);
        }
    }

    protected ImmutableLeafSetNodeBuilder(final ImmutableLeafSetNode<T> node) {
        nodeIdentifier = node.getIdentifier();
        value = MapAdaptor.getDefaultInstance().takeSnapshot(node.children);
    }

    public static <T> @NonNull ListNodeBuilder<T, SystemLeafSetNode<T>> create() {
        return new ImmutableLeafSetNodeBuilder<>();
    }

    public static <T> @NonNull ListNodeBuilder<T, SystemLeafSetNode<T>> create(final int sizeHint) {
        return new ImmutableLeafSetNodeBuilder<>(sizeHint);
    }

    public static <T> @NonNull ListNodeBuilder<T, SystemLeafSetNode<T>> create(final SystemLeafSetNode<T> node) {
        if (node instanceof ImmutableLeafSetNode) {
            return new ImmutableLeafSetNodeBuilder<>((ImmutableLeafSetNode<T>) node);
        }
        throw new UnsupportedOperationException("Cannot initialize from class " + node.getClass());
    }

    @Override
    public ImmutableLeafSetNodeBuilder<T> withChild(final LeafSetEntryNode<T> child) {
        this.value.put(child.getIdentifier(), child);
        return this;
    }

    @Override
    public ImmutableLeafSetNodeBuilder<T> withoutChild(final PathArgument key) {
        this.value.remove(key);
        return this;
    }

    @Override
    public SystemLeafSetNode<T> build() {
        return new ImmutableLeafSetNode<>(nodeIdentifier, MapAdaptor.getDefaultInstance().optimize(value));
    }

    @Override
    public ImmutableLeafSetNodeBuilder<T> withNodeIdentifier(final NodeIdentifier withNodeIdentifier) {
        this.nodeIdentifier = withNodeIdentifier;
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
        return withChild(ImmutableLeafSetEntryNodeBuilder.<T>create()
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

    protected static final class ImmutableLeafSetNode<T>
            extends AbstractImmutableNormalizedValueNode<NodeIdentifier, SystemLeafSetNode<?>,
                Collection<@NonNull LeafSetEntryNode<T>>>
            implements SystemLeafSetNode<T> {

        private final Map<NodeWithValue<?>, LeafSetEntryNode<T>> children;

        ImmutableLeafSetNode(final NodeIdentifier nodeIdentifier,
                final Map<NodeWithValue<?>, LeafSetEntryNode<T>> children) {
            super(nodeIdentifier, UnmodifiableCollection.create(children.values()));
            this.children = children;
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
        protected Class<SystemLeafSetNode<?>> implementedType() {
            return (Class) SystemLeafSetNode.class;
        }

        @Override
        protected int valueHashCode() {
            return children.hashCode();
        }

        @Override
        protected boolean valueEquals(final SystemLeafSetNode<?> other) {
            if (other instanceof ImmutableLeafSetNode) {
                return children.equals(((ImmutableLeafSetNode<?>) other).children);
            }
            if (size() != other.size()) {
                return false;
            }
            for (var child : children.values()) {
                if (!child.equals(other.childByArg(child.getIdentifier()))) {
                    return false;
                }
            }
            return true;
        }
    }
}
