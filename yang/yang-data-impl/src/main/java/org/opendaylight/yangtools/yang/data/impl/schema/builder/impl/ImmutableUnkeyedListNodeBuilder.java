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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeContainerBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableNormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableNormalizedValueNode;

public class ImmutableUnkeyedListNodeBuilder implements CollectionNodeBuilder<UnkeyedListEntryNode, UnkeyedListNode> {
    private List<UnkeyedListEntryNode> value;
    private NodeIdentifier nodeIdentifier;
    private boolean dirty;

    protected ImmutableUnkeyedListNodeBuilder() {
        this.value = new LinkedList<>();
        this.dirty = false;
    }

    protected ImmutableUnkeyedListNodeBuilder(final ImmutableUnkeyedListNode node) {
        this.nodeIdentifier = node.getIdentifier();
        // FIXME: clean this up, notably reuse unmodified lists
        this.value = new LinkedList<>();
        Iterables.addAll(value, node.getValue());
        this.dirty = true;
    }

    public static @NonNull CollectionNodeBuilder<UnkeyedListEntryNode, UnkeyedListNode> create() {
        return new ImmutableUnkeyedListNodeBuilder();
    }

    public static @NonNull CollectionNodeBuilder<UnkeyedListEntryNode, UnkeyedListNode> create(final int sizeHint) {
        return new ImmutableUnkeyedListNodeBuilder();
    }

    public static @NonNull CollectionNodeBuilder<UnkeyedListEntryNode, UnkeyedListNode> create(
            final UnkeyedListNode node) {
        if (!(node instanceof ImmutableUnkeyedListNode)) {
            throw new UnsupportedOperationException(String.format("Cannot initialize from class %s", node.getClass()));
        }

        return new ImmutableUnkeyedListNodeBuilder((ImmutableUnkeyedListNode) node);
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
        this.value.add(child);
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
        this.nodeIdentifier = withNodeIdentifier;
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

    protected static final class EmptyImmutableUnkeyedListNode extends
            AbstractImmutableNormalizedNode<NodeIdentifier, Collection<UnkeyedListEntryNode>>
            implements UnkeyedListNode {
        protected EmptyImmutableUnkeyedListNode(final NodeIdentifier nodeIdentifier) {
            super(nodeIdentifier);
        }

        @Override
        public ImmutableList<UnkeyedListEntryNode> getValue() {
            return ImmutableList.of();
        }

        @Override
        public UnkeyedListEntryNode getChild(final int position) {
            throw new IndexOutOfBoundsException();
        }

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        protected boolean valueEquals(final AbstractImmutableNormalizedNode<?, ?> other) {
            return Collections.EMPTY_LIST.equals(other.getValue());
        }

        @Override
        protected int valueHashCode() {
            return Collections.EMPTY_LIST.hashCode();
        }
    }

    protected static final class ImmutableUnkeyedListNode extends
            AbstractImmutableNormalizedValueNode<NodeIdentifier, Collection<UnkeyedListEntryNode>>
            implements UnkeyedListNode {

        private final ImmutableList<UnkeyedListEntryNode> children;

        ImmutableUnkeyedListNode(final NodeIdentifier nodeIdentifier,
                final ImmutableList<UnkeyedListEntryNode> children) {
            super(nodeIdentifier, children);
            this.children = children;
        }

        @Override
        protected int valueHashCode() {
            return children.hashCode();
        }

        @Override
        protected boolean valueEquals(final AbstractImmutableNormalizedNode<?, ?> other) {
            return children.equals(((ImmutableUnkeyedListNode) other).children);
        }

        @Override
        public UnkeyedListEntryNode getChild(final int position) {
            return children.get(position);
        }

        @Override
        public int getSize() {
            return children.size();
        }
    }
}
