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
import org.opendaylight.yangtools.yang.data.api.schema.AbstractChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.LazyLeafOperations;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.LazyValues;

public final class ImmutableChoiceNodeBuilder
        extends AbstractImmutableDataContainerNodeBuilder<NodeIdentifier, ChoiceNode>
        implements ChoiceNode.Builder {
    public ImmutableChoiceNodeBuilder() {

    }

    public ImmutableChoiceNodeBuilder(final int sizeHint) {
        super(sizeHint);
    }

    private ImmutableChoiceNodeBuilder(final ImmutableChoiceNode node) {
        super(node.name, node.children);
    }

    public static ChoiceNode.@NonNull Builder create(final ChoiceNode node) {
        if (node instanceof ImmutableChoiceNode immutableNode) {
            return new ImmutableChoiceNodeBuilder(immutableNode);
        }
        throw new UnsupportedOperationException("Cannot initialize from class " + node.getClass());
    }

    @Override
    public ChoiceNode build() {
        return new ImmutableChoiceNode(getNodeIdentifier(), buildValue());
    }

    private static final class ImmutableChoiceNode extends AbstractChoiceNode {
        private final @NonNull NodeIdentifier name;
        private final @NonNull Map<NodeIdentifier, Object> children;

        ImmutableChoiceNode(final NodeIdentifier name, final Map<NodeIdentifier, Object> children) {
            this.name = requireNonNull(name);
            // FIXME: move this to caller
            this.children = ImmutableOffsetMap.unorderedCopyOf(children);
        }

        @Override
        public NodeIdentifier name() {
            return name;
        }

        @Override
        public DataContainerChild childByArg(final NodeIdentifier child) {
            return LazyLeafOperations.getChild(children, child);
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
        protected boolean valueEquals(final ChoiceNode other) {
            return other instanceof ImmutableChoiceNode immutable ? children.equals(immutable.children)
                : ImmutableNormalizedNodeMethods.bodyEquals(this, other);
        }
    }
}
