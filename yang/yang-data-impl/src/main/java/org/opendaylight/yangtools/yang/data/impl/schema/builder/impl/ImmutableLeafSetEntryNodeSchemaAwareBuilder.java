/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import static java.util.Objects.requireNonNull;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeAttrBuilder;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;

public final class ImmutableLeafSetEntryNodeSchemaAwareBuilder<T> extends ImmutableLeafSetEntryNodeBuilder<T> {

    private final LeafListSchemaNode schema;

    private ImmutableLeafSetEntryNodeSchemaAwareBuilder(final LeafListSchemaNode schema) {
        this.schema = requireNonNull(schema);
    }

    public static <T> NormalizedNodeAttrBuilder<NodeWithValue, T, LeafSetEntryNode<T>> create(final LeafListSchemaNode schema) {
        return new ImmutableLeafSetEntryNodeSchemaAwareBuilder<>(schema);
    }

    @Override
    public NormalizedNodeAttrBuilder<NodeWithValue, T, LeafSetEntryNode<T>> withValue(final T value) {
        super.withNodeIdentifier(new NodeWithValue<>(schema.getQName(), value));
        // TODO check value type using TypeProvider ?
        return super.withValue(value);
    }

    @Override
    public NormalizedNodeAttrBuilder<NodeWithValue, T, LeafSetEntryNode<T>> withNodeIdentifier(final NodeWithValue nodeIdentifier) {
        throw new UnsupportedOperationException("Node identifier created from schema");
    }

}
