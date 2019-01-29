/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.tree.NormalizedNodeContainerSupport.Single;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;

/**
 * General container modification strategy. Used by {@link PresenceContainerModificationStrategy} via subclassing
 * and by {@link StructuralContainerModificationStrategy} as a delegate.
 */
class ContainerModificationStrategy extends AbstractDataNodeContainerModificationStrategy<ContainerSchemaNode> {
    private static final Single<NodeIdentifier, ContainerNode> SUPPORT = new Single<>(ContainerNode.class,
            ImmutableContainerNodeBuilder::create, ImmutableContainerNodeBuilder::create);

    ContainerModificationStrategy(final ContainerSchemaNode schemaNode, final DataTreeConfiguration treeConfig) {
        super(ContainerNode.class, SUPPORT, schemaNode, treeConfig);
    }
}
