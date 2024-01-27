/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.ImmutableBiMap;
import java.util.List;
import java.util.stream.Stream;
import javax.xml.xpath.XPathExpressionException;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.opendaylight.yangtools.yang.common.AbstractQName;
import org.opendaylight.yangtools.yang.common.BiMapYangNamespaceContext;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName;
import org.opendaylight.yangtools.yang.common.YangNamespaceContext;
import org.opendaylight.yangtools.yang.xpath.api.YangBinaryExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangBinaryOperator;
import org.opendaylight.yangtools.yang.xpath.api.YangExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangLiteralExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.Absolute;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.QNameStep;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.Step;
import org.opendaylight.yangtools.yang.xpath.api.YangQNameExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathMathMode;

class InstanceIdentifierParserTest {
    private static final QNameModule DEFNS = QNameModule.of("defaultns");
    private static final YangNamespaceContext CONTEXT = new BiMapYangNamespaceContext(ImmutableBiMap.of(
        "def", DEFNS,
        "foo", QNameModule.of("foo"),
        "bar", QNameModule.of("bar")));

    private static final QName FOO_FOO_QUALIFIED = CONTEXT.createQName("foo", "foo");
    private static final QName FOO_X_QUALIFIED = CONTEXT.createQName("foo", "x");
    private static final UnresolvedQName FOO_FOO_UNRESOLVED = UnresolvedQName.Qualified.of("foo", "foo");
    private static final UnresolvedQName FOO_X_UNRESOLVED = UnresolvedQName.Qualified.of("foo", "x");
    private static final UnresolvedQName FOO_UNRESOLVED = UnresolvedQName.Unqualified.of("foo");
    private static final UnresolvedQName X_UNRESOLVED = UnresolvedQName.Unqualified.of("x");

    private static final YangExpr POSITION_EXPR = FunctionSupport.POSITION;
    private static final YangExpr SELF_EXPR = YangLocationPath.self();

    private static final InstanceIdentifierParser BASE = new InstanceIdentifierParser.Base(YangXPathMathMode.IEEE754);
    private static final InstanceIdentifierParser QUALIFIED =
        new InstanceIdentifierParser.Qualified(YangXPathMathMode.IEEE754, CONTEXT);

    @ParameterizedTest(name = "Smoke test: {0}")
    @MethodSource("smokeTestArgs")
    void smokeTest(final String input) throws Exception {
        parseBase(input);
        parseQualified(input);
    }

    private static List<String> smokeTestArgs() {
        return List.of("/foo:foo/bar:bar", "/foo/bar", "/foo/bar[ 1 ]", "/foo/bar[11]", "/foo:foo[.='abc']",
                "/foo[ . = \"abc\" ]", "/bar:bar[foo:x = 'a'][foo:y = 'b']", "/a/b/c");
    }

    @ParameterizedTest(name = "Qualified parsing: {0}")
    @MethodSource("qualifiedParsingArgs")
    void qualifiedParsing(final String input, final AbstractQName expectedQName,
            final @Nullable YangExpr expectedLeft, final @Nullable YangExpr expectedRight) throws Exception {
        assertParsed(parseQualified(input), expectedQName, expectedLeft, expectedRight);
    }

    private static Stream<Arguments> qualifiedParsingArgs() {
        return Stream.of(
                // input, expected QName, expected left expr of predicate, expected right expr of predicate
                Arguments.of("/foo:foo", FOO_FOO_QUALIFIED, null, null),
                Arguments.of("/foo", FOO_UNRESOLVED, null, null),
                Arguments.of("/foo:foo[1]", FOO_FOO_QUALIFIED, POSITION_EXPR, toNumberExpr(1)),
                Arguments.of("/foo[ 101 ]", FOO_UNRESOLVED, POSITION_EXPR, toNumberExpr(101)),
                Arguments.of("/foo:foo[foo:x='abc']", FOO_FOO_QUALIFIED,
                        YangQNameExpr.of(FOO_X_QUALIFIED), YangLiteralExpr.of("abc")),
                Arguments.of("/foo[ x = \"xyz\" ]", FOO_UNRESOLVED,
                        YangQNameExpr.of(X_UNRESOLVED), YangLiteralExpr.of("xyz")),
                Arguments.of("/foo:foo[.=\"\"]", FOO_FOO_QUALIFIED, SELF_EXPR, YangLiteralExpr.of("")),
                Arguments.of("/foo[ . = \"value\" ]", FOO_UNRESOLVED, SELF_EXPR, YangLiteralExpr.of("value"))
        );
    }

    @ParameterizedTest(name = "Base parsing: {0}")
    @MethodSource("baseParsingArgs")
    void baseParsing(final String input, final AbstractQName expectedQName,
            @Nullable final YangExpr expectedLeft, @Nullable final YangExpr expectedRight) throws Exception {
        assertParsed(parseBase(input), expectedQName, expectedLeft, expectedRight);
    }

    private static Stream<Arguments> baseParsingArgs() {
        return Stream.of(
                // input, expected QName, expected left expr of predicate, expected right expr of predicate
                Arguments.of("/foo:foo", FOO_FOO_UNRESOLVED, null, null),
                Arguments.of("/foo", FOO_UNRESOLVED, null, null),
                Arguments.of("/foo:foo[1]", FOO_FOO_UNRESOLVED, POSITION_EXPR, toNumberExpr(1)),
                Arguments.of("/foo[ 101 ]", FOO_UNRESOLVED, POSITION_EXPR, toNumberExpr(101)),
                Arguments.of("/foo:foo[foo:x='abc']", FOO_FOO_UNRESOLVED,
                        YangQNameExpr.of(FOO_X_UNRESOLVED), YangLiteralExpr.of("abc")),
                Arguments.of("/foo[ x = \"xyz\" ]", FOO_UNRESOLVED,
                        YangQNameExpr.of(X_UNRESOLVED), YangLiteralExpr.of("xyz")),
                Arguments.of("/foo:foo[.=\"\"]", FOO_FOO_UNRESOLVED, SELF_EXPR, YangLiteralExpr.of("")),
                Arguments.of("/foo[ . = \"value\" ]", FOO_UNRESOLVED, SELF_EXPR, YangLiteralExpr.of("value"))
        );
    }

    private static void assertParsed(final Absolute parsed, final AbstractQName expectedQName,
            final @Nullable YangExpr expectedLeft, final @Nullable YangExpr expectedRight) throws Exception {
        final var step = assertInstanceOf(QNameStep.class, extractFirstStep(parsed));
        assertEquals(expectedQName, step.getQName());

        if (expectedLeft == null) {
            return;
        }

        final var predicate = assertInstanceOf(YangBinaryExpr.class, extractFirstPredicate(step));
        assertEquals(YangBinaryOperator.EQUALS, predicate.getOperator());
        assertEquals(expectedLeft, predicate.getLeftExpr());
        assertEquals(expectedRight, predicate.getRightExpr());
    }

    @ParameterizedTest(name = "Unrecognized prefix: {0}")
    @ValueSource(strings = {"/x:foo", "/foo:foo[x:bar = 'a']", "/foo[x:bar = 'b']"})
    void unrecognizedPrefix(final String input) {
        assertThrows(IllegalArgumentException.class, () -> parseQualified(input));
    }

    @ParameterizedTest(name = "Literals with escaped quotes: {0}")
    @MethodSource("escapedQuotesLiteralArgs")
    void escapedQuotesLiteral(final String input, final String expectedText) throws Exception {

        // expected 1 step with predicate having literal
        final var step = extractFirstStep(parseQualified(input));
        final var predicate = assertInstanceOf(YangBinaryExpr.class, extractFirstPredicate(step));
        final var right = assertInstanceOf(YangLiteralExpr.class, predicate.getRightExpr());

        // ensure the string literal value unquoted properly
        assertEquals(expectedText, right.getLiteral());
    }

    private static Stream<Arguments> escapedQuotesLiteralArgs() {
        return Stream.of(
                Arguments.of("/foo[.= \"quo\\\"tes\\\\\"]", "quo\"tes\\"),
                Arguments.of("/foo[.= \"quo\\ntes\\t\"]", "quo\ntes\t"),
                Arguments.of("/foo[.= 'quo\"tes']", "quo\"tes"),
                Arguments.of("/foo[.= 'quo\\tes']", "quo\\tes"),
                Arguments.of("/foo[.= \"\"]", ""),
                Arguments.of("/foo[.= '']", ""),
                Arguments.of("/foo[.= \"\"]", "")
        );
    }

    private static Absolute parseQualified(final String literal) throws XPathExpressionException {
        return QUALIFIED.interpretAsInstanceIdentifier(YangLiteralExpr.of(literal));
    }

    private static Absolute parseBase(final String literal) throws XPathExpressionException {
        return BASE.interpretAsInstanceIdentifier(YangLiteralExpr.of(literal));
    }

    private static YangExpr toNumberExpr(final int number) {
        return YangXPathMathMode.IEEE754.getSupport().createNumber(number);
    }

    private static Step extractFirstStep(final Absolute parsed) {
        assertNotNull(parsed);
        assertNotNull(parsed.getSteps());
        assertFalse(parsed.getSteps().isEmpty());
        final var step = parsed.getSteps().get(0);
        assertNotNull(step);
        return step;
    }

    private static YangExpr extractFirstPredicate(final Step step) {
        assertNotNull(step.getPredicates());
        assertFalse(step.getPredicates().isEmpty());
        final var predicate = step.getPredicates().iterator().next();
        assertNotNull(predicate);
        return predicate;
    }
}
