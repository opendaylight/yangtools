/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import static com.google.common.base.Verify.verify;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
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
public abstract class ListEntryNodeDataWithSchema extends CompositeNodeDataWithSchema {
    private static final class Keyed extends ListEntryNodeDataWithSchema {
        private final Map<QName, SimpleNodeDataWithSchema> keyValues = new HashMap<>();
        // This template results in Maps in schema definition order
        private final ImmutableMapTemplate<QName> predicateTemplate;

        Keyed(final ListSchemaNode schema, final List<QName> keyDef) {
            super(schema);
            predicateTemplate = ImmutableMapTemplate.ordered(keyDef);
        }

        @Override
        public void addChild(final AbstractNodeDataWithSchema newChild) {
            final DataSchemaNode childSchema = newChild.getSchema();
            if (childSchema instanceof LeafSchemaNode) {
                final QName childName = childSchema.getQName();
                if (predicateTemplate.keySet().contains(childName)) {
                    verify(newChild instanceof SimpleNodeDataWithSchema);
                    keyValues.put(childName, (SimpleNodeDataWithSchema)newChild);
                }
            }
            super.addChild(newChild);
        }

        @Override
        public void write(final NormalizedNodeStreamWriter writer) throws IOException {
            writer.nextDataSchemaNode(getSchema());
            final NodeIdentifierWithPredicates identifier = new NodeIdentifierWithPredicates(getSchema().getQName(),
                predicateTemplate.instantiateTransformed(keyValues, (key, node) -> node.getValue()));

            if (writer instanceof NormalizedNodeStreamAttributeWriter && getAttributes() != null) {
                ((NormalizedNodeStreamAttributeWriter) writer).startMapEntryNode(identifier, childSizeHint(),
                    getAttributes());
            } else {
                writer.startMapEntryNode(identifier, childSizeHint());
            }

            super.write(writer);
            writer.endNode();
        }
    }

    private static final class Unkeyed extends ListEntryNodeDataWithSchema {
        Unkeyed(final ListSchemaNode schema) {
            super(schema);
        }

        @Override
        public void write(final NormalizedNodeStreamWriter writer) throws IOException {
            writer.nextDataSchemaNode(getSchema());
            writer.startUnkeyedListItem(provideNodeIdentifier(), childSizeHint());
            super.write(writer);
            writer.endNode();
        }
    }

    ListEntryNodeDataWithSchema(final ListSchemaNode schema) {
        super(schema);
    }

    public static ListEntryNodeDataWithSchema forSchema(final ListSchemaNode schema) {
        final List<QName> keyDef = schema.getKeyDefinition();
        return keyDef.isEmpty() ? new Unkeyed(schema) :  new Keyed(schema, keyDef);
    }
}
