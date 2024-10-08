/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import java.io.IOException;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter.MetadataExtension;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

/**
 * Utility class used for tracking parser state as needed by a StAX-like parser. This class is to be used only by
 * respective XML and JSON parsers in yang-data-codec-xml and yang-data-codec-gson.
 *
 * <p>Represents a YANG list node.
 */
public final class ListNodeDataWithSchema extends CompositeNodeDataWithSchema<ListSchemaNode>
        implements MultipleEntryDataWithSchema<ListEntryNodeDataWithSchema> {
    ListNodeDataWithSchema(final ListSchemaNode schema) {
        super(schema);
    }

    @Override
    public void write(final NormalizedNodeStreamWriter writer, final MetadataExtension metaWriter) throws IOException {
        final ListSchemaNode schema = getSchema();
        writer.nextDataSchemaNode(schema);
        if (schema.getKeyDefinition().isEmpty()) {
            writer.startUnkeyedList(provideNodeIdentifier(), childSizeHint());
        } else if (schema.isUserOrdered()) {
            writer.startOrderedMapNode(provideNodeIdentifier(), childSizeHint());
        } else {
            writer.startMapNode(provideNodeIdentifier(), childSizeHint());
        }
        super.write(writer, metaWriter);
        writer.endNode();
    }

    @Override
    public ListEntryNodeDataWithSchema newChildEntry() {
        final ListEntryNodeDataWithSchema child = ListEntryNodeDataWithSchema.forSchema(getSchema());
        addChild(child);
        return child;
    }
}
