/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import java.util.Iterator;
import java.util.ServiceLoader;
import javax.xml.xpath.XPathExpressionException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.util.RevisionAwareXPathImpl;
import org.opendaylight.yangtools.yang.parser.rfc7950.namespace.YangNamespaceContextNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathExpression.QualifiedBound;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathParser;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
abstract class XPathSupport {
    private static final Logger LOG = LoggerFactory.getLogger(XPathSupport.class);

    private static final XPathSupport INSTANCE;

    static {
        final Iterator<YangXPathParserFactory> it = ServiceLoader.load(YangXPathParserFactory.class).iterator();
        if (!it.hasNext()) {
            LOG.warn("Failed to find XPath parser factory, no XPath validation will be performed");
            INSTANCE = new Noop();
        } else {
            INSTANCE = new XPathImpl(it.next());
        }
    }

    static RevisionAwareXPath parseXPath(final StmtContext<?, ?, ?> ctx, final String xpath) {
        return INSTANCE.parseXPath(xpath, ctx);
    }

    abstract RevisionAwareXPath parseXPath(String xpath, StmtContext<?, ?, ?> ctx);

    private static final class Noop extends XPathSupport {
        @Override
        RevisionAwareXPath parseXPath(final String xpath, final StmtContext<?, ?, ?> ctx) {
            return new RevisionAwareXPathImpl(xpath, ArgumentUtils.isAbsoluteXPath(xpath));
        }
    }

    private static final class XPathImpl extends XPathSupport {
        private final YangXPathParserFactory factory;

        XPathImpl(final YangXPathParserFactory factory) {
            this.factory = factory;
        }

        @Override
        RevisionAwareXPath parseXPath(final String xpath, final StmtContext<?, ?, ?> ctx) {
            final boolean isAbsolute = ArgumentUtils.isAbsoluteXPath(xpath);
            final YangXPathParser.QualifiedBound parser = factory.newParser(
                YangNamespaceContextNamespace.computeIfAbsent(ctx));
            final QualifiedBound parsed;
            try {
                parsed = parser.parseExpression(xpath);
            } catch (XPathExpressionException e) {
                LOG.warn("Argument \"{}\" is not valid XPath string at \"{}\"", xpath,
                    ctx.getStatementSourceReference(), e);
                return new RevisionAwareXPathImpl(xpath, isAbsolute);
            }

            if (ctx.getRootVersion().compareTo(parsed.getYangVersion()) < 0) {
                LOG.warn("{} features required in {} context to parse expression '{}' [at {}]",
                    parsed.getYangVersion().getReference(), ctx.getRootVersion().getReference(), xpath,
                    ctx.getStatementSourceReference());
            }
            return new WithExpressionImpl(xpath, isAbsolute, parsed);
        }
    }
}
