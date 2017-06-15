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
import org.opendaylight.yangtools.yang.model.api.YangModeledAnyXmlSchemaNode;

public final class YangModeledAnyXmlNodeDataWithSchema extends CompositeNodeDataWithSchema {

    public YangModeledAnyXmlNodeDataWithSchema(final YangModeledAnyXmlSchemaNode yangModeledAnyXml) {
        super(yangModeledAnyXml);
    }

    @Override
    public void write(final NormalizedNodeStreamWriter writer) throws IOException {
        writer.nextDataSchemaNode(getSchema());
        writer.startYangModeledAnyXmlNode(provideNodeIdentifier(), childSizeHint());
        super.write(writer);
        writer.endNode();
    }
}
