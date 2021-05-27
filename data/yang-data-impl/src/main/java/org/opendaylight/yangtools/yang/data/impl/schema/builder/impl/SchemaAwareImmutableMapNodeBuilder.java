/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import static java.util.Objects.requireNonNull;

import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.valid.DataValidationException;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

final class SchemaAwareImmutableMapNodeBuilder extends ImmutableMapNodeBuilder {
    private final ListSchemaNode schema;

    SchemaAwareImmutableMapNodeBuilder(final ListSchemaNode schema) {
        this.schema = requireNonNull(schema);
        super.withNodeIdentifier(NodeIdentifier.create(schema.getQName()));
    }

    SchemaAwareImmutableMapNodeBuilder(final ListSchemaNode schema, final ImmutableMapNode node) {
        super(node);
        this.schema = requireNonNull(schema);
        super.withNodeIdentifier(NodeIdentifier.create(schema.getQName()));
    }

    @Override
    public ImmutableMapNodeBuilder withChild(final MapEntryNode child) {
        final NodeIdentifierWithPredicates childId = child.getIdentifier();
        final QName qname = schema.getQName();

        DataValidationException.checkLegalChild(qname.equals(childId.getNodeType()), childId, schema, Set.of(qname));
        return super.withChild(child);
    }

    @Override
    public ImmutableMapNodeBuilder withNodeIdentifier(final NodeIdentifier withNodeIdentifier) {
        throw new UnsupportedOperationException("Node identifier created from schema");
    }
}
