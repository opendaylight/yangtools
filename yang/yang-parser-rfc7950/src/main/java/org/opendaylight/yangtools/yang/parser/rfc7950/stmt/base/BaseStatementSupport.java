/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.base;

import java.util.Collection;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityStatement;
import org.opendaylight.yangtools.yang.parser.spi.IdentityNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractQNameStatementSupport;
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

public final class BaseStatementSupport extends AbstractQNameStatementSupport<BaseStatement, BaseEffectiveStatement> {
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
    public BaseStatement createDeclared(final StmtContext<QName, BaseStatement, ?> ctx) {
        return new BaseStatementImpl(ctx);
    }

    @Override
    public BaseEffectiveStatement createEffective(final StmtContext<QName, BaseStatement, BaseEffectiveStatement> ctx) {
        return new BaseEffectiveStatementImpl(ctx);
    }

    @Override
    public void onStatementDefinitionDeclared(final Mutable<QName, BaseStatement, BaseEffectiveStatement> baseStmtCtx) {
        final Mutable<?, ?, ?> baseParentCtx = baseStmtCtx.getParentContext();
        if (baseParentCtx.producesDeclared(IdentityStatement.class)) {

            final QName baseIdentityQName = baseStmtCtx.coerceStatementArgument();
            final ModelActionBuilder baseIdentityAction = baseStmtCtx.newInferenceAction(
                ModelProcessingPhase.STATEMENT_DEFINITION);
            baseIdentityAction.requiresCtx(baseStmtCtx, IdentityNamespace.class, baseIdentityQName,
                ModelProcessingPhase.STATEMENT_DEFINITION);
            baseIdentityAction.mutatesCtx(baseParentCtx, ModelProcessingPhase.STATEMENT_DEFINITION);

            baseIdentityAction.apply(new InferenceAction() {
                @Override
                public void apply(final InferenceContext ctx) {
                    // No-op, we just want to ensure the statement is specified
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