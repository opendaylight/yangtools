/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.google.common.collect.ImmutableBiMap;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.BiMapYangNamespaceContext;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangNamespaceContext;
import org.opendaylight.yangtools.yang.xpath.api.YangBinaryExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangBinaryOperator;
import org.opendaylight.yangtools.yang.xpath.api.YangBooleanConstantExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.Relative;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathAxis;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathMathMode;

class XPathParserTest {
    private static final QNameModule DEFNS = QNameModule.of("defaultns");
    private static final YangNamespaceContext CONTEXT = new BiMapYangNamespaceContext(ImmutableBiMap.of(
        "def", DEFNS,
        "foo", QNameModule.of("foo"),
        "bar", QNameModule.of("bar")));

    private static final AntlrXPathParser PARSER =
        new AntlrXPathParser.Unqualified(YangXPathMathMode.IEEE754, CONTEXT, DEFNS);

    @Test
    void testSmoke() throws Exception {
        assertExpr("../a[foo = current()/foo]");

        assertExpr("3 + 5");
        assertExpr("/a/b");
        assertExpr("a/b");
        assertExpr("./a/b");
        assertExpr("../a/b");
        assertExpr("foo:foo");
        assertExpr("@foo");
        assertExpr("@foo:foo");
        assertExpr("current()");
        assertExpr("foo:self()");
        assertExpr("foo:comment()");
        assertExpr("/a[foo = 2 and bar = 3]");
        assertExpr("/a[foo = \"2\" and bar = '3']");
        assertExpr("/foo:a[foo = 2 and bar = 3]");
        assertExpr("//a[foo = 2 and bar = 3]");
        assertExpr("//foo:a[foo=2 and bar:bar=3]");
        assertExpr("a//b[foo = 2]");
        assertExpr("foo:a//b[foo = 2]");
        assertExpr("$foo:comment");
        assertExpr("$comment");
        assertExpr("$self");
    }

    @Test
    void testUnionSquashingOne() throws Exception {
        final var expr = assertExpr("a");
        assertEquals(expr, assertExpr("a|a"));
        assertEquals(expr, assertExpr("a|a|a"));
        assertEquals(expr, assertExpr("a|a|a|a"));
    }

    @Test
    void testUnionSquashingTwo() throws Exception {
        final var expr = assertExpr("a|b");
        assertEquals(expr, assertExpr("a|b|a"));
        assertEquals(expr, assertExpr("a|b|a|b"));
        assertEquals(expr, assertExpr("a|a|b|a|b"));
        assertEquals(expr, assertExpr("a|b|b|a|b"));
        assertEquals(expr, assertExpr("a|b|a|a|b"));
    }

    @Test
    void testNumberSquashing() throws Exception {
        final var expr = assertExpr("2");
        assertEquals(expr, assertExpr("1 + 1"));
        assertEquals(expr, assertExpr("3 - 1"));
        assertEquals(expr, assertExpr("2 * 1"));
        assertEquals(expr, assertExpr("4 div 2"));
        assertEquals(expr, assertExpr("6 mod 4"));
    }

    @Test
    void testSameExprSquashing() throws Exception {
        // Expressions
        assertEquals(YangBooleanConstantExpr.FALSE, assertExpr("/a != /a"));
        assertEquals(YangBooleanConstantExpr.TRUE, assertExpr("/a = /a"));

        // Numbers
        assertEquals(YangBooleanConstantExpr.FALSE, assertExpr("2 != 2"));
        assertEquals(YangBooleanConstantExpr.FALSE, assertExpr("2 != (1 + 1)"));
        assertEquals(YangBooleanConstantExpr.TRUE, assertExpr("2 = 2"));
        assertEquals(YangBooleanConstantExpr.TRUE, assertExpr("2 = (1 + 1)"));
    }

    @Test
    void testGreaterEqualReference() throws Exception {
        final var expr = assertInstanceOf(YangBinaryExpr.class, assertExpr(". >= ../lower-port"));
        assertEquals(YangBinaryOperator.GTE, expr.getOperator());
        assertEquals(YangLocationPath.self(), expr.getLeftExpr());
        assertEquals(YangLocationPath.relative(YangXPathAxis.PARENT.asStep(),
            YangXPathAxis.CHILD.asStep(QName.create(DEFNS, "lower-port"))), expr.getRightExpr());
    }

    @Test
    void testAnd() throws Exception {
        assertRelative("and");
        assertRelative("or");
        assertRelative("div");
        assertRelative("mod");
        assertRelative("andor");
    }

    private static void assertRelative(final String str) throws Exception {
        final var expr = assertInstanceOf(Relative.class, assertExpr(str));
        assertEquals(List.of(YangXPathAxis.CHILD.asStep(QName.create(DEFNS, str))), expr.getSteps());
    }

    private static YangExpr assertExpr(final String xpath) throws Exception {
        return PARSER.parseExpression(xpath).getRootExpr();
    }
}
