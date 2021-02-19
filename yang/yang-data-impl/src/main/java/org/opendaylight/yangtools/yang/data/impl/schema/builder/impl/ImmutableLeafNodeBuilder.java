/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeBuilder;
import org.opendaylight.yangtools.yang.data.ri.node.impl.ImmutableBinaryLeafNode;
import org.opendaylight.yangtools.yang.data.ri.node.impl.ImmutableLeafNode;

public class ImmutableLeafNodeBuilder<T>
        extends AbstractImmutableNormalizedNodeBuilder<NodeIdentifier, T, LeafNode<T>> {

    public static <T> @NonNull NormalizedNodeBuilder<NodeIdentifier, T, LeafNode<T>> create() {
        return new ImmutableLeafNodeBuilder<>();
    }

    @Beta
    @SuppressWarnings("unchecked")
    public static <T> @NonNull LeafNode<T> createNode(final NodeIdentifier identifier, final T value) {
        if (value instanceof byte[]) {
            return (LeafNode<T>) new ImmutableBinaryLeafNode(identifier, (byte[]) value);
        }
        return new ImmutableLeafNode<>(identifier, value);
    }

    @Override
    public LeafNode<T> build() {
        return createNode(getNodeIdentifier(), getValue());
    }
}
