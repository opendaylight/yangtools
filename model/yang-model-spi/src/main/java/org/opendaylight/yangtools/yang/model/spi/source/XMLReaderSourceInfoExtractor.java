/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.source;

import static java.util.Objects.requireNonNull;

import java.io.Reader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.meta.StatementDeclarations;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.ExtractorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
/**
 * Utility class for {@link SourceInfo} from a XML stream.
 */
@NonNullByDefault
final class XMLReaderSourceInfoExtractor {
    private static final Logger LOG = LoggerFactory.getLogger(XMLReaderSourceInfoExtractor.class);

    private final SourceIdentifier sourceId;
    private final XMLStreamReader reader;

    private XMLReaderSourceInfoExtractor(final SourceIdentifier sourceId, final XMLStreamReader reader) {
        this.sourceId = requireNonNull(sourceId);
        this.reader = requireNonNull(reader);
    }

    static SourceInfo extractSourceInfo(final SourceIdentifier sourceId, final Reader input)
            throws ExtractorException {
        final XMLStreamReader reader;
        try {
            reader = UntrustedXML.createXMLStreamReader(input);
        } catch (XMLStreamException e) {
            throw new ExtractorException(StatementDeclarations.inText(sourceId.name().getLocalName(), 1, 1),
                "Failed to open XML stream", e);
        }

        try {
            return new XMLReaderSourceInfoExtractor(sourceId, reader).extractSourceInfo();
        } finally {
            try {
                reader.close();
            } catch (XMLStreamException e) {
                LOG.warn("Failed to close XML stream", e);
            }
        }
    }

    private SourceInfo extractSourceInfo() throws ExtractorException {
        try {
            return consumeReader();
        } catch (XMLStreamException e) {
            throw new ExtractorException(currentRef(), "Failed to read XML stream", e);
        }
    }

    private SourceInfo consumeReader() throws ExtractorException, XMLStreamException {
        // FIXME: implement this
        throw new UnsupportedOperationException();
    }

    private StatementSourceReference currentRef() {
        final var location = reader.getLocation();
        // FIXME: implement this
        throw new UnsupportedOperationException();
    }
}
