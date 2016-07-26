/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

/**
 * Interface holding the common trait of {@link LeafSetEntryNode} and {@link LeafNode}, which both hold a value.
 *
 * @author Robert Varga
 *
 * @param <K> Local identifier of node
 * @param <V> Value of node
 */
public interface ValueNode<K extends PathArgument, V> extends NormalizedNode<K, V> {
    /**
     * Returns value of held by this node.
     *
     * <h3>Implementation notes</h3> Invocation of {@link #getValue()} must
     * provides same value as value in {@link #getIdentifier()}.
     * <code>true == this.getIdentifier().getValue().equals(this.getValue())</code>
     *
     * @return Returned value of this node. Value SHOULD meet criteria
     *         defined by schema.
     *
     */
    @Override
    V getValue();
}
