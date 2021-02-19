/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.ri.node.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;

public final class ImmutableUnkeyedListNode
        extends AbstractImmutableNormalizedValueNode<NodeIdentifier, UnkeyedListNode,
            Collection<@NonNull UnkeyedListEntryNode>>
        implements UnkeyedListNode {

    private final ImmutableList<UnkeyedListEntryNode> children;

    public ImmutableUnkeyedListNode(final NodeIdentifier nodeIdentifier,
            final ImmutableList<UnkeyedListEntryNode> children) {
        super(nodeIdentifier, children);
        this.children = children;
    }

    @Override
    public UnkeyedListEntryNode getChild(final int position) {
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
        if (other instanceof ImmutableUnkeyedListNode) {
            otherChildren = ((ImmutableUnkeyedListNode) other).children;
        } else {
            otherChildren = other.body();
        }
        return otherChildren instanceof List ? children.equals(otherChildren)
            : Iterables.elementsEqual(children, otherChildren);
    }
}