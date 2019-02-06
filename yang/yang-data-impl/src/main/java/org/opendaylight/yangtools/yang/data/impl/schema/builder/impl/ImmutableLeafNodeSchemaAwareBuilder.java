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
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeAttrBuilder;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;

public final class ImmutableLeafNodeSchemaAwareBuilder<T> extends ImmutableLeafNodeBuilder<T> {
    private ImmutableLeafNodeSchemaAwareBuilder(final LeafSchemaNode schema) {
        super.withNodeIdentifier(NodeIdentifier.create(schema.getQName()));
    }

    public static <T> @NonNull NormalizedNodeAttrBuilder<NodeIdentifier, T, LeafNode<T>> create(
            final LeafSchemaNode schema) {
        return new ImmutableLeafNodeSchemaAwareBuilder<>(schema);
    }

    @Override
    public NormalizedNodeAttrBuilder<NodeIdentifier, T, LeafNode<T>> withValue(final T withValue) {
        // TODO: check value type
        return super.withValue(withValue);
    }

    @Override
    public NormalizedNodeAttrBuilder<NodeIdentifier, T, LeafNode<T>> withNodeIdentifier(
            final NodeIdentifier withNodeIdentifier) {
        throw new UnsupportedOperationException("Node identifier created from schema");
    }
}
