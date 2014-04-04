/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import java.util.Map;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeAttrBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableDataContainerAttrNode;

public class ImmutableContainerNodeBuilder extends
        AbstractImmutableDataContainerNodeAttrBuilder<InstanceIdentifier.NodeIdentifier, ContainerNode> {

    protected ImmutableContainerNodeBuilder() {
        super();
    }

    protected ImmutableContainerNodeBuilder(final ImmutableContainerNode node) {
        super(node);
    }

    public static DataContainerNodeAttrBuilder<InstanceIdentifier.NodeIdentifier, ContainerNode> create() {
        return new ImmutableContainerNodeBuilder();
    }

    public static DataContainerNodeAttrBuilder<InstanceIdentifier.NodeIdentifier, ContainerNode> create(final ContainerNode node) {
        if (!(node instanceof ImmutableContainerNode)) {
            throw new UnsupportedOperationException(String.format("Cannot initialize from class %s", node.getClass()));
        }
        return new ImmutableContainerNodeBuilder((ImmutableContainerNode) node);
    }

    @Override
    public ContainerNode build() {
        return new ImmutableContainerNode(getNodeIdentifier(), buildValue(), getAttributes());
    }

    protected static final class ImmutableContainerNode extends
            AbstractImmutableDataContainerAttrNode<InstanceIdentifier.NodeIdentifier> implements ContainerNode {

        ImmutableContainerNode(
                final InstanceIdentifier.NodeIdentifier nodeIdentifier,
                final Map<InstanceIdentifier.PathArgument, DataContainerChild<? extends InstanceIdentifier.PathArgument, ?>> children,
                final Map<QName, String> attributes) {
            super(children, nodeIdentifier, attributes);
        }
    }
}
