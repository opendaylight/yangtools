/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;

/**
 * Leaf node with multiplicity 0...n. Leaf node has a value, but no child nodes in the data tree, schema for leaf node
 * and its value is described by {@link org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode}.
 *
 * @param <T> Value type
 */
public non-sealed interface LeafSetEntryNode<T> extends ValueNode<T> {
    @Override
    @SuppressWarnings("rawtypes")
    default Class<LeafSetEntryNode> contract() {
        return LeafSetEntryNode.class;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * <b>Implementation note</b>
     * Invocation of {@link NodeWithValue#getValue()} on returned instance of {@link NodeWithValue} must return the
     * same value as invocation of {@code #body()}, such as following condition is always met:
     * {@code true == this.getIdentifier().getValue().equals(this.body())}.
     */
    @Override
    NodeWithValue<T> name();

    /**
     * A builder of {@link LeafSetEntryNode}s.
     */
    interface Builder<T> extends NormalizedNode.Builder {

        @NonNull Builder<T> withName(NodeWithValue<T> name);

        @NonNull Builder<T> withBody(T value);

        @Override
        LeafSetEntryNode<T> build();
    }
}
