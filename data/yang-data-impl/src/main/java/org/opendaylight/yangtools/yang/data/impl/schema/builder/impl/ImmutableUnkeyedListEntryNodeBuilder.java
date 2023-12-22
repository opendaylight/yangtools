/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.util.ImmutableOffsetMap;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AbstractUnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.builder.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.LazyLeafOperations;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.LazyValues;

public final class ImmutableUnkeyedListEntryNodeBuilder
        extends AbstractImmutableDataContainerNodeBuilder<NodeIdentifier, UnkeyedListEntryNode>
        implements UnkeyedListEntryNode.Builder {
    public ImmutableUnkeyedListEntryNodeBuilder() {

    }

    public ImmutableUnkeyedListEntryNodeBuilder(final int sizeHint) {
        super(sizeHint);
    }

    private ImmutableUnkeyedListEntryNodeBuilder(final ImmutableUnkeyedListEntryNode node) {
        super(node.name, node.children);
    }

    public static @NonNull DataContainerNodeBuilder<NodeIdentifier, UnkeyedListEntryNode> create(
            final UnkeyedListEntryNode node) {
        if (node instanceof ImmutableUnkeyedListEntryNode immutableNode) {
            return new ImmutableUnkeyedListEntryNodeBuilder(immutableNode);
        }
        throw new UnsupportedOperationException("Cannot initialize from class " + node.getClass());
    }

    @Override
    public UnkeyedListEntryNode build() {
        return new ImmutableUnkeyedListEntryNode(getNodeIdentifier(), buildValue());
    }

    protected static final class ImmutableUnkeyedListEntryNode extends AbstractUnkeyedListEntryNode {
        private final @NonNull NodeIdentifier name;
        private final @NonNull Map<NodeIdentifier, Object> children;

        ImmutableUnkeyedListEntryNode(final NodeIdentifier name, final Map<NodeIdentifier, Object> children) {
            this.name = requireNonNull(name);
            // FIXME: move this to caller
            this.children = ImmutableOffsetMap.unorderedCopyOf(children);
        }

        @Override
        public NodeIdentifier name() {
            return name;
        }

        @Override
        public DataContainerChild childByArg(final NodeIdentifier key) {
            return LazyLeafOperations.getChild(children, key);
        }

        @Override
        public Collection<DataContainerChild> body() {
            return new LazyValues(children);
        }

        @Override
        public int size() {
            return children.size();
        }

        @Override
        protected int valueHashCode() {
            return children.hashCode();
        }

        @Override
        protected boolean valueEquals(final UnkeyedListEntryNode other) {
            return other instanceof ImmutableUnkeyedListEntryNode immutable ? children.equals(immutable.children)
                : ImmutableNormalizedNodeMethods.bodyEquals(this, other);
        }
    }
}
