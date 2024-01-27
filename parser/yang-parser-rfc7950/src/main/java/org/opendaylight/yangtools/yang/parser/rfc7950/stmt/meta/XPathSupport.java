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
import org.opendaylight.yangtools.yang.parser.rfc7950.namespace.YangNamespaceContextNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathExpression.QualifiedBound;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathParser;
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

    QualifiedBound parseXPath(final StmtContext<?, ?, ?> ctx, final String xpath) {
        final YangXPathParser.QualifiedBound parser = factory.newParser(
            YangNamespaceContextNamespace.computeIfAbsent(ctx));
        final QualifiedBound parsed;
        try {
            parsed = parser.parseExpression(xpath);
        } catch (XPathExpressionException e) {
            throw new SourceException(ctx, e, "Argument \"%s\" is not valid XPath string", xpath);
        }

        if (ctx.yangVersion().compareTo(parsed.getYangVersion()) < 0) {
            LOG.warn("{} features required in {} context to parse expression '{}' [at {}]",
                parsed.getYangVersion().reference(), ctx.yangVersion().reference(), xpath, ctx.sourceReference());
        }
        return parsed;
    }
}
