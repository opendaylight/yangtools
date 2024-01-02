/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.path;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.PathExpression;
import org.opendaylight.yangtools.yang.model.api.PathExpression.DerefSteps;
import org.opendaylight.yangtools.yang.model.api.PathExpression.LocationPathSteps;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.xpath.api.YangBinaryOperator;
import org.opendaylight.yangtools.yang.xpath.api.YangFunction;
import org.opendaylight.yangtools.yang.xpath.api.YangFunctionCallExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.Relative;
import org.opendaylight.yangtools.yang.xpath.api.YangPathExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangQNameExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathAxis;

@ExtendWith(MockitoExtension.class)
public class PathExpressionParserTest {
    @Mock
    public StmtContext<?, ?, ?> ctx;
    @Mock
    public StatementSourceReference ref;

    @SuppressWarnings("exports")
    public final PathExpressionParser parser = new PathExpressionParser();

    @BeforeEach
    void before() {
        doReturn(ref).when(ctx).sourceReference();
    }

    @Test
    void testDerefPath() {
        // deref() is not valid as per RFC7950, but we tolarate it.
        final PathExpression deref = parser.parseExpression(ctx, "deref(../id)/../type");

        final DerefSteps derefSteps = assertInstanceOf(DerefSteps.class, deref.getSteps());
        assertEquals(YangLocationPath.relative(YangXPathAxis.PARENT.asStep(),
            YangXPathAxis.CHILD.asStep(Unqualified.of("type"))), derefSteps.getRelativePath());
        assertEquals(YangLocationPath.relative(YangXPathAxis.PARENT.asStep(),
            YangXPathAxis.CHILD.asStep(Unqualified.of("id"))), derefSteps.getDerefArgument());
    }

    @Test
    void testInvalidLeftParent() {
        final var ex = assertThrows(SourceException.class, () -> parser.parseExpression(ctx, "foo("));
        assertSame(ref, ex.getSourceReference());
        assertThat(ex.getMessage(), allOf(
            startsWith("extraneous input '(' expecting "),
            containsString(" at 1:3 [at ")));
    }

    @Test
    void testInvalidRightParent() {
        final var ex = assertThrows(SourceException.class, () -> parser.parseExpression(ctx, "foo)"));
        assertSame(ref, ex.getSourceReference());
        assertThat(ex.getMessage(), allOf(
            startsWith("extraneous input ')' expecting "),
            containsString(" at 1:3 [at ")));
    }

    @Test
    void testInvalidIdentifier() {
        final var ex = assertThrows(SourceException.class, () -> parser.parseExpression(ctx, "foo%"));
        assertSame(ref, ex.getSourceReference());
        assertThat(ex.getMessage(), startsWith("token recognition error at: '%' at 1:3 [at "));
    }

    @Test
    void testCurrentPredicateParsing() {
        final YangLocationPath path = ((LocationPathSteps) parser.parseExpression(ctx,
            "/device_types/device_type[type = current()/../type_text]/desc").getSteps()).getLocationPath();
        assertTrue(path.isAbsolute());

        path.getSteps();
        assertEquals(ImmutableList.of(
            YangXPathAxis.CHILD.asStep(Unqualified.of("device_types")),
            YangXPathAxis.CHILD.asStep(Unqualified.of("device_type"),
                ImmutableSet.of(YangBinaryOperator.EQUALS.exprWith(
                    YangQNameExpr.of(Unqualified.of("type")),
                    YangPathExpr.of(YangFunctionCallExpr.of(YangFunction.CURRENT.getIdentifier()), Relative.relative(
                        YangXPathAxis.PARENT.asStep(),
                        YangXPathAxis.CHILD.asStep(Unqualified.of("type_text"))))))),
            YangXPathAxis.CHILD.asStep(Unqualified.of("desc"))), path.getSteps());
    }
}
