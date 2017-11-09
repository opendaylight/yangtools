/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.if_feature;

import static java.util.Objects.requireNonNull;

import java.util.Set;
import java.util.function.Predicate;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.opendaylight.yangtools.antlrv4.code.gen.IfFeatureExpressionLexer;
import org.opendaylight.yangtools.antlrv4.code.gen.IfFeatureExpressionParser;
import org.opendaylight.yangtools.antlrv4.code.gen.IfFeatureExpressionParser.Identifier_ref_argContext;
import org.opendaylight.yangtools.antlrv4.code.gen.IfFeatureExpressionParser.If_feature_exprContext;
import org.opendaylight.yangtools.antlrv4.code.gen.IfFeatureExpressionParser.If_feature_factorContext;
import org.opendaylight.yangtools.antlrv4.code.gen.IfFeatureExpressionParser.If_feature_termContext;
import org.opendaylight.yangtools.antlrv4.code.gen.IfFeatureExpressionParserBaseVisitor;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

final class IfFeaturePredicateVisitor extends IfFeatureExpressionParserBaseVisitor<Predicate<Set<QName>>> {
    private final StmtContext<?, ?, ?> stmtCtx;

    private IfFeaturePredicateVisitor(final StmtContext<?, ?, ?> ctx) {
        this.stmtCtx = requireNonNull(ctx);
    }

    static Predicate<Set<QName>> parseIfFeatureExpression(final StmtContext<?, ?, ?> ctx, final String value) {
        final IfFeatureExpressionLexer lexer = new IfFeatureExpressionLexer(CharStreams.fromString(value));
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final IfFeatureExpressionParser parser = new IfFeatureExpressionParser(tokens);

        return new IfFeaturePredicateVisitor(ctx).visit(parser.if_feature_expr());
    }

    @Override
    public Predicate<Set<QName>> visitIf_feature_expr(final If_feature_exprContext ctx) {
        if (ctx.if_feature_expr() == null) {
            return visitIf_feature_term(ctx.if_feature_term());
        }

        return visitIf_feature_term(ctx.if_feature_term()).or(visitIf_feature_expr(ctx.if_feature_expr()));
    }

    @Override
    public Predicate<Set<QName>> visitIf_feature_term(final If_feature_termContext ctx) {
        if (ctx.if_feature_term() == null) {
            return visitIf_feature_factor(ctx.if_feature_factor());
        }

        return visitIf_feature_factor(ctx.if_feature_factor()).and(visitIf_feature_term(ctx.if_feature_term()));
    }

    @Override
    public Predicate<Set<QName>> visitIf_feature_factor(final If_feature_factorContext ctx) {
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
    public Predicate<Set<QName>> visitIdentifier_ref_arg(final Identifier_ref_argContext ctx) {
        final QName featureQName = StmtContextUtils.qnameFromArgument(stmtCtx, ctx.getText());
        return setQNames -> setQNames.contains(featureQName);
    }
}