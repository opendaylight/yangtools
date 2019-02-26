/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.impl;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableBiMap;
import java.net.URI;
import javax.xml.xpath.XPathExpressionException;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.BiMapYangNamespaceContext;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangNamespaceContext;
import org.opendaylight.yangtools.yang.xpath.api.YangBooleanConstantExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathMathMode;

@SuppressWarnings("null")
public class XPathParserTest {
    private static final YangNamespaceContext CONTEXT = new BiMapYangNamespaceContext(
        QNameModule.create(URI.create("defaultns")), ImmutableBiMap.of(
            "foo", QNameModule.create(URI.create("foo")),
            "bar", QNameModule.create(URI.create("bar"))));

    private @Nullable AntlrXPathParser parser;

    @Before
    public void before() {
        parser = new AntlrXPathParser(YangXPathMathMode.IEEE754, CONTEXT);
    }

    @Test
    public void testSmoke() throws XPathExpressionException {
        parseExpr("3 + 5");
        parseExpr("/a/b");
        parseExpr("a/b");
        parseExpr("./a/b");
        parseExpr("../a/b");
        parseExpr("foo:foo");
        parseExpr("@foo");
        parseExpr("@foo:foo");
        parseExpr("current()");
        parseExpr("foo:self()");
        parseExpr("foo:comment()");
        parseExpr("/a[foo = 2 and bar = 3]");
        parseExpr("/a[foo = \"2\" and bar = '3']");
        parseExpr("/foo:a[foo = 2 and bar = 3]");
        parseExpr("//a[foo = 2 and bar = 3]");
        parseExpr("//foo:a[foo=2 and bar:bar=3]");
        parseExpr("a//b[foo = 2]");
        parseExpr("foo:a//b[foo = 2]");
        parseExpr("$foo:comment");
        parseExpr("$comment");
        parseExpr("$self");
    }

    @Test
    public void testUnionSquashing() throws XPathExpressionException {
        final YangExpr a = parseExpr("a");
        assertEquals(a, parseExpr("a|a"));
        assertEquals(a, parseExpr("a|a|a"));
        assertEquals(a, parseExpr("a|a|a|a"));

        final YangExpr ab = parseExpr("a|b");
        assertEquals(ab, parseExpr("a|b|a"));
        assertEquals(ab, parseExpr("a|b|a|b"));
        assertEquals(ab, parseExpr("a|a|b|a|b"));
        assertEquals(ab, parseExpr("a|b|b|a|b"));
        assertEquals(ab, parseExpr("a|b|a|a|b"));
    }

    @Test
    public void testNumberSquashing() throws XPathExpressionException {
        final YangExpr two = parseExpr("2");
        assertEquals(two, parseExpr("1 + 1"));
        assertEquals(two, parseExpr("3 - 1"));
        assertEquals(two, parseExpr("2 * 1"));
        assertEquals(two, parseExpr("4 div 2"));
        assertEquals(two, parseExpr("6 mod 4"));
    }

    @Test
    public void testSameExprSquashing() throws XPathExpressionException {
        // Expressions
        assertEquals(YangBooleanConstantExpr.FALSE, parseExpr("/a != /a"));
        assertEquals(YangBooleanConstantExpr.TRUE, parseExpr("/a = /a"));

        // Numbers
        assertEquals(YangBooleanConstantExpr.FALSE, parseExpr("2 != 2"));
        assertEquals(YangBooleanConstantExpr.FALSE, parseExpr("2 != (1 + 1)"));
        assertEquals(YangBooleanConstantExpr.TRUE, parseExpr("2 = 2"));
        assertEquals(YangBooleanConstantExpr.TRUE, parseExpr("2 = (1 + 1)"));
    }

    private YangExpr parseExpr(final String xpath) throws XPathExpressionException {
        return parser.parseExpression(xpath).getRootExpr();
    }
}
