/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;

/**
 *
 * Normalized Node container which preserves user supplied ordering
 * and allows addressing of child elements by positiion.
 *
 * @param <V>
 */
public interface OrderedNodeContainer<V extends NormalizedNode<?, ?>> extends MixinNode, NormalizedNode<NodeIdentifier, Iterable<V>> {

    /**
     * Returns child node by position
     *
     * @param position Position of child node
     * @return Child Node
     * @throws IndexOutOfBoundsException
     */
    V getChild(int position);

    /**
     * Returns count of child nodes
     *
     * @return count of child nodes.
     */
    int getSize();
}
