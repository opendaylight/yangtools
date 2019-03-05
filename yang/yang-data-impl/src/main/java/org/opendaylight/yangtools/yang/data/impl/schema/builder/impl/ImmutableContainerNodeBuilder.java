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
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeAttrBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableDataContainerNode;

public class ImmutableContainerNodeBuilder
        extends AbstractImmutableDataContainerNodeAttrBuilder<NodeIdentifier, ContainerNode> {

    protected ImmutableContainerNodeBuilder() {

    }

    protected ImmutableContainerNodeBuilder(final int sizeHint) {
        super(sizeHint);
    }

    protected ImmutableContainerNodeBuilder(final ImmutableContainerNode node) {
        super(node);
    }

    public static @NonNull DataContainerNodeAttrBuilder<NodeIdentifier, ContainerNode> create() {
        return new ImmutableContainerNodeBuilder();
    }

    public static @NonNull DataContainerNodeAttrBuilder<NodeIdentifier, ContainerNode> create(final int sizeHint) {
        return new ImmutableContainerNodeBuilder(sizeHint);
    }

    public static @NonNull DataContainerNodeAttrBuilder<NodeIdentifier, ContainerNode> create(
            final ContainerNode node) {
        if (!(node instanceof ImmutableContainerNode)) {
            throw new UnsupportedOperationException(String.format("Cannot initialize from class %s", node.getClass()));
        }
        return new ImmutableContainerNodeBuilder((ImmutableContainerNode) node);
    }

    @Override
    public ContainerNode build() {
        return new ImmutableContainerNode(getNodeIdentifier(), buildValue());
    }

    protected static final class ImmutableContainerNode extends AbstractImmutableDataContainerNode<NodeIdentifier>
            implements ContainerNode {

        ImmutableContainerNode(final NodeIdentifier nodeIdentifier,
                final Map<PathArgument, DataContainerChild<? extends PathArgument, ?>> children) {
            super(children, nodeIdentifier);
        }
    }
}
