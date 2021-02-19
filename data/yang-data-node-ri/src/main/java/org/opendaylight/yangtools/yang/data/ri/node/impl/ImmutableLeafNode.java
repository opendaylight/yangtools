/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.ri.node.impl;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;

public class ImmutableLeafNode<T>
        extends AbstractImmutableNormalizedSimpleValueNode<NodeIdentifier, LeafNode<?>, T> implements LeafNode<T> {
    ImmutableLeafNode(final NodeIdentifier nodeIdentifier, final T value) {
        super(nodeIdentifier, value);
    }

    @Beta
    @SuppressWarnings("unchecked")
    public static <T> @NonNull LeafNode<T> of(final NodeIdentifier identifier, final T value) {
        if (value instanceof byte[]) {
            return (LeafNode<T>) new ImmutableBinaryLeafNode(identifier, (byte[]) value);
        }
        return new ImmutableLeafNode<>(identifier, value);
    }

    @Override
    protected final Class<LeafNode<?>> implementedType() {
        return (Class) LeafNode.class;
    }
}