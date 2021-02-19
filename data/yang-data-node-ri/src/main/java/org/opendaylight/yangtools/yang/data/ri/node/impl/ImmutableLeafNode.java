/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.ri.node.impl;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;

public class ImmutableLeafNode<T>
        extends AbstractImmutableNormalizedSimpleValueNode<NodeIdentifier, LeafNode<?>, T> implements LeafNode<T> {
    public ImmutableLeafNode(final NodeIdentifier nodeIdentifier, final T value) {
        super(nodeIdentifier, value);
    }

    @Override
    protected final Class<LeafNode<?>> implementedType() {
        return (Class) LeafNode.class;
    }
}