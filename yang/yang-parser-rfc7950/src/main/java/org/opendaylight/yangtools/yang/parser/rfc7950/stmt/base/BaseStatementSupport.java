/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.base;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.IdentityNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.DerivedIdentitiesNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class BaseStatementSupport extends BaseQNameStatementSupport<BaseStatement, BaseEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
            SubstatementValidator.builder(YangStmtMapping.BASE).build();
    private static final BaseStatementSupport INSTANCE = new BaseStatementSupport();

    private BaseStatementSupport() {
        super(YangStmtMapping.BASE);
    }

    public static BaseStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return StmtContextUtils.parseNodeIdentifier(ctx, value);
    }

    @Override
    public void onStatementDefinitionDeclared(final Mutable<QName, BaseStatement, BaseEffectiveStatement> baseStmtCtx) {
        final Mutable<?, ?, ?> baseParentCtx = baseStmtCtx.getParentContext();
        if (baseParentCtx.producesDeclared(IdentityStatement.class)) {

            final QName baseIdentityQName = baseStmtCtx.coerceStatementArgument();
            final ModelActionBuilder baseIdentityAction = baseStmtCtx.newInferenceAction(
                ModelProcessingPhase.STATEMENT_DEFINITION);
            final Prerequisite<StmtContext<?, ?, ?>> requiresPrereq = baseIdentityAction.requiresCtx(baseStmtCtx,
                IdentityNamespace.class, baseIdentityQName, ModelProcessingPhase.STATEMENT_DEFINITION);
            final Prerequisite<StmtContext.Mutable<?, ?, ?>> mutatesPrereq = baseIdentityAction.mutatesCtx(
                baseParentCtx, ModelProcessingPhase.STATEMENT_DEFINITION);

            baseIdentityAction.apply(new InferenceAction() {
                @Override
                public void apply(final InferenceContext ctx) {
                    List<StmtContext<?, ?, ?>> derivedIdentities = baseStmtCtx.getFromNamespace(
                        DerivedIdentitiesNamespace.class, baseIdentityQName);
                    if (derivedIdentities == null) {
                        derivedIdentities = new ArrayList<>(1);
                        baseStmtCtx.addToNs(DerivedIdentitiesNamespace.class, baseIdentityQName, derivedIdentities);
                    }
                    derivedIdentities.add(baseParentCtx);
                }

                @Override
                public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed) {
                    throw new InferenceException(baseStmtCtx.getStatementSourceReference(),
                        "Unable to resolve identity %s and base identity %s",
                        baseParentCtx.getStatementArgument(), baseStmtCtx.getStatementArgument());
                }
            });
        }
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected BaseStatement createDeclared(final StmtContext<QName, BaseStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularBaseStatement(ctx.coerceStatementArgument(), substatements);
    }

    @Override
    protected BaseStatement createEmptyDeclared(final StmtContext<QName, BaseStatement, ?> ctx) {
        return new EmptyBaseStatement(ctx.coerceStatementArgument());
    }

    @Override
    protected BaseEffectiveStatement createEffective(
            final StmtContext<QName, BaseStatement, BaseEffectiveStatement> ctx, final BaseStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new RegularBaseEffectiveStatement(declared, substatements);
    }

    @Override
    protected BaseEffectiveStatement createEmptyEffective(
            final StmtContext<QName, BaseStatement, BaseEffectiveStatement> ctx, final BaseStatement declared) {
        return new EmptyBaseEffectiveStatement(declared);
    }
}
