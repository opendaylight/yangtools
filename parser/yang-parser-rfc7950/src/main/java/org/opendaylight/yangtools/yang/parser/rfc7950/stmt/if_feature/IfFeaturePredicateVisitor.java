/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.if_feature;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import java.util.HashSet;
import java.util.Set;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
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

// FIXME: YANGTOOLS-1396: refactor on top of vanilla IfFeatureExpressionParser
@NonNullByDefault
final class IfFeaturePredicateVisitor {
    private final StmtContext<?, ?, ?> stmtCtx;

    private IfFeaturePredicateVisitor(final StmtContext<?, ?, ?> ctx) {
        this.stmtCtx = requireNonNull(ctx);
    }

    static IfFeatureExpr parseIfFeatureExpression(final StmtContext<?, ?, ?> ctx, final String value) {
        final IfFeatureExpressionLexer lexer = new IfFeatureExpressionLexer(CharStreams.fromString(value));
        final IfFeatureExpressionParser parser = new IfFeatureExpressionParser(new CommonTokenStream(lexer));
        final If_feature_exprContext ifFeatureExprContext =
                SourceExceptionParser.parse(lexer, parser, parser::if_feature_expr, ctx.sourceReference());
        // TODO: Maybe making whole parsing static and pass StmtContext in arguments would be better
        return new IfFeaturePredicateVisitor(ctx).parseIfFeatureExpr(ifFeatureExprContext);
    }

    private IfFeatureExpr parseIfFeatureExpr(final If_feature_exprContext ctx) {
        final Set<IfFeatureExpr> expressions = new HashSet<>();
        for (int i = 0; i < ctx.getChildCount(); i += 4) {
            expressions.add(parseIfFeatureTerm(getChild(ctx, i, If_feature_termContext.class)));
        }
        return IfFeatureExpr.or(expressions);
    }

    private IfFeatureExpr parseIfFeatureTerm(final If_feature_termContext ctx) {
        final Set<IfFeatureExpr> expressions = new HashSet<>();
        expressions.add(parseIfFeatureFactor(getChild(ctx, 0, If_feature_factorContext.class)));
        for (int i = 4; i < ctx.getChildCount(); i += 4) {
            expressions.add(parseIfFeatureTerm(getChild(ctx, i, If_feature_termContext.class)));
        }
        return IfFeatureExpr.and(expressions);
    }

    private IfFeatureExpr parseIfFeatureFactor(final If_feature_factorContext ctx) {
        if (ctx.getChild(0) instanceof Identifier_ref_argContext) {
            return parseIdentifierRefArg((Identifier_ref_argContext) ctx.getChild(0));
        } else if (ctx.getChild(1) instanceof If_feature_exprContext) {
            return parseIfFeatureExpr((If_feature_exprContext) ctx.getChild(1));
        } else if (ctx.getChild(2) instanceof If_feature_exprContext) {
            return parseIfFeatureExpr((If_feature_exprContext) ctx.getChild(2));
        } else if (ctx.getChild(2) instanceof If_feature_factorContext) {
            return parseIfFeatureFactor((If_feature_factorContext) ctx.getChild(2)).negate();
        }

        throw new SourceException("Unexpected grammar context during parsing of IfFeature expression. "
                + "Most probably IfFeature grammar has been changed.", stmtCtx);
    }

    public IfFeatureExpr parseIdentifierRefArg(final Identifier_ref_argContext ctx) {
        return IfFeatureExpr.isPresent(StmtContextUtils.parseNodeIdentifier(stmtCtx, ctx.getText()));
    }

    private static <T> T getChild(final ParseTree parent, final int offset, final Class<T> clazz) {
        final ParseTree child = parent.getChild(offset);
        verify(clazz.isInstance(child), "Unexpected child %s at offset %s of %s when expecting %s", child, offset,
                parent, clazz);
        return clazz.cast(child);
    }
}
