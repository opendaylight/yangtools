/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static com.google.common.base.Preconditions.checkArgument;

import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeContainerBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafSetNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;

final class UnorderedLeafSetModificationStrategy extends AbstractLeafSetModificationStrategy {
    UnorderedLeafSetModificationStrategy(final LeafListSchemaNode schema, final DataTreeConfiguration treeConfig) {
        super(schema, treeConfig);
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected NormalizedNodeContainerBuilder createBuilder(final NormalizedNode<?, ?> original) {
        checkArgument(original instanceof LeafSetNode<?>);
        return ImmutableLeafSetNodeBuilder.create((LeafSetNode<?>) original);
    }

    @Override
    protected NormalizedNode<?, ?> createEmptyValue(final NormalizedNode<?, ?> original) {
        checkArgument(original instanceof LeafSetNode<?>);
        return ImmutableLeafSetNodeBuilder.create().withNodeIdentifier(((LeafSetNode<?>) original).getIdentifier())
                .build();
    }
}