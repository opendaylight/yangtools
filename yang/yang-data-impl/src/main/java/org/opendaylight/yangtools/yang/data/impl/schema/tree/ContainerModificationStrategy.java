/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static com.google.common.base.Preconditions.checkArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TreeType;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;

/**
 * General container modification strategy. Used by {@link PresenceContainerModificationStrategy} via subclassing
 * and by {@link StructuralContainerModificationStrategy} as a delegate.
 */
class ContainerModificationStrategy extends AbstractDataNodeContainerModificationStrategy<ContainerSchemaNode> {
    ContainerModificationStrategy(final ContainerSchemaNode schemaNode, final TreeType treeType) {
        super(schemaNode, ContainerNode.class, treeType);
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected final DataContainerNodeBuilder createBuilder(final NormalizedNode<?, ?> original) {
        checkArgument(original instanceof ContainerNode);
        return ImmutableContainerNodeBuilder.create((ContainerNode) original);
    }
}
