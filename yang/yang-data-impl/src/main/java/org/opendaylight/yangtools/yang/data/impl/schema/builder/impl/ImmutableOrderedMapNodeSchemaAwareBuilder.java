/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import com.google.common.base.Preconditions;
import java.util.Collections;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.OrderedMapNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.valid.DataValidationException;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

public class ImmutableOrderedMapNodeSchemaAwareBuilder extends ImmutableOrderedMapNodeBuilder {

    private final ListSchemaNode schema;

    protected ImmutableOrderedMapNodeSchemaAwareBuilder(final ListSchemaNode schema) {
        this.schema = Preconditions.checkNotNull(schema);
        super.withNodeIdentifier(NodeIdentifier.create(schema.getQName()));
    }

    protected ImmutableOrderedMapNodeSchemaAwareBuilder(final ListSchemaNode schema,
            final ImmutableOrderedMapNode node) {
        super(node);
        this.schema = Preconditions.checkNotNull(schema);
        super.withNodeIdentifier(NodeIdentifier.create(schema.getQName()));
    }

    public static @NonNull CollectionNodeBuilder<MapEntryNode, OrderedMapNode> create(final ListSchemaNode schema) {
        return new ImmutableOrderedMapNodeSchemaAwareBuilder(schema);
    }

    public static @NonNull CollectionNodeBuilder<MapEntryNode, OrderedMapNode> create(final ListSchemaNode schema,
        final MapNode node) {
        if (!(node instanceof ImmutableOrderedMapNode)) {
            throw new UnsupportedOperationException(String.format("Cannot initialize from class %s", node.getClass()));
        }

        return new ImmutableOrderedMapNodeSchemaAwareBuilder(schema, (ImmutableOrderedMapNode) node);
    }

    @Override
    public CollectionNodeBuilder<MapEntryNode, OrderedMapNode> withChild(final MapEntryNode child) {
        DataValidationException.checkLegalChild(schema.getQName().equals(child.getNodeType()), child.getIdentifier(),
            schema, Collections.singleton(schema.getQName()));
        return super.withChild(child);
    }

    @Override
    public CollectionNodeBuilder<MapEntryNode, OrderedMapNode> withNodeIdentifier(
            final NodeIdentifier withNodeIdentifier) {
        throw new UnsupportedOperationException("Node identifier created from schema");
    }
}
