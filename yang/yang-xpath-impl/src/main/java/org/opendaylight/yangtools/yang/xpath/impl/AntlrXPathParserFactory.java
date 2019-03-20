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

@MetaInfServices
public final class AntlrXPathParserFactory implements YangXPathParserFactory {
    @Override
    public YangXPathParser newParser(final YangXPathMathMode mathMode) {
        return new AntlrXPathParser.Base(mathMode);
    }

    @Override
    public QualifiedBound newParser(final YangXPathMathMode mathMode, final YangNamespaceContext namespaceContext) {
        return new AntlrXPathParser.Qualified(mathMode, namespaceContext);
    }

    @Override
    public UnqualifiedBound newParser(final YangXPathMathMode mathMode, final YangNamespaceContext namespaceContext,
            final QNameModule defaultNamespace) {
        return new AntlrXPathParser.Unqualified(mathMode, namespaceContext, defaultNamespace);
    }
}
