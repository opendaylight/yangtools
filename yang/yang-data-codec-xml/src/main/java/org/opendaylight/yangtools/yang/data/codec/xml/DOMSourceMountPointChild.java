/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.net.URISyntaxException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.dom.DOMSource;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.AbstractMountPointChild;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.xml.sax.SAXException;

/**
 * Internal MountPointChild implementation, reusing data bits from {@link DOMSourceAnydata}.
 */
@NonNullByDefault
final class DOMSourceMountPointChild extends AbstractMountPointChild {
    private final DOMSource source;

    DOMSourceMountPointChild(final DOMSource source) {
        this.source = requireNonNull(source);
    }

    @Override
    public void writeTo(final NormalizedNodeStreamWriter writer, final SchemaContext schemaContext)
            throws IOException {

        final XmlParserStream xmlParser;
        try {
            xmlParser = XmlParserStream.create(writer, schemaContext, schemaContext);
        } catch (IllegalArgumentException e) {
            throw new IOException("Failed to instantiate XML parser", e);
        }

        try {
            final XMLStreamReader reader = new DOMSourceXMLStreamReader(source);
            xmlParser.parse(reader).flush();
        } catch (XMLStreamException | URISyntaxException | SAXException e) {
            throw new IOException("Failed to parse payload", e);
        }
    }
}
