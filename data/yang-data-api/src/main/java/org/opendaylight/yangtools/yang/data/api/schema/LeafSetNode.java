/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.builder.ListNodeBuilder;

/**
 * Node representing set of simple leaf nodes. Node containing instances of {@link LeafSetEntryNode}.
 *
 * <p>
 * Schema and semantics of this node are described by instance of
 * {@link org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode}.
 *
 * @param <T> Type of leaf node values.
 */
public sealed interface LeafSetNode<T>
        extends DistinctNodeContainer<NodeWithValue<?>, LeafSetEntryNode<T>>, DataContainerChild, MixinNode
        permits SystemLeafSetNode, UserLeafSetNode {
    @Override
    @SuppressWarnings("rawtypes")
    Class<? extends LeafSetNode> contract();

    /**
     * A builder of {@link LeafSetNode}s.
     */
    sealed interface Builder<T, N extends LeafSetNode<T>> extends ListNodeBuilder<T, N>
        permits SystemLeafSetNode.Builder, UserLeafSetNode.Builder {
        // Just a specialization
    }
}
