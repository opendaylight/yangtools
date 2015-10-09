/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import com.google.common.base.Preconditions;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.stream.SchemaAwareNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

class ListEntryNodeDataWithSchema extends CompositeNodeDataWithSchema {

    private final Map<QName, SimpleNodeDataWithSchema> qNameToKeys = new HashMap<>();

    public ListEntryNodeDataWithSchema(final DataSchemaNode schema) {
        super(schema);
    }

    @Override
    public void addChild(final AbstractNodeDataWithSchema newChild) {
        final DataSchemaNode childSchema = newChild.getSchema();
        if (childSchema instanceof LeafSchemaNode && isPartOfKey((LeafSchemaNode) childSchema)) {
            qNameToKeys.put(childSchema.getQName(), (SimpleNodeDataWithSchema)newChild);
        }
        super.addChild(newChild);
    }

    private boolean isPartOfKey(final LeafSchemaNode potentialKey) {
        List<QName> keys = ((ListSchemaNode) getSchema()).getKeyDefinition();
        for (QName qName : keys) {
            if (qName.equals(potentialKey.getQName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void write(final SchemaAwareNormalizedNodeStreamWriter writer) throws IOException {
        final Collection<QName> keyDef = ((ListSchemaNode) getSchema()).getKeyDefinition();
        if (keyDef.isEmpty()) {
            writer.nextDataSchemaNode(getSchema());
            writer.startUnkeyedListItem(provideNodeIdentifier(), childSizeHint());
            super.write(writer);
            writer.endNode();
            return;
        }

        Preconditions.checkState(keyDef.size() == qNameToKeys.size(), "Input is missing some of the keys of %s", getSchema().getQName());

        // Need to restore schema order...
        final Map<QName, Object> predicates = new LinkedHashMap<>();
        for (QName qname : keyDef) {
            predicates.put(qname, qNameToKeys.get(qname).getValue());
        }

        writer.nextDataSchemaNode(getSchema());
        writer.startMapEntryNode(
            new NodeIdentifierWithPredicates(getSchema().getQName(), predicates),
            childSizeHint());
        super.write(writer);
        writer.endNode();
    }
}
