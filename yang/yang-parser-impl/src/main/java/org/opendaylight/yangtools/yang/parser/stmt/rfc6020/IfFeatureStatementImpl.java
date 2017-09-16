/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import com.google.common.base.Preconditions;
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
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.IfFeatureEffectiveStatementImpl;

public class IfFeatureStatementImpl extends AbstractDeclaredStatement<Predicate<Set<QName>>>
        implements IfFeatureStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
            .IF_FEATURE)
            .build();

    protected IfFeatureStatementImpl(final StmtContext<Predicate<Set<QName>>, IfFeatureStatement, ?> context) {
        super(context);
    }

    public static class Definition extends AbstractStatementSupport<Predicate<Set<QName>>, IfFeatureStatement,
            EffectiveStatement<Predicate<Set<QName>>, IfFeatureStatement>> {

        public Definition() {
            super(YangStmtMapping.IF_FEATURE);
        }

        @Override
        public Predicate<Set<QName>> parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            if (YangVersion.VERSION_1_1.equals(ctx.getRootVersion())) {
                return parseIfFeatureExpression(ctx, value);
            }

            final QName qname = StmtContextUtils.qnameFromArgument(ctx, value);
            return setQNames -> setQNames.contains(qname);
        }

        @Override
        public IfFeatureStatement createDeclared(final StmtContext<Predicate<Set<QName>>, IfFeatureStatement, ?> ctx) {
            return new IfFeatureStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<Predicate<Set<QName>>, IfFeatureStatement> createEffective(
                final StmtContext<Predicate<Set<QName>>, IfFeatureStatement,
                EffectiveStatement<Predicate<Set<QName>>, IfFeatureStatement>> ctx) {
            return new IfFeatureEffectiveStatementImpl(ctx);
        }

        @Override
        protected SubstatementValidator getSubstatementValidator() {
            return SUBSTATEMENT_VALIDATOR;
        }

        private static Predicate<Set<QName>> parseIfFeatureExpression(final StmtContext<?, ?, ?> ctx,
                final String value) {
            final IfFeatureExpressionLexer lexer = new IfFeatureExpressionLexer(CharStreams.fromString(value));
            final CommonTokenStream tokens = new CommonTokenStream(lexer);
            final IfFeatureExpressionParser parser = new IfFeatureExpressionParser(tokens);

            return new IfFeaturePredicateVisitor(ctx).visit(parser.if_feature_expr());
        }

        private static class IfFeaturePredicateVisitor
                extends IfFeatureExpressionParserBaseVisitor<Predicate<Set<QName>>> {
            private final StmtContext<?, ?, ?> stmtCtx;

            IfFeaturePredicateVisitor(final StmtContext<?, ?, ?> ctx) {
                this.stmtCtx = Preconditions.checkNotNull(ctx);
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
    }

    @Override
    public Predicate<Set<QName>> getIfFeaturePredicate() {
        return argument();
    }
}
