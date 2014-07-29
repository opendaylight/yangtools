/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeAttrBuilder;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;

public final class ImmutableLeafNodeSchemaAwareBuilder<T> extends ImmutableLeafNodeBuilder<T> {

    private ImmutableLeafNodeSchemaAwareBuilder(LeafSchemaNode schema) {
        super.withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(schema.getQName()));
    }

    public static <T> NormalizedNodeAttrBuilder<YangInstanceIdentifier.NodeIdentifier, T, LeafNode<T>> create(LeafSchemaNode schema) {
        return new ImmutableLeafNodeSchemaAwareBuilder<>(schema);
    }

    @Override
    public NormalizedNodeAttrBuilder<YangInstanceIdentifier.NodeIdentifier, T, LeafNode<T>> withValue(T value) {
//        TODO check value type
        return super.withValue(value);
    }

    @Override
    public NormalizedNodeAttrBuilder<YangInstanceIdentifier.NodeIdentifier, T, LeafNode<T>> withNodeIdentifier(YangInstanceIdentifier.NodeIdentifier nodeIdentifier) {
        throw new UnsupportedOperationException("Node identifier created from schema");
    }
}
