/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.impl;

import com.google.common.collect.ImmutableBiMap;
import javax.xml.xpath.XPathExpressionException;
import org.opendaylight.yangtools.yang.common.BiMapYangNamespaceContext;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.common.YangNamespaceContext;
import org.opendaylight.yangtools.yang.xpath.api.YangExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathExpression;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathMathMode;

public abstract class AbstractParserTest {
    static final QNameModule DEFNS = QNameModule.create(XMLNamespace.of("defaultns"));
    private static final YangNamespaceContext CONTEXT = new BiMapYangNamespaceContext(ImmutableBiMap.of(
        "def", DEFNS,
        "foo", QNameModule.create(XMLNamespace.of("foo")),
        "bar", QNameModule.create(XMLNamespace.of("bar"))));

    private final AntlrXPathParser parser = new AntlrXPathParser.Unqualified(YangXPathMathMode.IEEE754, CONTEXT, DEFNS);

    final YangExpr assertExpr(final String xpath) {
        return assertXPath(xpath).getRootExpr();
    }

    final YangXPathExpression assertXPath(final String xpath) {
        try {
            return parser.parseExpression(xpath);
        } catch (XPathExpressionException e) {
            throw new AssertionError(e);
        }
    }
}
