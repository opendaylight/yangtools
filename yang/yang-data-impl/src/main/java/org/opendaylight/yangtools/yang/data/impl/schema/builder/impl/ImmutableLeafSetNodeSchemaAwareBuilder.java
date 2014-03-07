/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.ListNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;

import com.google.common.base.Preconditions;

public final class ImmutableLeafSetNodeSchemaAwareBuilder<T> extends ImmutableLeafSetNodeBuilder<T> {

    private final LeafListSchemaNode schema;

    private ImmutableLeafSetNodeSchemaAwareBuilder(LeafListSchemaNode schema) {
        super();
        this.schema = schema;
        super.withNodeIdentifier(new InstanceIdentifier.NodeIdentifier(schema.getQName()));
    }

    public static <T> ListNodeBuilder<T, LeafSetEntryNode<T>> create(LeafListSchemaNode schema) {
        return new ImmutableLeafSetNodeSchemaAwareBuilder<>(schema);
    }

    @Override
    public ListNodeBuilder<T, LeafSetEntryNode<T>> withChildValue(T value) {
        // TODO check value type
        return super.withChildValue(value);
    }

    @Override
    public ListNodeBuilder<T, LeafSetEntryNode<T>> withChild(LeafSetEntryNode<T> child) {
        Preconditions.checkArgument(schema.getQName().equals(child.getNodeType()),
                "Incompatible node type, should be: %s, is: %s", schema.getQName(), child.getNodeType());
        return super.withChild(child);
    }

    @Override
    public ListNodeBuilder<T, LeafSetEntryNode<T>> withNodeIdentifier(InstanceIdentifier.NodeIdentifier nodeIdentifier) {
        throw new UnsupportedOperationException("Node identifier created from schema");
    }
}
