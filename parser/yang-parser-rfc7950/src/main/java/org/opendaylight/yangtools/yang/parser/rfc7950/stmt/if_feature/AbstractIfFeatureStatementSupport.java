/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.if_feature;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureExpr;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractIfFeatureStatementSupport
        extends AbstractStatementSupport<IfFeatureExpr, IfFeatureStatement, IfFeatureEffectiveStatement> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractIfFeatureStatementSupport.class);
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
        SubstatementValidator.builder(YangStmtMapping.IF_FEATURE).build();

    AbstractIfFeatureStatementSupport(final YangParserConfiguration config) {
        super(YangStmtMapping.IF_FEATURE, StatementPolicy.contextIndependent(), config, SUBSTATEMENT_VALIDATOR);
    }

    @Override
    public final void onFullDefinitionDeclared(
            final Mutable<IfFeatureExpr, IfFeatureStatement, IfFeatureEffectiveStatement> stmt) {
        super.onFullDefinitionDeclared(stmt);

        final var verifyFeatures = stmt.newInferenceAction(ModelProcessingPhase.EFFECTIVE_MODEL);
        final var backRef = new HashMap<Prerequisite<?>, QName>();
        for (var feature : stmt.getArgument().getReferencedFeatures()) {
            backRef.put(verifyFeatures.requiresCtx(stmt, ParserNamespaces.FEATURE, feature,
                ModelProcessingPhase.EFFECTIVE_MODEL), feature);
        }

        verifyFeatures.apply(new InferenceAction() {
            @Override
            public void apply(final InferenceContext ctx) {
                LOG.debug("Resolved all feature references in {}", backRef.values());
            }

            @Override
            public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed) {
                final var unresolvedFeatures = new HashSet<QName>();
                for (var prereq : failed) {
                    unresolvedFeatures.add(verifyNotNull(backRef.get(prereq)));
                }

                throw stmt.newInferenceException("Failed to resolve feature references %s in \"%s\"",
                    unresolvedFeatures, stmt.rawArgument());
            }
        });
    }

    @Override
    protected final IfFeatureStatement createDeclared(final BoundStmtCtx<IfFeatureExpr> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createIfFeature(ctx.getRawArgument(), ctx.getArgument(), substatements);
    }

    @Override
    protected final IfFeatureStatement attachDeclarationReference(final IfFeatureStatement stmt,
            final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateIfFeature(stmt, reference);
    }

    @Override
    protected final IfFeatureEffectiveStatement createEffective(final Current<IfFeatureExpr, IfFeatureStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return EffectiveStatements.createIfFeature(stmt.declared(), substatements);
    }
}
