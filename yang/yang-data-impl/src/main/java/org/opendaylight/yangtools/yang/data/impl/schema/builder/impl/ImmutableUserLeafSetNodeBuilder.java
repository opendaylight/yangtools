/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UserLeafSetNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.ListNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableNormalizedNode;

public class ImmutableUserLeafSetNodeBuilder<T> implements ListNodeBuilder<T, UserLeafSetNode<T>> {
    private NodeIdentifier nodeIdentifier;
    private List<T> values;
    private boolean dirty;

    protected ImmutableUserLeafSetNodeBuilder() {
        values = new ArrayList<>();
        dirty = false;
    }

    protected ImmutableUserLeafSetNodeBuilder(final ImmutableUserLeafSetNode<T> node) {
        nodeIdentifier = node.getIdentifier();
        values = node.values();
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
            values = new ArrayList<>(values);
            dirty = false;
        }
    }

    @Override
    public ImmutableUserLeafSetNodeBuilder<T> withChild(final LeafSetEntryNode<T> child) {
        final T value = child.body();
        if (!values.contains(child)) {
            checkDirty();
            values.add(value);
        }
        return this;
    }

    @Override
    public ImmutableUserLeafSetNodeBuilder<T> withoutChild(final PathArgument key) {
        checkDirty();
        this.values.remove(key);
        return this;
    }

    @Override
    public UserLeafSetNode<T> build() {
        dirty = true;
        return new ImmutableUserLeafSetNode<>(nodeIdentifier, values);
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
            extends AbstractImmutableNormalizedNode<NodeIdentifier, UserLeafSetNode<?>>
            implements UserLeafSetNode<T> {
        private final List<T> values;

        ImmutableUserLeafSetNode(final NodeIdentifier nodeIdentifier, final List<T> values) {
            super(nodeIdentifier);
            this.values = ImmutableList.copyOf(values);
        }

        @Override
        public List<T> values() {
            return values;
        }

        @Override
        @SuppressWarnings("unchecked")
        public LeafSetEntryNode<T> childByArg(final NodeWithValue<?> child) {
            return values.contains(child.getValue()) ? ImmutableNodes.leafSetEntry((NodeWithValue<T>) child) : null;
        }

        @Override
        public LeafSetEntryNode<T> getChild(final int position) {
            return ImmutableNodes.leafSetEntry(getIdentifier().getNodeType(), values.get(position));
        }

        @Override
        public int size() {
            return values.size();
        }

        @Override
        public Collection<LeafSetEntryNode<T>> body() {
            final QName name = getIdentifier().getNodeType();
            return Lists.transform(values, value -> ImmutableNodes.leafSetEntry(name, value));
        }

        @Override
        protected Class<UserLeafSetNode<?>> implementedType() {
            return (Class) UserLeafSetNode.class;
        }

        @Override
        protected int valueHashCode() {
            return values.hashCode();
        }

        @Override
        protected boolean valueEquals(final UserLeafSetNode<?> other) {
            return values.equals(((ImmutableUserLeafSetNode<?>) other).values);
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
