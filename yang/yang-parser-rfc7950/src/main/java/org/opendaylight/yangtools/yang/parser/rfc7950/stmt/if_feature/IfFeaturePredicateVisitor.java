/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.if_feature;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.antlrv4.code.gen.IfFeatureExpressionLexer;
import org.opendaylight.yangtools.antlrv4.code.gen.IfFeatureExpressionParser;
import org.opendaylight.yangtools.antlrv4.code.gen.IfFeatureExpressionParser.Identifier_ref_argContext;
import org.opendaylight.yangtools.antlrv4.code.gen.IfFeatureExpressionParser.If_feature_exprContext;
import org.opendaylight.yangtools.antlrv4.code.gen.IfFeatureExpressionParser.If_feature_factorContext;
import org.opendaylight.yangtools.antlrv4.code.gen.IfFeatureExpressionParser.If_feature_termContext;
import org.opendaylight.yangtools.antlrv4.code.gen.IfFeatureExpressionParserBaseVisitor;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureExpr;
import org.opendaylight.yangtools.yang.parser.rfc7950.antlr.SourceExceptionParser;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

@NonNullByDefault
final class IfFeaturePredicateVisitor extends IfFeatureExpressionParserBaseVisitor<IfFeatureExpr> {
    private final StmtContext<?, ?, ?> stmtCtx;

    private IfFeaturePredicateVisitor(final StmtContext<?, ?, ?> ctx) {
        this.stmtCtx = requireNonNull(ctx);
    }

    static IfFeatureExpr parseIfFeatureExpression(final StmtContext<?, ?, ?> ctx, final String value) {
        final IfFeatureExpressionLexer lexer = new IfFeatureExpressionLexer(CharStreams.fromString(value));
        final IfFeatureExpressionParser parser = new IfFeatureExpressionParser(new CommonTokenStream(lexer));
        final IfFeatureExpr ret = new IfFeaturePredicateVisitor(ctx).visit(SourceExceptionParser.parse(lexer, parser,
            parser::if_feature_expr, ctx.getStatementSourceReference()));

        return ret;
    }

    @Override
    public IfFeatureExpr visitIf_feature_expr(final @Nullable If_feature_exprContext ctx) {
        return IfFeatureExpr.or(ctx.if_feature_term().stream()
            .map(this::visitIf_feature_term)
            .collect(ImmutableSet.toImmutableSet()));
    }

    @Override
    public IfFeatureExpr visitIf_feature_term(final @Nullable If_feature_termContext ctx) {
        final IfFeatureExpr factor = visitIf_feature_factor(ctx.if_feature_factor());
        final List<If_feature_termContext> terms = ctx.if_feature_term();
        if (terms == null || terms.isEmpty()) {
            return factor;
        }
        final List<IfFeatureExpr> factors = new ArrayList<>(terms.size() + 1);
        factors.add(factor);
        for (If_feature_termContext term : terms) {
            factors.add(visitIf_feature_term(term));
        }
        return IfFeatureExpr.and(ImmutableSet.copyOf(factors));
    }

    @Override
    public IfFeatureExpr visitIf_feature_factor(final @Nullable If_feature_factorContext ctx) {
        if (ctx.if_feature_expr() != null) {
            return visitIf_feature_expr(ctx.if_feature_expr());
        } else if (ctx.if_feature_factor() != null) {
            return visitIf_feature_factor(ctx.if_feature_factor()).negate();
        } else if (ctx.identifier_ref_arg() != null) {
            return visitIdentifier_ref_arg(ctx.identifier_ref_arg());
        }

        throw new SourceException("Unexpected grammar context during parsing of IfFeature expression. "
                + "Most probably IfFeature grammar has been changed.", stmtCtx.getStatementSourceReference());
    }

    @Override
    public IfFeatureExpr visitIdentifier_ref_arg(final @Nullable Identifier_ref_argContext ctx) {
        return IfFeatureExpr.isPresent(StmtContextUtils.parseNodeIdentifier(stmtCtx, ctx.getText()));
    }
}