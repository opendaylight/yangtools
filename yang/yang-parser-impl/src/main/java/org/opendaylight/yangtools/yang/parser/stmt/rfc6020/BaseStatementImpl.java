/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityStatement;
import org.opendaylight.yangtools.yang.parser.spi.IdentityNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.DerivedIdentitiesNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.BaseEffectiveStatementImpl;

public class BaseStatementImpl extends AbstractDeclaredStatement<QName> implements BaseStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
            SubstatementValidator.builder(YangStmtMapping.BASE).build();

    protected BaseStatementImpl(final StmtContext<QName, BaseStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends AbstractStatementSupport<QName, BaseStatement, EffectiveStatement<QName, BaseStatement>> {

        public Definition() {
            super(YangStmtMapping.BASE);
        }

        @Override
        public QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            return StmtContextUtils.qnameFromArgument(ctx, value);
        }

        @Override
        public BaseStatement createDeclared(final StmtContext<QName, BaseStatement, ?> ctx) {
            return new BaseStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<QName, BaseStatement> createEffective(
                final StmtContext<QName, BaseStatement, EffectiveStatement<QName, BaseStatement>> ctx) {
            return new BaseEffectiveStatementImpl(ctx);
        }

        @Override
        public void onStatementDefinitionDeclared(
                final Mutable<QName, BaseStatement, EffectiveStatement<QName, BaseStatement>> baseStmtCtx) {
            final Mutable<?, ?, ?> baseParentCtx = baseStmtCtx.getParentContext();
            if (StmtContextUtils.producesDeclared(baseParentCtx, IdentityStatement.class)) {

                final QName baseIdentityQName = baseStmtCtx.getStatementArgument();
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
                            DerivedIdentitiesNamespace.class, baseStmtCtx.getStatementArgument());
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
    }

    @Nonnull
    @Override
    public QName getName() {
        return argument();
    }
}
