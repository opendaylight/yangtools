/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UserMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.builder.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.valid.DataValidationException;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

public class ImmutableUserMapNodeSchemaAwareBuilder extends ImmutableUserMapNodeBuilder {
    private final ListSchemaNode schema;

    protected ImmutableUserMapNodeSchemaAwareBuilder(final ListSchemaNode schema) {
        this.schema = requireNonNull(schema);
        super.withNodeIdentifier(NodeIdentifier.create(schema.getQName()));
    }

    protected ImmutableUserMapNodeSchemaAwareBuilder(final ListSchemaNode schema,
            final ImmutableUserMapNode node) {
        super(node);
        this.schema = requireNonNull(schema);
        super.withNodeIdentifier(NodeIdentifier.create(schema.getQName()));
    }

    public static @NonNull CollectionNodeBuilder<MapEntryNode, UserMapNode> create(final ListSchemaNode schema) {
        return new ImmutableUserMapNodeSchemaAwareBuilder(schema);
    }

    public static @NonNull CollectionNodeBuilder<MapEntryNode, UserMapNode> create(final ListSchemaNode schema,
            final UserMapNode node) {
        if (!(node instanceof ImmutableUserMapNode)) {
            throw new UnsupportedOperationException(String.format("Cannot initialize from class %s", node.getClass()));
        }

        return new ImmutableUserMapNodeSchemaAwareBuilder(schema, (ImmutableUserMapNode) node);
    }

    @Override
    public CollectionNodeBuilder<MapEntryNode, UserMapNode> withChild(final MapEntryNode child) {
        DataValidationException.checkLegalChild(schema.getQName().equals(child.getNodeType()), child.getIdentifier(),
            schema, Collections.singleton(schema.getQName()));
        return super.withChild(child);
    }

    @Override
    public CollectionNodeBuilder<MapEntryNode, UserMapNode> withNodeIdentifier(
            final NodeIdentifier withNodeIdentifier) {
        throw new UnsupportedOperationException("Node identifier created from schema");
    }
}
