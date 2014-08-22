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

import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

class ListNodeDataWithSchema extends CompositeNodeDataWithSchema {

    public ListNodeDataWithSchema(final DataSchemaNode schema) {
        super(schema);
    }

    @Override
    protected void writeToStream(final NormalizedNodeStreamWriter nnStreamWriter) throws IOException {
        if (!((ListSchemaNode) getSchema()).getKeyDefinition().isEmpty()) {
            nnStreamWriter.startMapNode(provideNodeIdentifier(), UNKNOWN_SIZE);
        } else {
            nnStreamWriter.startUnkeyedList(provideNodeIdentifier(), UNKNOWN_SIZE);
        }
        super.writeToStream(nnStreamWriter);
        nnStreamWriter.endNode();
    }

}
