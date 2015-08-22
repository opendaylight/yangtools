/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.ListNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.valid.DataValidationException;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;

public final class ImmutableOrderedLeafSetNodeSchemaAwareBuilder<T> extends ImmutableOrderedLeafSetNodeBuilder<T> {

    private final LeafListSchemaNode schema;

    private ImmutableOrderedLeafSetNodeSchemaAwareBuilder(final LeafListSchemaNode schema) {
        this.schema = Preconditions.checkNotNull(schema);
        super.withNodeIdentifier(new NodeIdentifier(schema.getQName()));
    }

    public ImmutableOrderedLeafSetNodeSchemaAwareBuilder(final LeafListSchemaNode schema, final ImmutableOrderedLeafSetNode<T> node) {
        super(node);
        this.schema = Preconditions.checkNotNull(schema);
        // FIXME: BUG-4158:: Preconditions.checkArgument(schema.getQName().equals(node.getIdentifier()));
        super.withNodeIdentifier(new NodeIdentifier(schema.getQName()));
    }

    public static <T> ListNodeBuilder<T, LeafSetEntryNode<T>> create(final LeafListSchemaNode schema) {
        return new ImmutableOrderedLeafSetNodeSchemaAwareBuilder<>(schema);
    }

    public static <T> ListNodeBuilder<T, LeafSetEntryNode<T>> create(final LeafListSchemaNode schema, final LeafSetNode<T> node) {
        if (!(node instanceof ImmutableOrderedLeafSetNode<?>)) {
            throw new UnsupportedOperationException(String.format("Cannot initialize from class %s", node.getClass()));
        }

        return new ImmutableOrderedLeafSetNodeSchemaAwareBuilder<T>(schema, (ImmutableOrderedLeafSetNode<T>) node);
    }

    @Override
    public ListNodeBuilder<T, LeafSetEntryNode<T>> withChildValue(final T value) {
        // FIXME: BUG-4158: check value type
        return super.withChildValue(value);
    }

    @Override
    public ListNodeBuilder<T, LeafSetEntryNode<T>> withChild(final LeafSetEntryNode<T> child) {
        Preconditions.checkArgument(schema.getQName().equals(child.getNodeType()),
                "Incompatible node type, should be: %s, is: %s", schema.getQName(), child.getNodeType());
        // FIXME: BUG-4158: check value type using TypeProvider ?
        DataValidationException.checkLegalChild(schema.getQName().equals(child.getNodeType()), child.getIdentifier(), schema, Sets.newHashSet(schema.getQName()));
        return super.withChild(child);
    }

    @Override
    public ListNodeBuilder<T, LeafSetEntryNode<T>> withNodeIdentifier(final NodeIdentifier nodeIdentifier) {
        throw new UnsupportedOperationException("Node identifier created from schema");
    }
}
