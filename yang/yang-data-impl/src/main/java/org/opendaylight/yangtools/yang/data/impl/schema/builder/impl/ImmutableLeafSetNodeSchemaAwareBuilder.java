/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.Sets;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.ListNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.valid.DataValidationException;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;

public final class ImmutableLeafSetNodeSchemaAwareBuilder<T> extends ImmutableLeafSetNodeBuilder<T> {

    private final LeafListSchemaNode schema;

    private ImmutableLeafSetNodeSchemaAwareBuilder(final LeafListSchemaNode schema) {
        this.schema = requireNonNull(schema);
        super.withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(schema.getQName()));
    }

    public ImmutableLeafSetNodeSchemaAwareBuilder(final LeafListSchemaNode schema, final ImmutableLeafSetNode<T> node) {
        super(node);
        this.schema = requireNonNull(schema);
        // FIXME: Preconditions.checkArgument(schema.getQName().equals(node.getIdentifier()));
        super.withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(schema.getQName()));
    }

    public static <T> ListNodeBuilder<T, LeafSetEntryNode<T>> create(final LeafListSchemaNode schema) {
        return new ImmutableLeafSetNodeSchemaAwareBuilder<>(schema);
    }

    public static <T> ListNodeBuilder<T, LeafSetEntryNode<T>> create(final LeafListSchemaNode schema, final LeafSetNode<T> node) {
        if (!(node instanceof ImmutableLeafSetNode<?>)) {
            throw new UnsupportedOperationException(String.format("Cannot initialize from class %s", node.getClass()));
        }

        return new ImmutableLeafSetNodeSchemaAwareBuilder<>(schema, (ImmutableLeafSetNode<T>) node);
    }

    @Override
    public ListNodeBuilder<T, LeafSetEntryNode<T>> withChildValue(final T value) {
        // TODO check value type
        return super.withChildValue(value);
    }

    @Override
    public ListNodeBuilder<T, LeafSetEntryNode<T>> withChild(final LeafSetEntryNode<T> child) {
        checkArgument(schema.getQName().equals(child.getNodeType()),
                "Incompatible node type, should be: %s, is: %s", schema.getQName(), child.getNodeType());
        // TODO check value type using TypeProvider ?
        DataValidationException.checkLegalChild(schema.getQName().equals(child.getNodeType()), child.getIdentifier(),
            schema, Sets.newHashSet(schema.getQName()));
        return super.withChild(child);
    }

    @Override
    public ListNodeBuilder<T, LeafSetEntryNode<T>> withNodeIdentifier(final YangInstanceIdentifier.NodeIdentifier nodeIdentifier) {
        throw new UnsupportedOperationException("Node identifier created from schema");
    }
}
