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

import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.valid.DataValidationException;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;

final class SchemaAwareImmutableOrderedLeafSetNodeBuilder<T> extends ImmutableUserLeafSetNodeBuilder<T> {
    private final LeafListSchemaNode schema;

    SchemaAwareImmutableOrderedLeafSetNodeBuilder(final LeafListSchemaNode schema) {
        this.schema = requireNonNull(schema);
        super.withNodeIdentifier(new NodeIdentifier(schema.getQName()));
    }

    SchemaAwareImmutableOrderedLeafSetNodeBuilder(final LeafListSchemaNode schema,
            final ImmutableUserLeafSetNode<T> node) {
        super(node);
        this.schema = requireNonNull(schema);
        // FIXME: Preconditions.checkArgument(schema.getQName().equals(node.getIdentifier()));
        super.withNodeIdentifier(new NodeIdentifier(schema.getQName()));
    }

    @Override
    public ImmutableUserLeafSetNodeBuilder<T> withChildValue(final T childValue) {
        // TODO check value type
        return super.withChildValue(childValue);
    }

    @Override
    public ImmutableUserLeafSetNodeBuilder<T> withChild(final LeafSetEntryNode<T> child) {
        final NodeWithValue<T> childId = child.getIdentifier();
        final QName childName = childId.getNodeType();
        final QName qname = schema.getQName();

        checkArgument(qname.equals(childName), "Incompatible node type, should be: %s, is: %s", qname, childName);
        // TODO check value type using TypeProvider ?
        DataValidationException.checkLegalChild(qname.equals(childName), childId, schema, Set.of(qname));
        return super.withChild(child);
    }

    @Override
    public ImmutableUserLeafSetNodeBuilder<T> withNodeIdentifier(final NodeIdentifier withNodeIdentifier) {
        throw new UnsupportedOperationException("Node identifier created from schema");
    }
}
