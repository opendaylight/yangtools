/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.spi.node.AbstractNormalizedNode;

final class EmptyImmutableUnkeyedListNode
        extends AbstractNormalizedNode<NodeIdentifier, UnkeyedListNode> implements UnkeyedListNode {
    protected EmptyImmutableUnkeyedListNode(final NodeIdentifier nodeIdentifier) {
        super(nodeIdentifier);
    }

    @Override
    public ImmutableList<UnkeyedListEntryNode> body() {
        return ImmutableList.of();
    }

    @Override
    public UnkeyedListEntryNode getChild(final int position) {
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