/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6020.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.PathExpression;
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
class PathExpressionParserTest {
    @Mock
    public StmtContext<?, ?, ?> ctx;
    @Mock
    public StatementSourceReference ref;

    @BeforeEach
    void before() {
        doReturn(ref).when(ctx).sourceReference();
    }

    @Test
    void testDerefPath() {
        // deref() is not valid as per RFC7950, but we tolarate it.
        final var deref = assertInstanceOf(PathExpression.Deref.class,
            PathArgumentParser.parseExpression(ctx, "deref(../id)/../type"));

        assertEquals(YangLocationPath.relative(YangXPathAxis.PARENT.asStep(),
            YangXPathAxis.CHILD.asStep(Unqualified.of("type"))), deref.relativePath());
        assertEquals(YangLocationPath.relative(YangXPathAxis.PARENT.asStep(),
            YangXPathAxis.CHILD.asStep(Unqualified.of("id"))), deref.derefArgument());
    }

    @Test
    void testInvalidLeftParent() {
        final var ex = assertThrows(SourceException.class, () -> PathArgumentParser.parseExpression(ctx, "foo("));
        assertSame(ref, ex.sourceRef());
        assertEquals("extraneous input '(' expecting <EOF> at 1:3 [at ref]", ex.getMessage());
    }

    @Test
    void testInvalidRightParent() {
        final var ex = assertThrows(SourceException.class, () -> PathArgumentParser.parseExpression(ctx, "foo)"));
        assertSame(ref, ex.sourceRef());
        assertEquals("extraneous input ')' expecting <EOF> at 1:3 [at ref]", ex.getMessage());
    }

    @Test
    void testInvalidIdentifier() {
        final var ex = assertThrows(SourceException.class, () -> PathArgumentParser.parseExpression(ctx, "foo%"));
        assertSame(ref, ex.sourceRef());
        assertEquals("token recognition error at: '%' at 1:3 [at ref]", ex.getMessage());
    }

    @Test
    void testCurrentPredicateParsing() {
        final var path = assertInstanceOf(PathExpression.LocationPath.class,
            PathArgumentParser.parseExpression(ctx, "/device_types/device_type[type = current()/../type_text]/desc"))
            .locationPath();
        assertTrue(path.isAbsolute());

        assertEquals(List.of(
            YangXPathAxis.CHILD.asStep(Unqualified.of("device_types")),
            YangXPathAxis.CHILD.asStep(Unqualified.of("device_type"),
                Set.of(YangBinaryOperator.EQUALS.exprWith(
                    YangQNameExpr.of(Unqualified.of("type")),
                    YangPathExpr.of(YangFunctionCallExpr.of(YangFunction.CURRENT.getIdentifier()), Relative.relative(
                        YangXPathAxis.PARENT.asStep(),
                        YangXPathAxis.CHILD.asStep(Unqualified.of("type_text"))))))),
            YangXPathAxis.CHILD.asStep(Unqualified.of("desc"))), path.getSteps());
    }
}
