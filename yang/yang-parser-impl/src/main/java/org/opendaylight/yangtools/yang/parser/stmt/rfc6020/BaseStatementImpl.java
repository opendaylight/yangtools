/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.IdentityNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.DerivedIdentitiesNamespace;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.BaseEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public class BaseStatementImpl extends AbstractDeclaredStatement<QName>
        implements BaseStatement {

    protected BaseStatementImpl(StmtContext<QName, BaseStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<QName, BaseStatement, EffectiveStatement<QName, BaseStatement>> {

        public Definition() {
            super(Rfc6020Mapping.BASE);
        }

        @Override
        public QName parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) {
            return Utils.qNameFromArgument(ctx, value);
        }

        @Override
        public BaseStatement createDeclared(
                StmtContext<QName, BaseStatement, ?> ctx) {
            return new BaseStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<QName, BaseStatement> createEffective(
                StmtContext<QName, BaseStatement, EffectiveStatement<QName, BaseStatement>> ctx) {
            return new BaseEffectiveStatementImpl(ctx);
        }

        @Override
        public void onStatementDefinitionDeclared(
                final Mutable<QName, BaseStatement, EffectiveStatement<QName, BaseStatement>> baseStmtCtx)
                throws SourceException {
            final Mutable<?, ?, ?> baseParentCtx = baseStmtCtx.getParentContext();
            if(StmtContextUtils.producesDeclared(baseParentCtx, IdentityStatement.class)) {

                final QName baseIdentityQName = baseStmtCtx.getStatementArgument();
                ModelActionBuilder baseIdentityAction = baseStmtCtx.newInferenceAction(ModelProcessingPhase.STATEMENT_DEFINITION);
                final Prerequisite<StmtContext<?, ?, ?>> requiresPrereq = baseIdentityAction.requiresCtx(baseStmtCtx, IdentityNamespace.class, baseIdentityQName, ModelProcessingPhase.STATEMENT_DEFINITION);
                final Prerequisite<StmtContext.Mutable<?, ?, ?>> mutatesPrereq = baseIdentityAction.mutatesCtx
                        (baseParentCtx, ModelProcessingPhase.STATEMENT_DEFINITION);

                baseIdentityAction.apply( new InferenceAction() {

                    @Override
                    public void apply() throws InferenceException {
                        List<StmtContext<?, ?, ?>> derivedIdentities = baseStmtCtx.getFromNamespace(DerivedIdentitiesNamespace.class, baseStmtCtx.getStatementArgument());
                        if(derivedIdentities == null) {
                            derivedIdentities = new LinkedList<>();
                            baseStmtCtx.addToNs(DerivedIdentitiesNamespace.class, baseIdentityQName, derivedIdentities);
                        }
                        derivedIdentities.add(baseParentCtx);
                    }

                    @Override
                    public void prerequisiteFailed(
                            Collection<? extends Prerequisite<?>> failed)
                            throws InferenceException {
                            throw new InferenceException("Unable to resolve identity "+baseParentCtx.getStatementArgument()+" and base identity " + baseStmtCtx.getStatementArgument(), baseStmtCtx
                                    .getStatementSourceReference());
                    }

                });
            }
        }
    }

    @Override
    public QName getName() {
        return argument();
    }

}
