/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;

/**
 * General container modification strategy. This is used by {@link PresenceContainerModificationStrategy}, which needs
 * to tap into {@link SchemaAwareApplyOperation}'s operations, or wrapped through {@link AutomaticLifecycleMixin} when
 * the container is a structural one.
 */
class ContainerModificationStrategy extends AbstractDataNodeContainerModificationStrategy<ContainerSchemaNode> {
    ContainerModificationStrategy(final ContainerSchemaNode schemaNode, final DataTreeConfiguration treeConfig) {
        super(schemaNode, ContainerNode.class, treeConfig);
    }

    static ModificationApplyOperation of(final ContainerSchemaNode schema, final DataTreeConfiguration treeConfig) {
        return schema.isPresenceContainer() ? new PresenceContainerModificationStrategy(schema, treeConfig)
                : new AutomaticLifecycleMixin(new ContainerModificationStrategy(schema, treeConfig),
                    ImmutableNodes.containerNode(schema.getQName()));
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected final DataContainerNodeBuilder createBuilder(final NormalizedNode<?, ?> original) {
        checkArgument(original instanceof ContainerNode);
        return ImmutableContainerNodeBuilder.create((ContainerNode) original);
    }

    @Override
    protected NormalizedNode<?, ?> createEmptyValue(final NormalizedNode<?, ?> original) {
        Preconditions.checkArgument(original instanceof ContainerNode);
        return ImmutableContainerNodeBuilder.create().withNodeIdentifier(((ContainerNode) original).getIdentifier())
                .build();
    }
}
