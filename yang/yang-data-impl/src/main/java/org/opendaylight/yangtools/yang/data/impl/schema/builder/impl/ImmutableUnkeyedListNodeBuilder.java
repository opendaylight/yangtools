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
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.api.schema.builder.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.data.api.schema.builder.NormalizedNodeContainerBuilder;
import org.opendaylight.yangtools.yang.data.ri.node.impl.ImmutableUnkeyedListNode;

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
        Iterables.addAll(value, node.body());
        this.dirty = true;
    }

    public static CollectionNodeBuilder<UnkeyedListEntryNode, UnkeyedListNode> create() {
        return new ImmutableUnkeyedListNodeBuilder();
    }

    public static CollectionNodeBuilder<UnkeyedListEntryNode, UnkeyedListNode> create(final int sizeHint) {
        return new ImmutableUnkeyedListNodeBuilder();
    }

    public static CollectionNodeBuilder<UnkeyedListEntryNode, UnkeyedListNode> create(
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
}
