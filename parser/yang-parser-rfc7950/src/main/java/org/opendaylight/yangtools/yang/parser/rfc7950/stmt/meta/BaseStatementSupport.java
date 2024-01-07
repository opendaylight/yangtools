/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class BaseStatementSupport extends AbstractQNameStatementSupport<BaseStatement, BaseEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
            SubstatementValidator.builder(YangStmtMapping.BASE).build();

    public BaseStatementSupport(final YangParserConfiguration config) {
        super(YangStmtMapping.BASE, StatementPolicy.contextIndependent(), config, SUBSTATEMENT_VALIDATOR);
    }

    @Override
    public QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return StmtContextUtils.parseNodeIdentifier(ctx, value);
    }

    @Override
    public void onStatementDefinitionDeclared(final Mutable<QName, BaseStatement, BaseEffectiveStatement> baseStmtCtx) {
        final Mutable<?, ?, ?> baseParentCtx = baseStmtCtx.coerceParentContext();
        if (baseParentCtx.producesDeclared(IdentityStatement.class)) {
            final QName baseIdentityQName = baseStmtCtx.getArgument();
            final ModelActionBuilder baseIdentityAction = baseStmtCtx.newInferenceAction(
                ModelProcessingPhase.STATEMENT_DEFINITION);
            baseIdentityAction.requiresCtx(baseStmtCtx, ParserNamespaces.IDENTITY, baseIdentityQName,
                ModelProcessingPhase.STATEMENT_DEFINITION);
            baseIdentityAction.mutatesCtx(baseParentCtx, ModelProcessingPhase.STATEMENT_DEFINITION);

            baseIdentityAction.apply(new InferenceAction() {
                @Override
                public void apply(final InferenceContext ctx) {
                    // No-op, we just want to ensure the statement is specified
                }

                @Override
                public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed) {
                    throw baseStmtCtx.newInferenceException("Unable to resolve identity %s and base identity %s",
                        baseParentCtx.argument(), baseStmtCtx.argument());
                }
            });
        }
    }

    @Override
    protected BaseStatement createDeclared(final BoundStmtCtx<QName> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createBase(ctx.getArgument(), substatements);
    }

    @Override
    protected BaseStatement attachDeclarationReference(final BaseStatement stmt, final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateBase(stmt, reference);
    }

    @Override
    protected BaseEffectiveStatement createEffective(final Current<QName, BaseStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return EffectiveStatements.createBase(stmt.declared(), substatements);
    }
}
