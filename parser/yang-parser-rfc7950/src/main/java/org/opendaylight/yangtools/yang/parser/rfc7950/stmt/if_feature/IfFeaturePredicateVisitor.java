/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.if_feature;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableSet;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureExpr;
import org.opendaylight.yangtools.yang.parser.antlr.IfFeatureExpressionLexer;
import org.opendaylight.yangtools.yang.parser.antlr.IfFeatureExpressionParser;
import org.opendaylight.yangtools.yang.parser.antlr.IfFeatureExpressionParser.Identifier_ref_argContext;
import org.opendaylight.yangtools.yang.parser.antlr.IfFeatureExpressionParser.If_feature_exprContext;
import org.opendaylight.yangtools.yang.parser.antlr.IfFeatureExpressionParser.If_feature_factorContext;
import org.opendaylight.yangtools.yang.parser.antlr.IfFeatureExpressionParser.If_feature_termContext;
import org.opendaylight.yangtools.yang.parser.rfc7950.antlr.SourceExceptionParser;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

@NonNullByDefault
final class IfFeaturePredicateVisitor {
    private IfFeaturePredicateVisitor() {
    }

    static IfFeatureExpr parseIfFeatureExpression(final StmtContext<?, ?, ?> ctx, final String value) {
        final var lexer = new IfFeatureExpressionLexer(CharStreams.fromString(value));
        final var parser = new IfFeatureExpressionParser(new CommonTokenStream(lexer));
        final var ifFeatureExprContext =
                SourceExceptionParser.parse(lexer, parser, parser::if_feature_expr, ctx.sourceReference());
        return parseIfFeatureExpr(ifFeatureExprContext, ctx);
    }

    private static IfFeatureExpr parseIfFeatureExpr(final If_feature_exprContext expr, final StmtContext<?, ?, ?> ctx) {
        final int count = expr.getChildCount();
        verify(count % 4 == 1, "Unexpected number of children %s", count);

        final var expressions = ImmutableSet.<IfFeatureExpr>builderWithExpectedSize(count / 4 + 1);
        for (int i = 0; i < count; i += 4) {
            expressions.add(parseIfFeatureTerm(getChild(expr, i, If_feature_termContext.class), ctx));
        }
        return IfFeatureExpr.or(expressions.build());
    }

    private static IfFeatureExpr parseIfFeatureTerm(final If_feature_termContext term, final StmtContext<?, ?, ?> ctx) {
        final int count = term.getChildCount();
        verify(count % 4 == 1, "Unexpected number of children %s", count);

        final var expressions = ImmutableSet.<IfFeatureExpr>builderWithExpectedSize(count / 4 + 1);
        expressions.add(parseIfFeatureFactor(getChild(term, 0, If_feature_factorContext.class), ctx));

        for (int i = 4; i < count; i += 4) {
            expressions.add(parseIfFeatureTerm(getChild(term, i, If_feature_termContext.class), ctx));
        }
        return IfFeatureExpr.and(expressions.build());
    }

    private static IfFeatureExpr parseIfFeatureFactor(final If_feature_factorContext factor,
            final StmtContext<?, ?, ?> ctx) {
        final var first = factor.getChild(0);
        if (first instanceof Identifier_ref_argContext refArg) {
            return IfFeatureExpr.isPresent(StmtContextUtils.parseNodeIdentifier(ctx, refArg.getText()));
        } else if (first instanceof TerminalNode terminal) {
            return switch (terminal.getSymbol().getType()) {
                case IfFeatureExpressionParser.LP ->
                    parseIfFeatureExpr(verifyNotNull(factor.getChild(If_feature_exprContext.class, 0)), ctx);
                case IfFeatureExpressionParser.NOT ->
                    parseIfFeatureFactor(getChild(factor, 2, If_feature_factorContext.class), ctx).negate();
                default -> throw new VerifyException("Unexpected terminal " + terminal.getText());
            };
        } else {
            throw new SourceException("Unexpected grammar context during parsing of IfFeature expression. "
                + "Most probably IfFeature grammar has been changed.", ctx);
        }
    }

    private static <T> T getChild(final ParseTree parent, final int offset, final Class<T> clazz) {
        final var child = parent.getChild(offset);
        verify(clazz.isInstance(child), "Unexpected child %s at offset %s of %s when expecting %s", child, offset,
                parent, clazz);
        return clazz.cast(child);
    }
}
