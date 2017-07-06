/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import com.google.common.base.Preconditions;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamAttributeWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

/**
 * Utility class used for tracking parser state as needed by a StAX-like parser.
 * This class is to be used only by respective XML and JSON parsers in yang-data-codec-xml and yang-data-codec-gson.
 *
 * <p>
 * Represents a YANG list entry node.
 */
public class ListEntryNodeDataWithSchema extends CompositeNodeDataWithSchema {

    private final Map<QName, SimpleNodeDataWithSchema> qnameToKeys = new HashMap<>();

    public ListEntryNodeDataWithSchema(final DataSchemaNode schema) {
        super(schema);
    }

    @Override
    public void addChild(final AbstractNodeDataWithSchema newChild) {
        final DataSchemaNode childSchema = newChild.getSchema();
        if (childSchema instanceof LeafSchemaNode && isPartOfKey((LeafSchemaNode) childSchema)) {
            qnameToKeys.put(childSchema.getQName(), (SimpleNodeDataWithSchema)newChild);
        }
        super.addChild(newChild);
    }

    private boolean isPartOfKey(final LeafSchemaNode potentialKey) {
        for (QName qname : ((ListSchemaNode) getSchema()).getKeyDefinition()) {
            if (qname.equals(potentialKey.getQName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void write(final NormalizedNodeStreamWriter writer) throws IOException {
        final Collection<QName> keyDef = ((ListSchemaNode) getSchema()).getKeyDefinition();
        if (keyDef.isEmpty()) {
            writer.nextDataSchemaNode(getSchema());
            writer.startUnkeyedListItem(provideNodeIdentifier(), childSizeHint());
            super.write(writer);
            writer.endNode();
            return;
        }

        Preconditions.checkState(keyDef.size() == qnameToKeys.size(), "Input is missing some of the keys of %s",
                getSchema().getQName());

        // Need to restore schema order...
        final Map<QName, Object> predicates = new LinkedHashMap<>();
        for (QName qname : keyDef) {
            predicates.put(qname, qnameToKeys.get(qname).getValue());
        }

        writer.nextDataSchemaNode(getSchema());

        if (writer instanceof NormalizedNodeStreamAttributeWriter && getAttributes() != null) {
            ((NormalizedNodeStreamAttributeWriter) writer).startMapEntryNode(
                    new NodeIdentifierWithPredicates(getSchema().getQName(), predicates), childSizeHint(),
                    getAttributes());
        } else {
            writer.startMapEntryNode(new NodeIdentifierWithPredicates(getSchema().getQName(), predicates),
                    childSizeHint());
        }

        super.write(writer);
        writer.endNode();
    }
}
