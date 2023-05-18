/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

/**
 * Data subtree with cardinality 0..1 in the context of parent node.
 *
 * <p>
 * Node which does not have value but contains valid {@link DataContainerChild} nodes.
 *
 * <p>
 * Schema of this node is described by instance of
 * {@link org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode}.
 */
public interface ContainerNode extends DataContainerNode, DataContainerChild {
    @Override
    default Class<ContainerNode> contract() {
        return ContainerNode.class;
    }
}
