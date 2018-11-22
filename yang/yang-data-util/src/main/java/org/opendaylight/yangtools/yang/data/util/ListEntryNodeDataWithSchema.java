/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Verify.verify;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.opendaylight.yangtools.util.ImmutableMapTemplate;
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
    // This template results in Maps in schema definition order
    private final ImmutableMapTemplate<QName> predicateTemplate;
    private final Map<QName, SimpleNodeDataWithSchema> keyValues;

    // FIXME: 3.0.0: require ListSchemaNode
    // FIXME: 3.0.0: hide this constructor and provide specialized keyed/unkeyed classes
    public ListEntryNodeDataWithSchema(final DataSchemaNode schema) {
        super(schema);

        final Collection<QName> keyDef = ((ListSchemaNode) getSchema()).getKeyDefinition();
        if (keyDef.isEmpty()) {
            predicateTemplate = null;
            keyValues = null;
        } else {
            predicateTemplate = ImmutableMapTemplate.ordered(keyDef);
            keyValues = new HashMap<>();
        }
    }

    @Override
    public void addChild(final AbstractNodeDataWithSchema newChild) {
        if (predicateTemplate != null) {
            final DataSchemaNode childSchema = newChild.getSchema();
            if (childSchema instanceof LeafSchemaNode) {
                populateKeyValue(childSchema.getQName(), newChild);
            }
        }
        super.addChild(newChild);
    }

    private void populateKeyValue(final QName childName, final AbstractNodeDataWithSchema child) {
        if (predicateTemplate.keySet().contains(childName)) {
            verify(child instanceof SimpleNodeDataWithSchema);
            keyValues.put(childName, (SimpleNodeDataWithSchema)child);
        }
    }

    @Override
    public void write(final NormalizedNodeStreamWriter writer) throws IOException {
        writer.nextDataSchemaNode(getSchema());
        if (predicateTemplate != null) {
            writeKeyedListItem(writer);
        } else {
            writer.startUnkeyedListItem(provideNodeIdentifier(), childSizeHint());
        }

        super.write(writer);
        writer.endNode();
    }

    private void writeKeyedListItem(final NormalizedNodeStreamWriter writer) throws IOException {
        // FIXME: 3.0.0: remove this check? predicateTemplate will throw an IllegalArgumentException if anything
        //               goes wrong -- which is a change of behavior, as now we're throwing an ISE. Do we want that?
        final Collection<QName> keySet = predicateTemplate.keySet();
        checkState(keySet.size() == keyValues.size(),
                "Map entry corresponding to %s is missing some of required keys %s", getSchema().getQName(), keySet);

        final NodeIdentifierWithPredicates identifier = new NodeIdentifierWithPredicates(getSchema().getQName(),
            predicateTemplate.instantiateTransformed(keyValues, (key, node) -> node.getValue()));

        if (writer instanceof NormalizedNodeStreamAttributeWriter && getAttributes() != null) {
            ((NormalizedNodeStreamAttributeWriter) writer).startMapEntryNode(identifier, childSizeHint(),
                getAttributes());
        } else {
            writer.startMapEntryNode(identifier, childSizeHint());
        }
    }
}
