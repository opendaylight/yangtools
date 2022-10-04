/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.nodes;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;

public sealed class ImmutableLeafNode<T>
        extends AbstractImmutableNormalizedSimpleValueNode<NodeIdentifier, LeafNode<?>, T> implements LeafNode<T>
        permits ImmutableBinaryLeafNode {
    ImmutableLeafNode(final NodeIdentifier nodeIdentifier, final T value) {
        super(nodeIdentifier, value);
    }

    @SuppressWarnings("unchecked")
    public static <T> @NonNull ImmutableLeafNode<T> of(final NodeIdentifier nodeIdentifier, final T value) {
        return value instanceof byte[]
            ? (ImmutableLeafNode<T>) new ImmutableBinaryLeafNode(nodeIdentifier, (byte[]) value)
                : new ImmutableLeafNode<>(nodeIdentifier, value);
    }

    @Override
    protected final Class<LeafNode<?>> implementedType() {
        return (Class) LeafNode.class;
    }
}