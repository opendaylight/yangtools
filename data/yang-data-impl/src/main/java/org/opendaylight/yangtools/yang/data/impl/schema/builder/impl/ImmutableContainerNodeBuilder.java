/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableDataContainerNode;

public final class ImmutableContainerNodeBuilder
        extends AbstractImmutableDataContainerNodeBuilder<NodeIdentifier, ContainerNode>
        implements ContainerNode.Builder {
    public ImmutableContainerNodeBuilder() {

    }

    public ImmutableContainerNodeBuilder(final int sizeHint) {
        super(sizeHint);
    }

    private ImmutableContainerNodeBuilder(final ImmutableContainerNode node) {
        super(node);
    }

    public static ContainerNode.@NonNull Builder create(final ContainerNode node) {
        if (node instanceof ImmutableContainerNode immutableNode) {
            return new ImmutableContainerNodeBuilder(immutableNode);
        }
        throw new UnsupportedOperationException("Cannot initialize from class " + node.getClass());
    }

    @Override
    public ContainerNode build() {
        return new ImmutableContainerNode(getNodeIdentifier(), buildValue());
    }

    protected static final class ImmutableContainerNode
            extends AbstractImmutableDataContainerNode<NodeIdentifier, ContainerNode> implements ContainerNode {
        ImmutableContainerNode(final NodeIdentifier nodeIdentifier, final Map<NodeIdentifier, Object> children) {
            super(children, nodeIdentifier);
        }

        @Override
        protected Class<ContainerNode> implementedType() {
            return ContainerNode.class;
        }
    }
}
