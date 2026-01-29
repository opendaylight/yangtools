/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yin.source.dom;

import java.io.IOException;
import javax.xml.transform.dom.DOMSource;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.model.api.source.YinTextSource;
import org.opendaylight.yangtools.yang.model.spi.meta.StatementDeclarations;
import org.opendaylight.yangtools.yang.model.spi.source.SourceSyntaxException;
import org.opendaylight.yangtools.yang.model.spi.source.YinDomSource;
import org.opendaylight.yangtools.yang.model.spi.source.YinTextToDOMSourceTransformer;
import org.osgi.service.component.annotations.Component;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Default implementation of {@link YinTextToDOMSourceTransformer}.
 */
@Component
@MetaInfServices
@NonNullByDefault
public final class DefaultYinTextToDOMSourceTransformer implements YinTextToDOMSourceTransformer {
    @Override
    public YinDomSource transformSource(final YinTextSource input) throws SourceSyntaxException {
        final var doc = UntrustedXML.newDocumentBuilder().newDocument();
        final var parser = UntrustedXML.newSAXParser();
        final var handler = new StatementSourceReferenceHandler(doc, null);
        try {
            parser.parse(input.openStream(), handler);
        } catch (IOException e) {
            throw new SourceSyntaxException("Failed to read YIN source", e);
        } catch (SAXParseException e) {
            throw new SourceSyntaxException("Failed to parse YIN source", e, sourceRefOf(e, input.symbolicName()));
        } catch (SAXException e) {
            throw new SourceSyntaxException("Failed to parse YIN source", e);
        }
        return YinDomSource.of(input.sourceId(), new DOMSource(doc), StatementSourceReferenceHandler.REF_PROVIDER,
            input.symbolicName());
    }

    private static @Nullable StatementSourceReference sourceRefOf(final SAXParseException cause,
                final @Nullable String symbolicName) {
        final var lineNumber = cause.getLineNumber();
        final var columnNumber = cause.getColumnNumber();
        return lineNumber < 1 || columnNumber < 1 ? null
            : StatementDeclarations.inText(symbolicName, lineNumber, columnNumber);
    }
}
