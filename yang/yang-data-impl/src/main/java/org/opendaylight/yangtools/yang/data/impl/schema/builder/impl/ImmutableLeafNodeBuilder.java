/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import java.util.Map;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeAttrBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableNormalizedValueAttrNode;

public class ImmutableLeafNodeBuilder<T> extends AbstractImmutableNormalizedNodeBuilder<NodeIdentifier, T, LeafNode<T>> {

    public static <T> NormalizedNodeAttrBuilder<NodeIdentifier, T, LeafNode<T>> create() {
        return new ImmutableLeafNodeBuilder<>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public LeafNode<T> build() {
        final T value = getValue();
        if (value instanceof byte[]) {
            return (LeafNode<T>) new ImmutableBinaryLeafNode(getNodeIdentifier(), (byte[]) value, getAttributes());
        }

        return new ImmutableLeafNode<>(getNodeIdentifier(), value, getAttributes());
    }

    private static final class ImmutableLeafNode<T>
            extends AbstractImmutableNormalizedValueAttrNode<NodeIdentifier, T> implements LeafNode<T> {
        ImmutableLeafNode(final NodeIdentifier nodeIdentifier, final T value, final Map<QName, String> attributes) {
            super(nodeIdentifier, value, attributes);
        }
    }

    private static final class ImmutableBinaryLeafNode
            extends AbstractImmutableNormalizedValueAttrNode<NodeIdentifier, byte[]> implements LeafNode<byte[]> {
        ImmutableBinaryLeafNode(final NodeIdentifier nodeIdentifier, final byte[] value,
            final Map<QName, String> attributes) {
            super(nodeIdentifier, value, attributes);
        }

        @Override
        protected byte[] wrapValue(final byte[] value) {
            return value.clone();
        }
    }
}
