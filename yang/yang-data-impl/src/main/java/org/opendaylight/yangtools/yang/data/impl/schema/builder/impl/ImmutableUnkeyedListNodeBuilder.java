/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import java.util.LinkedList;
import java.util.List;

import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeContainerBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableNormalizedNode;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class ImmutableUnkeyedListNodeBuilder implements CollectionNodeBuilder<UnkeyedListEntryNode, UnkeyedListNode> {

    private List<UnkeyedListEntryNode> value;
    private InstanceIdentifier.NodeIdentifier nodeIdentifier;
    private boolean dirty = false;

    protected ImmutableUnkeyedListNodeBuilder() {
        this.value = new LinkedList<>();
        this.dirty = false;
    }

    protected ImmutableUnkeyedListNodeBuilder(final ImmutableUnkeyedListNode node) {
        this.value = new LinkedList<>();
        Iterables.addAll(value, node.getValue());
        this.dirty = true;
    }

    public static CollectionNodeBuilder<UnkeyedListEntryNode, UnkeyedListNode> create() {
        return new ImmutableUnkeyedListNodeBuilder();
    }

    public static CollectionNodeBuilder<UnkeyedListEntryNode, UnkeyedListNode> create(final UnkeyedListNode node) {
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
            final InstanceIdentifier.PathArgument key) {
        checkDirty();
        throw new UnsupportedOperationException("Children does not have identifiers.");
    }

    @Override
    public CollectionNodeBuilder<UnkeyedListEntryNode, UnkeyedListNode> withValue(final List<UnkeyedListEntryNode> value) {
        // TODO replace or putAll ?
        for (final UnkeyedListEntryNode UnkeyedListEntryNode : value) {
            withChild(UnkeyedListEntryNode);
        }

        return this;
    }

    @Override
    public CollectionNodeBuilder<UnkeyedListEntryNode, UnkeyedListNode> withNodeIdentifier(
            final InstanceIdentifier.NodeIdentifier nodeIdentifier) {
        this.nodeIdentifier = nodeIdentifier;
        return this;
    }

    @Override
    public UnkeyedListNode build() {
        dirty = true;
        return new ImmutableUnkeyedListNode(nodeIdentifier, ImmutableList.copyOf(value));
    }

    @Override
    public CollectionNodeBuilder<UnkeyedListEntryNode, UnkeyedListNode> addChild(final UnkeyedListEntryNode child) {
        return withChild(child);
    }

    @Override
    public NormalizedNodeContainerBuilder<NodeIdentifier, PathArgument, UnkeyedListEntryNode, UnkeyedListNode> removeChild(
            final PathArgument key) {
        return withoutChild(key);
    }

    protected static final class ImmutableUnkeyedListNode extends
            AbstractImmutableNormalizedNode<InstanceIdentifier.NodeIdentifier, Iterable<UnkeyedListEntryNode>>
            implements Immutable, UnkeyedListNode {

        private final ImmutableList<UnkeyedListEntryNode> children;

        ImmutableUnkeyedListNode(final InstanceIdentifier.NodeIdentifier nodeIdentifier,
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
