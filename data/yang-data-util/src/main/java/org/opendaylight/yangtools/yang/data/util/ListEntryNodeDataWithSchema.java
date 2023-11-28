/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import com.google.common.base.VerifyException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.util.ImmutableMapTemplate;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter.MetadataExtension;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

/**
 * Utility class used for tracking parser state as needed by a StAX-like parser.
 * This class is to be used only by respective XML and JSON parsers in yang-data-codec-xml and yang-data-codec-gson.
 *
 * <p>
 * Represents a YANG list entry node.
 */
public abstract sealed class ListEntryNodeDataWithSchema extends AbstractMountPointDataWithSchema<ListSchemaNode> {
    private static final class Keyed extends ListEntryNodeDataWithSchema {
        private final Map<QName, SimpleNodeDataWithSchema<?>> keyValues = new HashMap<>();
        // This template results in Maps in schema definition order
        private final ImmutableMapTemplate<QName> predicateTemplate;

        Keyed(final ListSchemaNode schema, final List<QName> keyDef) {
            super(schema);
            predicateTemplate = ImmutableMapTemplate.ordered(keyDef);
        }

        @Override
        void addChild(final AbstractNodeDataWithSchema<?> newChild) {
            if (newChild.getSchema() instanceof LeafSchemaNode leaf) {
                final var childName = leaf.getQName();
                if (predicateTemplate.keySet().contains(childName)) {
                    if (newChild instanceof SimpleNodeDataWithSchema<?> simpleChild) {
                        keyValues.put(childName, simpleChild);
                    } else {
                        throw new VerifyException("Unexpected child " + newChild);
                    }
                }
            }
            super.addChild(newChild);
        }

        @Override
        public void write(final NormalizedNodeStreamWriter writer, final MetadataExtension metaWriter)
                throws IOException {
            final var schema = getSchema();
            writer.nextDataSchemaNode(schema);

            final var nodeType = schema.getQName();
            final Map<QName, Object> predicates;
            try {
                predicates = predicateTemplate.instantiateTransformed(keyValues, (key, node) -> node.getValue());
            } catch (IllegalArgumentException e) {
                final var present = keyValues.keySet();
                final var module = nodeType.getModule();
                final var missing = predicateTemplate.keySet().stream()
                    .filter(key -> !present.contains(key))
                    .map(key -> module.equals(key.getModule()) ? key.getLocalName() : key)
                    .distinct()
                    .toList();
                throw new IOException("List entry " + nodeType + " is missing leaf values for " + missing, e);
            }

            writer.startMapEntryNode(NodeIdentifierWithPredicates.of(nodeType, predicates), childSizeHint());
            writeMetadata(metaWriter);
            super.write(writer, metaWriter);
            writer.endNode();
        }
    }

    private static final class Unkeyed extends ListEntryNodeDataWithSchema {
        Unkeyed(final ListSchemaNode schema) {
            super(schema);
        }

        @Override
        public void write(final NormalizedNodeStreamWriter writer, final MetadataExtension metaWriter)
                throws IOException {
            writer.nextDataSchemaNode(getSchema());
            writer.startUnkeyedListItem(provideNodeIdentifier(), childSizeHint());
            super.write(writer, metaWriter);
            writer.endNode();
        }
    }

    ListEntryNodeDataWithSchema(final ListSchemaNode schema) {
        super(schema);
    }

    static @NonNull ListEntryNodeDataWithSchema forSchema(final ListSchemaNode schema) {
        final var keyDef = schema.getKeyDefinition();
        return keyDef.isEmpty() ? new Unkeyed(schema) : new Keyed(schema, keyDef);
    }
}
