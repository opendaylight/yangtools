/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.xpath;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import java.net.URI;
import java.util.Map;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.antlrv4.code.gen.xpathParser.ExprContext;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.xpath.YangBooleanExpr;
import org.opendaylight.yangtools.yang.model.api.xpath.YangExpr;

@SuppressWarnings("null")
public class XPathParserTest {
    private static final QNameModule DEF_NS = QNameModule.create(URI.create("defaultns"));
    private static final Map<String, QNameModule> NAMESPACES = ImmutableMap.of(
        "foo", QNameModule.create(URI.create("foo")),
        "bar", QNameModule.create(URI.create("bar")));

    private @Nullable XPathParser parser;

    @Before
    public void before() {
        parser = new ExactXPathParser(DEF_NS, NAMESPACES::get);
    }

    @Test
    public void testSmoke() {
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
        parseExpr("$foo:comment");
        parseExpr("$comment");
        parseExpr("$self");
    }

    @Test
    public void testUnionSquashing() {
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
    public void testNumberSquashing() {
        final YangExpr two = parseExpr("2");
        assertEquals(two, parseExpr("1 + 1"));
        assertEquals(two, parseExpr("3 - 1"));
        assertEquals(two, parseExpr("2 * 1"));
        assertEquals(two, parseExpr("4 div 2"));
        assertEquals(two, parseExpr("6 mod 4"));
    }

    @Test
    public void testSameExprSquashing() {
        // Expressions
        assertEquals(YangBooleanExpr.FALSE, parseExpr("/a != /a"));
        assertEquals(YangBooleanExpr.TRUE, parseExpr("/a = /a"));

        // Numbers
        assertEquals(YangBooleanExpr.FALSE, parseExpr("2 != 2"));
        assertEquals(YangBooleanExpr.FALSE, parseExpr("2 != (1 + 1)"));
        assertEquals(YangBooleanExpr.TRUE, parseExpr("2 = 2"));
        assertEquals(YangBooleanExpr.TRUE, parseExpr("2 = (1 + 1)"));
    }

    private YangExpr parseExpr(final String xpath) {
        return parser.parseExpr(parseXPath(xpath));
    }

    private static ExprContext parseXPath(final String xpath) {
        return XPathParser.parseXPath(xpath, new BaseErrorListener() {
            @Override
            public void syntaxError(final @Nullable Recognizer<?, ?> recognizer, final @Nullable Object offendingSymbol,
                    final int line, final int charPositionInLine, final @Nullable String msg,
                    final @Nullable RecognitionException e) {
                throw new IllegalArgumentException(msg, e);
            }
        });
    }
}
