/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.impl;

import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.yang.common.YangNamespaceContext;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathMathMode;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathParser;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathParserFactory;

@MetaInfServices
public final class AntlrXPathParserFactory implements YangXPathParserFactory {
    @Override
    public YangXPathParser newParser(final YangNamespaceContext namespaceContext,
            final YangXPathMathMode mathMode) {
        return new AntlrXPathParser(mathMode, namespaceContext);
    }
}
