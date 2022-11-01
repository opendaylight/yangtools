/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.dom.DOMSource;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.AbstractNormalizableAnydata;
import org.opendaylight.yangtools.yang.model.api.EffectiveStatementInference;

/**
 * Internal parser representation of a parsed-out chunk of XML. This format is completely internal to the parser
 * and can be changed at any time. Current implementation uses W3C DOM tree as the backing implementations, but others
 * are possible as well.
 *
 * <p>
 * Note that the DOMSource is expected to contain a top-level synthetic element, which acts as holder of namespace
 * declarations coming from parsing context but is otherwise ignored. Parser-side of things is expected to reuse the
 * {@code anydata} element name for this purpose. Writer-side of things is expected to skip this element except for
 * its namespace declarations.
 *
 * @author Robert Varga
 */
@NonNullByDefault
final class DOMSourceAnydata extends AbstractNormalizableAnydata {
    private final DOMSource source;

    DOMSourceAnydata(final DOMSource source) {
        this.source = requireNonNull(source);
    }

    XMLStreamReader toStreamReader() throws XMLStreamException {
        return new DOMSourceXMLStreamReader(source);
    }

    @Override
    protected void writeTo(final NormalizedNodeStreamWriter streamWriter, final EffectiveStatementInference inference)
            throws IOException {
        final XmlParserStream xmlParser;
        try {
            xmlParser = XmlParserStream.create(streamWriter, inference);
        } catch (IllegalArgumentException e) {
            throw new IOException("Failed to instantiate XML parser", e);
        }

        try {
            final XMLStreamReader reader = toStreamReader();
            reader.nextTag();

            xmlParser.parse(reader).flush();
        } catch (XMLStreamException e) {
            throw new IOException("Failed to parse payload", e);
        }
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("source", source);
    }

    @VisibleForTesting
    DOMSource getSource() {
        return source;
    }
}
