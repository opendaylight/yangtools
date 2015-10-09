/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import java.io.IOException;
import org.opendaylight.yangtools.yang.data.api.schema.stream.SchemaAwareNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

class AnyXmlNodeDataWithSchema extends SimpleNodeDataWithSchema {

    public AnyXmlNodeDataWithSchema(final DataSchemaNode dataSchemaNode) {
        super(dataSchemaNode);
    }

    @Override
    public void write(final SchemaAwareNormalizedNodeStreamWriter writer) throws IOException {
        writer.nextDataSchemaNode(getSchema());
        // FIXME: should be changed according to format of value
        writer.anyxmlNode(provideNodeIdentifier(), getValue());
    }
}
