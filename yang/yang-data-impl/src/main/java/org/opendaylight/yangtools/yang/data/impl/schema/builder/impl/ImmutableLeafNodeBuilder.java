/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableNormalizedSimpleValueNode;

public class ImmutableLeafNodeBuilder<T>
        extends AbstractImmutableNormalizedNodeBuilder<NodeIdentifier, T, LeafNode<T>> {

    public static <T> @NonNull NormalizedNodeBuilder<NodeIdentifier, T, LeafNode<T>> create() {
        return new ImmutableLeafNodeBuilder<>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public LeafNode<T> build() {
        final T value = getValue();
        if (value instanceof byte[]) {
            return (LeafNode<T>) new ImmutableBinaryLeafNode(getNodeIdentifier(), (byte[]) value);
        }

        return new ImmutableLeafNode<>(getNodeIdentifier(), value);
    }

    private static final class ImmutableLeafNode<T>
            extends AbstractImmutableNormalizedSimpleValueNode<NodeIdentifier, T> implements LeafNode<T> {
        ImmutableLeafNode(final NodeIdentifier nodeIdentifier, final T value) {
            super(nodeIdentifier, value);
        }
    }

    private static final class ImmutableBinaryLeafNode
            extends AbstractImmutableNormalizedSimpleValueNode<NodeIdentifier, byte[]> implements LeafNode<byte[]> {
        ImmutableBinaryLeafNode(final NodeIdentifier nodeIdentifier, final byte[] value) {
            super(nodeIdentifier, value);
        }

        @Override
        protected byte[] wrapValue(final byte[] valueToWrap) {
            return valueToWrap.clone();
        }
    }
}
