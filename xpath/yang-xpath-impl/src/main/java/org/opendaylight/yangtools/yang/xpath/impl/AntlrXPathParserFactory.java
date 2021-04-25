/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.impl;

import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangNamespaceContext;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathMathMode;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathParser;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathParser.QualifiedBound;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathParser.UnqualifiedBound;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathParserFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@MetaInfServices
@Component(immediate = true)
public class AntlrXPathParserFactory implements YangXPathParserFactory {
    private static final Logger LOG = LoggerFactory.getLogger(AntlrXPathParserFactory.class);

    @Override
    public final YangXPathParser newParser(final YangXPathMathMode mathMode) {
        return new AntlrXPathParser.Base(mathMode);
    }

    @Override
    public final QualifiedBound newParser(final YangXPathMathMode mathMode,
            final YangNamespaceContext namespaceContext) {
        return new AntlrXPathParser.Qualified(mathMode, namespaceContext);
    }

    @Override
    public final UnqualifiedBound newParser(final YangXPathMathMode mathMode,
            final YangNamespaceContext namespaceContext, final QNameModule defaultNamespace) {
        return new AntlrXPathParser.Unqualified(mathMode, namespaceContext, defaultNamespace);
    }

    @Activate
    @SuppressWarnings("static-method")
    void activate() {
        LOG.info("XPath Parser activated");
    }

    @Deactivate
    @SuppressWarnings("static-method")
    void deactivate() {
        LOG.info("XPath Parser deactivated");
    }
}
