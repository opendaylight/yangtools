/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.if_feature;

import static com.google.common.base.Verify.verify;

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

@NonNullByDefault
final class IfFeaturePredicateVisitor {
    private IfFeaturePredicateVisitor() {
    }

    static IfFeatureExpr parseIfFeatureExpression(final StmtContext<?, ?, ?> ctx, final String value) {
        final IfFeatureExpressionLexer lexer = new IfFeatureExpressionLexer(CharStreams.fromString(value));
        final IfFeatureExpressionParser parser = new IfFeatureExpressionParser(new CommonTokenStream(lexer));
        final If_feature_exprContext ifFeatureExprContext =
                SourceExceptionParser.parse(lexer, parser, parser::if_feature_expr, ctx.sourceReference());
        return parseIfFeatureExpr(ifFeatureExprContext, ctx);
    }

    private static IfFeatureExpr parseIfFeatureExpr(final If_feature_exprContext expr, final StmtContext<?, ?, ?> ctx) {
        final Set<IfFeatureExpr> expressions = new HashSet<>();
        for (int i = 0; i < expr.getChildCount(); i += 4) {
            expressions.add(parseIfFeatureTerm(getChild(expr, i, If_feature_termContext.class), ctx));
        }
        return IfFeatureExpr.or(expressions);
    }

    private static IfFeatureExpr parseIfFeatureTerm(final If_feature_termContext term, final StmtContext<?, ?, ?> ctx) {
        final Set<IfFeatureExpr> expressions = new HashSet<>();
        expressions.add(parseIfFeatureFactor(getChild(term, 0, If_feature_factorContext.class), ctx));
        for (int i = 4; i < term.getChildCount(); i += 4) {
            expressions.add(parseIfFeatureTerm(getChild(term, i, If_feature_termContext.class), ctx));
        }
        return IfFeatureExpr.and(expressions);
    }

    private static IfFeatureExpr parseIfFeatureFactor(final If_feature_factorContext factor,
            final StmtContext<?, ?, ?> ctx) {
        if (factor.getChild(0) instanceof Identifier_ref_argContext refArg) {
            return parseIdentifierRefArg(refArg, ctx);
        } else if (factor.getChild(1) instanceof If_feature_exprContext expr) {
            return parseIfFeatureExpr(expr, ctx);
        } else if (factor.getChild(2) instanceof If_feature_exprContext expr) {
            return parseIfFeatureExpr(expr, ctx);
        } else if (factor.getChild(2) instanceof If_feature_factorContext childFactor) {
            return parseIfFeatureFactor(childFactor, ctx).negate();
        }

        throw new SourceException("Unexpected grammar context during parsing of IfFeature expression. "
                + "Most probably IfFeature grammar has been changed.", ctx);
    }

    private static IfFeatureExpr parseIdentifierRefArg(final Identifier_ref_argContext refArg,
            final StmtContext<?, ?, ?> ctx) {
        return IfFeatureExpr.isPresent(StmtContextUtils.parseNodeIdentifier(ctx, refArg.getText()));
    }

    private static <T> T getChild(final ParseTree parent, final int offset, final Class<T> clazz) {
        final var child = parent.getChild(offset);
        verify(clazz.isInstance(child), "Unexpected child %s at offset %s of %s when expecting %s", child, offset,
                parent, clazz);
        return clazz.cast(child);
    }
}
