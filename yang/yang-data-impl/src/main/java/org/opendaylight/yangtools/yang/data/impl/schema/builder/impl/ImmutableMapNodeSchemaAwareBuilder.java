/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import com.google.common.collect.Sets;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.valid.DataValidationException;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

import com.google.common.base.Preconditions;

public class ImmutableMapNodeSchemaAwareBuilder extends ImmutableMapNodeBuilder {

    private final ListSchemaNode schema;

    protected ImmutableMapNodeSchemaAwareBuilder(final ListSchemaNode schema) {
        this.schema = Preconditions.checkNotNull(schema);
        super.withNodeIdentifier(new InstanceIdentifier.NodeIdentifier(schema.getQName()));
    }

    protected ImmutableMapNodeSchemaAwareBuilder(final ListSchemaNode schema, final ImmutableMapNode node) {
        super(node);
        this.schema = Preconditions.checkNotNull(schema);
        super.withNodeIdentifier(new InstanceIdentifier.NodeIdentifier(schema.getQName()));
    }

    public static CollectionNodeBuilder<MapEntryNode, MapNode> create(final ListSchemaNode schema) {
        return new ImmutableMapNodeSchemaAwareBuilder(schema);
    }

    public static CollectionNodeBuilder<MapEntryNode, MapNode> create(final ListSchemaNode schema, final MapNode node) {
        if (!(node instanceof ImmutableMapNode)) {
            throw new UnsupportedOperationException(String.format("Cannot initialize from class %s", node.getClass()));
        }

        return new ImmutableMapNodeSchemaAwareBuilder(schema, (ImmutableMapNode) node);
    }

    @Override
    public CollectionNodeBuilder<MapEntryNode, MapNode> withChild(final MapEntryNode child) {
        DataValidationException.checkLegalChild(schema.getQName().equals(child.getNodeType()), child.getIdentifier(), schema, Sets.newHashSet(schema.getQName()));
        return super.withChild(child);
    }

    @Override
    public CollectionNodeBuilder<MapEntryNode, MapNode> withNodeIdentifier(final InstanceIdentifier.NodeIdentifier nodeIdentifier) {
        throw new UnsupportedOperationException("Node identifier created from schema");
    }
}
