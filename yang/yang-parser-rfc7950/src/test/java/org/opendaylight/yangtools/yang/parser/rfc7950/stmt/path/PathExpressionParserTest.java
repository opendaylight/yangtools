/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.path;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.yangtools.yang.common.UnqualifiedQName;
import org.opendaylight.yangtools.yang.model.api.PathExpression;
import org.opendaylight.yangtools.yang.model.api.PathExpression.DerefSteps;
import org.opendaylight.yangtools.yang.model.api.PathExpression.LocationPathSteps;
import org.opendaylight.yangtools.yang.model.api.PathExpression.Steps;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.opendaylight.yangtools.yang.xpath.api.YangBinaryOperator;
import org.opendaylight.yangtools.yang.xpath.api.YangFunction;
import org.opendaylight.yangtools.yang.xpath.api.YangFunctionCallExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.Relative;
import org.opendaylight.yangtools.yang.xpath.api.YangPathExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangQNameExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathAxis;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class PathExpressionParserTest {
    @Mock
    private StmtContext<?, ?, ?> ctx;
    @Mock
    private StatementSourceReference ref;
    private final PathExpressionParser parser = new PathExpressionParser();

    @Before
    public void before() {
        doReturn(ref).when(ctx).getStatementSourceReference();
    }

    @Test
    public void testDerefPath() {
        // deref() is not valid as per RFC7950, but we tolarate it.
        final PathExpression deref = parser.parseExpression(ctx, "deref(../id)/../type");

        final Steps steps = deref.getSteps();
        assertThat(steps, isA(DerefSteps.class));

        final DerefSteps derefSteps = (DerefSteps) steps;
        assertEquals(YangLocationPath.relative(YangXPathAxis.PARENT.asStep(),
            YangXPathAxis.CHILD.asStep(UnqualifiedQName.of("type"))), derefSteps.getRelativePath());
        assertEquals(YangLocationPath.relative(YangXPathAxis.PARENT.asStep(),
            YangXPathAxis.CHILD.asStep(UnqualifiedQName.of("id"))), derefSteps.getDerefArgument());
    }

    @Test
    public void testInvalidLeftParent() {
        try {
            parser.parseExpression(ctx, "foo(");
            fail("SourceException should have been thrown");
        } catch (SourceException e) {
            assertSame(ref, e.getSourceReference());
            assertThat(e.getMessage(), startsWith("extraneous input '(' expecting "));
            assertThat(e.getMessage(), containsString(" at 1:3 [at "));
        }
    }

    @Test
    public void testInvalidRightParent() {
        try {
            parser.parseExpression(ctx, "foo)");
            fail("SourceException should have been thrown");
        } catch (SourceException e) {
            assertSame(ref, e.getSourceReference());
            assertThat(e.getMessage(), startsWith("extraneous input ')' expecting "));
            assertThat(e.getMessage(), containsString(" at 1:3 [at "));
        }
    }

    @Test
    public void testInvalidIdentifier() {
        try {
            parser.parseExpression(ctx, "foo%");
            fail("SourceException should have been thrown");
        } catch (SourceException e) {
            assertSame(ref, e.getSourceReference());
            assertThat(e.getMessage(), startsWith("token recognition error at: '%' at 1:3 [at "));
        }
    }

    @Test
    public void testCurrentPredicateParsing() {
        final YangLocationPath path = ((LocationPathSteps) parser.parseExpression(ctx,
            "/device_types/device_type[type = current()/../type_text]/desc").getSteps()).getLocationPath();
        assertTrue(path.isAbsolute());

        path.getSteps();
        assertEquals(ImmutableList.of(
            YangXPathAxis.CHILD.asStep(UnqualifiedQName.of("device_types")),
            YangXPathAxis.CHILD.asStep(UnqualifiedQName.of("device_type"),
                ImmutableSet.of(YangBinaryOperator.EQUALS.exprWith(
                    YangQNameExpr.of(UnqualifiedQName.of("type")),
                    YangPathExpr.of(YangFunctionCallExpr.of(YangFunction.CURRENT.getIdentifier()), Relative.relative(
                        YangXPathAxis.PARENT.asStep(),
                        YangXPathAxis.CHILD.asStep(UnqualifiedQName.of("type_text"))))))),
            YangXPathAxis.CHILD.asStep(UnqualifiedQName.of("desc"))), path.getSteps());
    }
}
