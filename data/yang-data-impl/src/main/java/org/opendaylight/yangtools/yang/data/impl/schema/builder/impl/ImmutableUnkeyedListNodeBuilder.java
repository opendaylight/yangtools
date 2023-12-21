/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.api.schema.builder.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.data.api.schema.builder.NormalizedNodeContainerBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableNormalizedValueNode;
import org.opendaylight.yangtools.yang.data.spi.node.AbstractNormalizedNode;

public final class ImmutableUnkeyedListNodeBuilder implements UnkeyedListNode.Builder {
    private List<UnkeyedListEntryNode> value;
    private NodeIdentifier nodeIdentifier;
    private boolean dirty;

    public ImmutableUnkeyedListNodeBuilder() {
        value = new LinkedList<>();
        dirty = false;
    }

    public ImmutableUnkeyedListNodeBuilder(final int sizeHint) {
        this();
    }


    protected ImmutableUnkeyedListNodeBuilder(final ImmutableUnkeyedListNode node) {
        nodeIdentifier = node.name();
        // FIXME: clean this up, notably reuse unmodified lists
        value = new LinkedList<>();
        Iterables.addAll(value, node.body());
        dirty = true;
    }

    public static UnkeyedListNode.@NonNull Builder create(final UnkeyedListNode node) {
        if (!(node instanceof ImmutableUnkeyedListNode immutableNode)) {
            throw new UnsupportedOperationException("Cannot initialize from class " + node.getClass());
        }
        return new ImmutableUnkeyedListNodeBuilder(immutableNode);
    }

    private void checkDirty() {
        if (dirty) {
            value = new LinkedList<>(value);
            dirty = false;
        }
    }

    @Override
    public CollectionNodeBuilder<UnkeyedListEntryNode, UnkeyedListNode> withChild(final UnkeyedListEntryNode child) {
        checkDirty();
        value.add(child);
        return this;
    }

    @Override
    public CollectionNodeBuilder<UnkeyedListEntryNode, UnkeyedListNode> withoutChild(
            final PathArgument key) {
        checkDirty();
        throw new UnsupportedOperationException("Children does not have identifiers.");
    }

    @Override
    public CollectionNodeBuilder<UnkeyedListEntryNode, UnkeyedListNode> withValue(
            final Collection<UnkeyedListEntryNode> withValue) {
        // TODO replace or putAll ?
        for (final UnkeyedListEntryNode node : withValue) {
            withChild(node);
        }

        return this;
    }

    @Override
    public CollectionNodeBuilder<UnkeyedListEntryNode, UnkeyedListNode> withNodeIdentifier(
            final NodeIdentifier withNodeIdentifier) {
        nodeIdentifier = withNodeIdentifier;
        return this;
    }

    @Override
    public UnkeyedListNode build() {
        dirty = true;
        if (value.isEmpty()) {
            return new EmptyImmutableUnkeyedListNode(nodeIdentifier);
        }
        return new ImmutableUnkeyedListNode(nodeIdentifier, ImmutableList.copyOf(value));
    }

    @Override
    public CollectionNodeBuilder<UnkeyedListEntryNode, UnkeyedListNode> addChild(final UnkeyedListEntryNode child) {
        return withChild(child);
    }

    @Override
    public NormalizedNodeContainerBuilder<NodeIdentifier, PathArgument, UnkeyedListEntryNode, UnkeyedListNode>
            removeChild(final PathArgument key) {
        return withoutChild(key);
    }

    protected static final class EmptyImmutableUnkeyedListNode
            extends AbstractNormalizedNode<NodeIdentifier, UnkeyedListNode> implements UnkeyedListNode {
        protected EmptyImmutableUnkeyedListNode(final NodeIdentifier nodeIdentifier) {
            super(nodeIdentifier);
        }

        @Override
        public ImmutableList<UnkeyedListEntryNode> body() {
            return ImmutableList.of();
        }

        @Override
        public UnkeyedListEntryNode childAt(final int position) {
            throw new IndexOutOfBoundsException();
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        protected Class<UnkeyedListNode> implementedType() {
            return UnkeyedListNode.class;
        }

        @Override
        protected int valueHashCode() {
            return 1;
        }

        @Override
        protected boolean valueEquals(final UnkeyedListNode other) {
            return other.isEmpty();
        }
    }

    protected static final class ImmutableUnkeyedListNode
            extends AbstractImmutableNormalizedValueNode<NodeIdentifier, UnkeyedListNode,
                Collection<@NonNull UnkeyedListEntryNode>>
            implements UnkeyedListNode {

        private final ImmutableList<UnkeyedListEntryNode> children;

        ImmutableUnkeyedListNode(final NodeIdentifier nodeIdentifier,
                final ImmutableList<UnkeyedListEntryNode> children) {
            super(nodeIdentifier, children);
            this.children = children;
        }

        @Override
        public UnkeyedListEntryNode childAt(final int position) {
            return children.get(position);
        }

        @Override
        public int size() {
            return children.size();
        }

        @Override
        protected Class<UnkeyedListNode> implementedType() {
            return UnkeyedListNode.class;
        }

        @Override
        protected int valueHashCode() {
            return children.hashCode();
        }

        @Override
        protected boolean valueEquals(final UnkeyedListNode other) {
            final Collection<UnkeyedListEntryNode> otherChildren;
            if (other instanceof ImmutableUnkeyedListNode immutableOther) {
                otherChildren = immutableOther.children;
            } else {
                otherChildren = other.body();
            }
            return otherChildren instanceof List ? children.equals(otherChildren)
                : Iterables.elementsEqual(children, otherChildren);
        }
    }
}
