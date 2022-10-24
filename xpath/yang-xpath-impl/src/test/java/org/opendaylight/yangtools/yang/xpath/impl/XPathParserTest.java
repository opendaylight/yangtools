/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.opendaylight.yangtools.yang.xpath.impl.TestUtils.DEFAULT_NS;
import static org.opendaylight.yangtools.yang.xpath.impl.TestUtils.NAMESPACE_CONTEXT;

import java.util.List;
import javax.xml.xpath.XPathExpressionException;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.xpath.api.YangBinaryExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangBinaryOperator;
import org.opendaylight.yangtools.yang.xpath.api.YangBooleanConstantExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.Relative;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathAxis;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathMathMode;

@SuppressWarnings("null")
public class XPathParserTest {

    private @Nullable AntlrXPathParser parser;

    @BeforeEach
    public void before() {
        parser = new AntlrXPathParser.Unqualified(YangXPathMathMode.IEEE754, NAMESPACE_CONTEXT, DEFAULT_NS);
    }

    @Test
    public void testSmoke() throws XPathExpressionException {
        parseExpr("../a[foo = current()/foo]");

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

    @Test
    public void testGreaterEqualReference() throws XPathExpressionException {
        final YangExpr expr = parseExpr(". >= ../lower-port");
        assertThat(expr, isA(YangBinaryExpr.class));

        final YangBinaryExpr binary = (YangBinaryExpr) expr;
        assertEquals(YangBinaryOperator.GTE, binary.getOperator());
        assertEquals(YangLocationPath.self(), binary.getLeftExpr());
        assertEquals(YangLocationPath.relative(YangXPathAxis.PARENT.asStep(),
            YangXPathAxis.CHILD.asStep(QName.create(DEFAULT_NS, "lower-port"))), binary.getRightExpr());
    }

    @Test
    public void testAnd() throws XPathExpressionException {
        assertRelative("and");
        assertRelative("or");
        assertRelative("div");
        assertRelative("mod");
        assertRelative("andor");
    }

    private void assertRelative(final String str) throws XPathExpressionException {
        final YangExpr expr = parseExpr(str);
        assertThat(expr, isA(Relative.class));
        assertEquals(List.of(YangXPathAxis.CHILD.asStep(QName.create(DEFAULT_NS, str))), ((Relative) expr).getSteps());
    }

    private YangExpr parseExpr(final String xpath) throws XPathExpressionException {
        return parser.parseExpression(xpath).getRootExpr();
    }
}
