/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.builder.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.ImmutableContainerNode;

public final class ImmutableContainerNodeBuilder
        extends AbstractImmutableDataContainerNodeBuilder<NodeIdentifier, ContainerNode> {
    ImmutableContainerNodeBuilder() {

    }

    ImmutableContainerNodeBuilder(final int sizeHint) {
        super(sizeHint);
    }

    ImmutableContainerNodeBuilder(final ImmutableContainerNode node) {
        super(node);
    }

    public static @NonNull DataContainerNodeBuilder<NodeIdentifier, ContainerNode> create() {
        return new ImmutableContainerNodeBuilder();
    }

    public static @NonNull DataContainerNodeBuilder<NodeIdentifier, ContainerNode> create(final int sizeHint) {
        return new ImmutableContainerNodeBuilder(sizeHint);
    }

    public static @NonNull DataContainerNodeBuilder<NodeIdentifier, ContainerNode> create(final ContainerNode node) {
        if (!(node instanceof ImmutableContainerNode)) {
            throw new UnsupportedOperationException(String.format("Cannot initialize from class %s", node.getClass()));
        }
        return new ImmutableContainerNodeBuilder((ImmutableContainerNode) node);
    }

    @Override
    public ContainerNode build() {
        return new ImmutableContainerNode(getNodeIdentifier(), buildValue());
    }
}
