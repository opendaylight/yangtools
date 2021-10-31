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
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.xpath.api.YangBinaryExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangBinaryOperator;
import org.opendaylight.yangtools.yang.xpath.api.YangBooleanConstantExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathAxis;

public class XPathParserTest extends AbstractParserTest {
    @Test
    public void testSmoke() {
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
    public void testUnionSquashing() {
        final YangExpr a = assertExpr("a");
        assertEquals(a, assertExpr("a|a"));
        assertEquals(a, assertExpr("a|a|a"));
        assertEquals(a, assertExpr("a|a|a|a"));

        final YangExpr ab = assertExpr("a|b");
        assertEquals(ab, assertExpr("a|b|a"));
        assertEquals(ab, assertExpr("a|b|a|b"));
        assertEquals(ab, assertExpr("a|a|b|a|b"));
        assertEquals(ab, assertExpr("a|b|b|a|b"));
        assertEquals(ab, assertExpr("a|b|a|a|b"));
    }

    @Test
    public void testNumberSquashing() {
        final YangExpr two = assertExpr("2");
        assertEquals(two, assertExpr("1 + 1"));
        assertEquals(two, assertExpr("3 - 1"));
        assertEquals(two, assertExpr("2 * 1"));
        assertEquals(two, assertExpr("4 div 2"));
        assertEquals(two, assertExpr("6 mod 4"));
    }

    @Test
    public void testSameExprSquashing() {
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
    public void testGreaterEqualReference() {
        final YangExpr expr = assertExpr(". >= ../lower-port");
        assertThat(expr, isA(YangBinaryExpr.class));

        final YangBinaryExpr binary = (YangBinaryExpr) expr;
        assertEquals(YangBinaryOperator.GTE, binary.getOperator());
        assertEquals(YangLocationPath.self(), binary.getLeftExpr());
        assertEquals(YangLocationPath.relative(YangXPathAxis.PARENT.asStep(),
            YangXPathAxis.CHILD.asStep(QName.create(DEFNS, "lower-port"))), binary.getRightExpr());
    }
}
