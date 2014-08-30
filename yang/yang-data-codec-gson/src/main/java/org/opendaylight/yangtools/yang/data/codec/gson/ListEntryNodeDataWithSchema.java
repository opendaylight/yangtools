/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter.UNKNOWN_SIZE;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
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
        DataSchemaNode childSchema = newChild.getSchema();
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
    public void write(final NormalizedNodeStreamWriter writer) throws IOException {
        int keyCount = ((ListSchemaNode) getSchema()).getKeyDefinition().size();
        if (keyCount == 0) {
            writer.startUnkeyedListItem(provideNodeIdentifier(), UNKNOWN_SIZE);
            super.write(writer);
            writer.endNode();
        } else if (keyCount == qNameToKeys.size()) {
            writer.startMapEntryNode(provideNodeIdentifierWithPredicates(), UNKNOWN_SIZE);
            super.write(writer);
            writer.endNode();
        } else {
            throw new IllegalStateException("Some of keys of " + getSchema().getQName() + " are missing in input.");
        }
    }

    private NodeIdentifierWithPredicates provideNodeIdentifierWithPredicates() {
        Map<QName, Object> qNameToPredicateValues = new HashMap<>();

        for (SimpleNodeDataWithSchema simpleNodeDataWithSchema : qNameToKeys.values()) {
            qNameToPredicateValues.put(simpleNodeDataWithSchema.getSchema().getQName(), simpleNodeDataWithSchema.getValue());
        }

        return new NodeIdentifierWithPredicates(getSchema().getQName(), qNameToPredicateValues);
    }

}
