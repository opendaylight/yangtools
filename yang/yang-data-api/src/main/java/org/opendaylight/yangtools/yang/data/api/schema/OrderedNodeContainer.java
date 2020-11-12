/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import org.opendaylight.yangtools.concepts.ItemOrder.Ordered;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;

/**
 * A {@link NormalizedNodeContainer} which preserves user supplied ordering and allows addressing of child elements by
 * position.
 *
 * @param <V> child type
 */
public interface OrderedNodeContainer<V extends NormalizedNode>
        // FIXME: 7.0.0: do we really mean 'List' for body?
        extends NormalizedNodeContainer<NodeIdentifier, V>, Ordered, MixinNode {
    /**
     * Returns child node by position.
     *
     * @param position Position of child node
     * @return Child Node
     * @throws IndexOutOfBoundsException Out of bound Exception
     */
    V getChild(int position);
}
