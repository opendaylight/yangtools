/*
 * Copyright (c) 2017, 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.antlr;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableSet;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureExpr;
import org.opendaylight.yangtools.yang.parser.grammar.IfFeatureExpressionLexer;
import org.opendaylight.yangtools.yang.parser.grammar.IfFeatureExpressionParser;
import org.opendaylight.yangtools.yang.parser.grammar.IfFeatureExpressionParser.Identifier_ref_argContext;
import org.opendaylight.yangtools.yang.parser.grammar.IfFeatureExpressionParser.If_feature_exprContext;
import org.opendaylight.yangtools.yang.parser.grammar.IfFeatureExpressionParser.If_feature_factorContext;
import org.opendaylight.yangtools.yang.parser.grammar.IfFeatureExpressionParser.If_feature_termContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

@NonNullByDefault
final class IfFeaturePredicateParser {
    private final StmtContext<?, ?, ?> stmt;

    private IfFeaturePredicateParser(final StmtContext<?, ?, ?> stmt) {
        this.stmt = requireNonNull(stmt);
    }

    static IfFeatureExpr parseIfFeatureExpression(final StmtContext<?, ?, ?> stmt, final String value) {
        final var expr = SourceExceptionParser.parseString(IfFeatureExpressionLexer::new,
            IfFeatureExpressionParser::new, IfFeatureExpressionParser::if_feature_expr, stmt.sourceReference(), value);
        return new IfFeaturePredicateParser(stmt).parseIfFeatureExpr(expr);
    }

    private IfFeatureExpr parseIfFeatureExpr(final If_feature_exprContext expr) {
        final int count = verifyExprOrTermChildren(expr);
        final var expressions = ImmutableSet.<IfFeatureExpr>builderWithExpectedSize(count / 4 + 1);
        for (int i = 0; i < count; i += 4) {
            expressions.add(parseIfFeatureTerm(getChild(expr, i, If_feature_termContext.class)));
        }
        return IfFeatureExpr.or(expressions.build());
    }

    private IfFeatureExpr parseIfFeatureTerm(final If_feature_termContext term) {
        final int count = verifyExprOrTermChildren(term);
        final var expressions = ImmutableSet.<IfFeatureExpr>builderWithExpectedSize(count / 4 + 1);
        expressions.add(parseIfFeatureFactor(getChild(term, 0, If_feature_factorContext.class)));

        for (int i = 4; i < count; i += 4) {
            expressions.add(parseIfFeatureTerm(getChild(term, i, If_feature_termContext.class)));
        }
        return IfFeatureExpr.and(expressions.build());
    }

    private IfFeatureExpr parseIfFeatureFactor(final If_feature_factorContext factor) {
        final var first = factor.getChild(0);
        return switch (first) {
            case Identifier_ref_argContext refArg ->
                IfFeatureExpr.isPresent(stmt.identifierBinding().parseIdentifierRefArg(stmt, refArg.getText()));
            case TerminalNode terminal ->
                switch (terminal.getSymbol().getType()) {
                    case IfFeatureExpressionParser.LP ->
                        parseIfFeatureExpr(factor.getChild(If_feature_exprContext.class, 0));
                    case IfFeatureExpressionParser.NOT ->
                        parseIfFeatureFactor(getChild(factor, 2, If_feature_factorContext.class)).negate();
                    default ->
                        throw new SourceException(stmt, "Unexpected terminal %s in sub-expression at %s",
                            terminal.getText(), factor.getSourceInterval());
                };
            default -> throw new SourceException(stmt, """
                Unexpected error: sub-expression at %s has context %s. Please file a bug report with the corresponding \
                model attached.""", factor.getSourceInterval(), first);
        };
    }

    private int verifyExprOrTermChildren(final ParserRuleContext context) {
        final int count = context.getChildCount();
        if (count % 4 != 1) {
            throw new SourceException(stmt, """
                Unexpected error: sub-expression at %s has %s children. Please file a bug report with the \
                corresponding model attached.""", context.getSourceInterval(), count);
        }
        return count;
    }

    private <T> T getChild(final ParserRuleContext parent, final int offset, final Class<T> clazz) {
        final var child = parent.getChild(offset);
        if (!clazz.isInstance(child)) {
            throw new SourceException(stmt, """
                Unexpected error: sub-expression at %s has child %s at offset %s when expecting %s. Please file a bug \
                report with the corresponding model attached.""", parent.getSourceInterval(), child, offset, clazz);
        }
        return clazz.cast(child);
    }
}
