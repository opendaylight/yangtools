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

import java.util.Collections;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UserLeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.builder.ListNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.valid.DataValidationException;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;

public final class ImmutableUserLeafSetNodeSchemaAwareBuilder<T> extends ImmutableUserLeafSetNodeBuilder<T> {
    private final LeafListSchemaNode schema;

    private ImmutableUserLeafSetNodeSchemaAwareBuilder(final LeafListSchemaNode schema) {
        this.schema = requireNonNull(schema);
        super.withNodeIdentifier(new NodeIdentifier(schema.getQName()));
    }

    public ImmutableUserLeafSetNodeSchemaAwareBuilder(final LeafListSchemaNode schema,
            final ImmutableUserLeafSetNode<T> node) {
        super(node);
        this.schema = requireNonNull(schema);
        // FIXME: Preconditions.checkArgument(schema.getQName().equals(node.getIdentifier()));
        super.withNodeIdentifier(new NodeIdentifier(schema.getQName()));
    }

    public static <T> @NonNull ListNodeBuilder<T, UserLeafSetNode<T>> create(final LeafListSchemaNode schema) {
        return new ImmutableUserLeafSetNodeSchemaAwareBuilder<>(schema);
    }

    public static <T> @NonNull ListNodeBuilder<T, UserLeafSetNode<T>> create(final LeafListSchemaNode schema,
            final UserLeafSetNode<T> node) {
        if (!(node instanceof ImmutableUserLeafSetNode<?>)) {
            throw new UnsupportedOperationException(String.format("Cannot initialize from class %s", node.getClass()));
        }

        return new ImmutableUserLeafSetNodeSchemaAwareBuilder<>(schema, (ImmutableUserLeafSetNode<T>) node);
    }

    @Override
    public ImmutableUserLeafSetNodeBuilder<T> withChildValue(final T childValue) {
        // TODO check value type
        return super.withChildValue(childValue);
    }

    @Override
    public ImmutableUserLeafSetNodeBuilder<T> withChild(final LeafSetEntryNode<T> child) {
        checkArgument(schema.getQName().equals(child.getNodeType()), "Incompatible node type, should be: %s, is: %s",
            schema.getQName(), child.getNodeType());
        // TODO check value type using TypeProvider ?
        DataValidationException.checkLegalChild(schema.getQName().equals(child.getNodeType()), child.getIdentifier(),
            schema, Collections.singleton(schema.getQName()));
        return super.withChild(child);
    }

    @Override
    public ImmutableUserLeafSetNodeBuilder<T> withNodeIdentifier(final NodeIdentifier withNodeIdentifier) {
        throw new UnsupportedOperationException("Node identifier created from schema");
    }
}
