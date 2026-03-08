/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta;

import static java.util.Objects.requireNonNull;

import javax.xml.xpath.XPathExpressionException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.YangNamespaceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.CommonStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathExpression.QualifiedBound;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
public final class XPathSupport {
    private static final Logger LOG = LoggerFactory.getLogger(XPathSupport.class);

    private final YangXPathParserFactory factory;

    public XPathSupport(final YangXPathParserFactory factory) {
        this.factory = requireNonNull(factory);
    }

    QualifiedBound parseXPath(final CommonStmtCtx stmt, final YangNamespaceContext namespaceContext,
            final String rawArgument) {
        final var parser = factory.newParser(namespaceContext);
        final QualifiedBound parsed;
        try {
            parsed = parser.parseExpression(rawArgument);
        } catch (XPathExpressionException e) {
            throw new SourceException(stmt, e, "Argument \"%s\" is not valid XPath string", rawArgument);
        }

        final var sourceVersion = stmt.sourceVersion();
        if (sourceVersion.compareTo(parsed.getYangVersion()) < 0) {
            LOG.warn("{} features required in {} context to parse expression '{}' [at {}]",
                parsed.getYangVersion().reference(), sourceVersion.reference(), rawArgument, stmt.sourceReference());
        }
        return parsed;
    }
}
