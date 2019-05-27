/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import java.io.IOException;
import java.net.URISyntaxException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.dom.DOMSource;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.AbstractNormalizableAnydata;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextNode;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.xml.sax.SAXException;

@NonNullByDefault
final class DOMSourceAnydata extends AbstractNormalizableAnydata<DOMSource> {
    DOMSourceAnydata(final DOMSource data) {
        super(data);
    }

    @Override
    protected void writeTo(final NormalizedNodeStreamWriter streamWriter, final DataSchemaContextTree contextTree,
            final DataSchemaContextNode<?> contextNode) throws IOException {
        // TODO: this is rather ugly
        final DataSchemaNode root = contextTree.getRoot().getDataSchemaNode();
        if (!(root instanceof SchemaContext)) {
            throw new IOException("Unexpected root context " + root);
        }

        final XmlParserStream xmlParser;
        try {
            xmlParser = XmlParserStream.create(streamWriter, (SchemaContext) root,
                contextNode.getDataSchemaNode());
        } catch (IllegalArgumentException e) {
            throw new IOException("Failed to instantiate XML parser", e);
        }

        try {
            xmlParser.traverse(getData()).close();
        } catch (XMLStreamException | URISyntaxException | SAXException e) {
            throw new IOException("Failed to parse payload", e);
        }
    }
}
