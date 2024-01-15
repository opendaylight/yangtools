/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

/**
 * Interface holding the common trait of {@link LeafSetEntryNode} and {@link LeafNode}, which both hold a value.
 *
 * @param <V> Value of node, which needs to be a well-published simple value type.
 */
public sealed interface ValueNode<V> extends NormalizedNode permits LeafNode, LeafSetEntryNode, AbstractValueNode {
    /**
     * {@inheritDoc}
     *
     * <p>
     * <b>Implementation note</b>
     * Invocation of {@code body()} must provide the same value as value in {@link #name()}.
     * {@code true == this.name().getValue().equals(this.body())}.
     */
    @Override
    V body();
}
