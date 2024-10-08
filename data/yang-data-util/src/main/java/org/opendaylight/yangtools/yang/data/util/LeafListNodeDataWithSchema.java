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
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;

/**
 * Utility class used for tracking parser state as needed by a StAX-like parser. This class is to be used only by
 * respective XML and JSON parsers in yang-data-codec-xml and yang-data-codec-gson.
 *
 * <p>Represents a YANG leaf-list node.
 */
public final class LeafListNodeDataWithSchema extends CompositeNodeDataWithSchema<LeafListSchemaNode>
        implements MultipleEntryDataWithSchema<LeafListEntryNodeDataWithSchema> {
    LeafListNodeDataWithSchema(final LeafListSchemaNode schema) {
        super(schema);
    }

    @Override
    public void write(final NormalizedNodeStreamWriter writer, final MetadataExtension metaWriter) throws IOException {
        final LeafListSchemaNode schema = getSchema();
        writer.nextDataSchemaNode(schema);
        if (schema.isUserOrdered()) {
            writer.startOrderedLeafSet(provideNodeIdentifier(), childSizeHint());
        } else {
            writer.startLeafSet(provideNodeIdentifier(), childSizeHint());
        }
        super.write(writer, metaWriter);
        writer.endNode();
    }

    @Override
    public LeafListEntryNodeDataWithSchema newChildEntry() {
        final LeafListEntryNodeDataWithSchema child = new LeafListEntryNodeDataWithSchema(getSchema());
        addChild(child);
        return child;
    }
}
