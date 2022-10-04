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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.util.UnmodifiableCollection;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UserLeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.builder.ListNodeBuilder;
import org.opendaylight.yangtools.yang.data.spi.node.AbstractNormalizedNode;

public class ImmutableUserLeafSetNodeBuilder<T> implements ListNodeBuilder<T, UserLeafSetNode<T>> {
    private Map<NodeWithValue, LeafSetEntryNode<T>> value;
    private NodeIdentifier nodeIdentifier;
    private boolean dirty;

    ImmutableUserLeafSetNodeBuilder() {
        value = new LinkedHashMap<>();
        dirty = false;
    }

    ImmutableUserLeafSetNodeBuilder(final ImmutableUserLeafSetNode<T> node) {
        nodeIdentifier = node.getIdentifier();
        value = node.getChildren();
        dirty = true;
    }

    public static <T> @NonNull ListNodeBuilder<T, UserLeafSetNode<T>> create() {
        return new ImmutableUserLeafSetNodeBuilder<>();
    }

    public static <T> @NonNull ListNodeBuilder<T, UserLeafSetNode<T>> create(
            final UserLeafSetNode<T> node) {
        if (!(node instanceof ImmutableUserLeafSetNode<?>)) {
            throw new UnsupportedOperationException(String.format("Cannot initialize from class %s", node.getClass()));
        }

        return new ImmutableUserLeafSetNodeBuilder<>((ImmutableUserLeafSetNode<T>) node);
    }

    private void checkDirty() {
        if (dirty) {
            value = new LinkedHashMap<>(value);
            dirty = false;
        }
    }

    @Override
    public ImmutableUserLeafSetNodeBuilder<T> withChild(final LeafSetEntryNode<T> child) {
        checkDirty();
        this.value.put(child.getIdentifier(), child);
        return this;
    }

    @Override
    public ImmutableUserLeafSetNodeBuilder<T> withoutChild(final PathArgument key) {
        checkDirty();
        this.value.remove(key);
        return this;
    }

    @Override
    public UserLeafSetNode<T> build() {
        dirty = true;
        return new ImmutableUserLeafSetNode<>(nodeIdentifier, value);
    }

    @Override
    public ImmutableUserLeafSetNodeBuilder<T> withNodeIdentifier(final NodeIdentifier withNodeIdentifier) {
        this.nodeIdentifier = withNodeIdentifier;
        return this;
    }

    @Override
    public ImmutableUserLeafSetNodeBuilder<T> withValue(final Collection<LeafSetEntryNode<T>> withValue) {
        checkDirty();
        for (final LeafSetEntryNode<T> leafSetEntry : withValue) {
            withChild(leafSetEntry);
        }
        return this;
    }

    @Override
    public ImmutableUserLeafSetNodeBuilder<T> withChildValue(final T childValue) {
        return withChild(ImmutableLeafSetEntryNodeBuilder.<T>create()
            .withNodeIdentifier(new NodeWithValue<>(nodeIdentifier.getNodeType(), childValue))
            .withValue(childValue).build());
    }

    protected static final class ImmutableUserLeafSetNode<T>
            extends AbstractNormalizedNode<NodeIdentifier, UserLeafSetNode<?>>
            implements UserLeafSetNode<T> {
        private final Map<NodeWithValue, LeafSetEntryNode<T>> children;

        ImmutableUserLeafSetNode(final NodeIdentifier nodeIdentifier,
                final Map<NodeWithValue, LeafSetEntryNode<T>> children) {
            super(nodeIdentifier);
            this.children = children;
        }

        @Override
        public LeafSetEntryNode<T> childByArg(final NodeWithValue child) {
            return children.get(child);
        }

        @Override
        public LeafSetEntryNode<T> childAt(final int position) {
            return Iterables.get(children.values(), position);
        }

        @Override
        public int size() {
            return children.size();
        }

        @Override
        public Collection<LeafSetEntryNode<T>> body() {
            return UnmodifiableCollection.create(children.values());
        }

        @Override
        protected Class<UserLeafSetNode<?>> implementedType() {
            return (Class) UserLeafSetNode.class;
        }

        @Override
        protected int valueHashCode() {
            return children.hashCode();
        }

        @Override
        protected boolean valueEquals(final UserLeafSetNode<?> other) {
            if (other instanceof ImmutableUserLeafSetNode) {
                return children.equals(((ImmutableUserLeafSetNode<?>) other).children);
            }
            // Note: performs a size() check first
            return Iterables.elementsEqual(children.values(), other.body());
        }

        private Map<NodeWithValue, LeafSetEntryNode<T>> getChildren() {
            return Collections.unmodifiableMap(children);
        }
    }

    @Override
    public ImmutableUserLeafSetNodeBuilder<T> addChild(final LeafSetEntryNode<T> child) {
        return withChild(child);
    }

    @Override
    public ImmutableUserLeafSetNodeBuilder<T> removeChild(final PathArgument key) {
        return withoutChild(key);
    }
}
