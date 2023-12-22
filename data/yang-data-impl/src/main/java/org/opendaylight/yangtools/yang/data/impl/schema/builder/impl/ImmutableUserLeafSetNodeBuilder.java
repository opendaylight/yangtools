/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
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
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.spi.node.AbstractNormalizedNode;

public final class ImmutableUserLeafSetNodeBuilder<T> implements UserLeafSetNode.Builder<T> {
    private Map<NodeWithValue<T>, LeafSetEntryNode<T>> value;
    private NodeIdentifier nodeIdentifier;
    private boolean dirty;

    public ImmutableUserLeafSetNodeBuilder() {
        value = new LinkedHashMap<>();
        dirty = false;
    }

    public ImmutableUserLeafSetNodeBuilder(final int sizeHint) {
        value = Maps.newLinkedHashMapWithExpectedSize(sizeHint);
        dirty = false;
    }

    ImmutableUserLeafSetNodeBuilder(final ImmutableUserLeafSetNode<T> node) {
        nodeIdentifier = node.name();
        value = node.getChildren();
        dirty = true;
    }

    public static <T> UserLeafSetNode.@NonNull Builder<T> create(final UserLeafSetNode<T> node) {
        if (node instanceof ImmutableUserLeafSetNode<?>) {
            return new ImmutableUserLeafSetNodeBuilder<>((ImmutableUserLeafSetNode<T>) node);
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
    public ImmutableUserLeafSetNodeBuilder<T> withChild(final LeafSetEntryNode<T> child) {
        checkDirty();
        value.put(child.name(), child);
        return this;
    }

    @Override
    public ImmutableUserLeafSetNodeBuilder<T> withoutChild(final PathArgument key) {
        checkDirty();
        value.remove(key);
        return this;
    }

    @Override
    public UserLeafSetNode<T> build() {
        dirty = true;
        return new ImmutableUserLeafSetNode<>(nodeIdentifier, value);
    }

    @Override
    public ImmutableUserLeafSetNodeBuilder<T> withNodeIdentifier(final NodeIdentifier withNodeIdentifier) {
        nodeIdentifier = withNodeIdentifier;
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
        return withChild(Builders.<T>leafSetEntryBuilder()
            .withNodeIdentifier(new NodeWithValue<>(nodeIdentifier.getNodeType(), childValue))
            .withValue(childValue).build());
    }

    protected static final class ImmutableUserLeafSetNode<T>
            extends AbstractNormalizedNode<NodeIdentifier, UserLeafSetNode<?>>
            implements UserLeafSetNode<T> {
        private final Map<NodeWithValue<T>, LeafSetEntryNode<T>> children;

        ImmutableUserLeafSetNode(final NodeIdentifier nodeIdentifier,
                final Map<NodeWithValue<T>, LeafSetEntryNode<T>> children) {
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
            if (other instanceof ImmutableUserLeafSetNode<?> immutableOther) {
                return children.equals(immutableOther.children);
            }
            // Note: performs a size() check first
            return Iterables.elementsEqual(children.values(), other.body());
        }

        private Map<NodeWithValue<T>, LeafSetEntryNode<T>> getChildren() {
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
