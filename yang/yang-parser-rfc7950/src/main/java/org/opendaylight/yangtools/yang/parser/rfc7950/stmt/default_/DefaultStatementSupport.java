/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.default_;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStringStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class DefaultStatementSupport
        extends BaseStringStatementSupport<DefaultStatement, DefaultEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.DEFAULT).build();
    private static final DefaultStatementSupport INSTANCE = new DefaultStatementSupport();

    private DefaultStatementSupport() {
        // Note: if we start interpreting the string we'll need to use CopyPolicy.DECLARED_COPY
        super(YangStmtMapping.DEFAULT, CopyPolicy.CONTEXT_INDEPENDENT);
    }

    public static DefaultStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected DefaultStatement createDeclared(final StmtContext<String, DefaultStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularDefaultStatement(ctx.getRawArgument(), substatements);
    }

    @Override
    protected DefaultStatement createEmptyDeclared(final StmtContext<String, DefaultStatement, ?> ctx) {
        return new EmptyDefaultStatement(ctx.getRawArgument());
    }

    @Override
    protected DefaultEffectiveStatement createEffective(final Current<String, DefaultStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyDefaultEffectiveStatement(stmt.declared())
            : new RegularDefaultEffectiveStatement(stmt.declared(), substatements);
    }

    @Override
    public @NonNull boolean copyEffective(final DefaultEffectiveStatement original,
                                          final Current<String, DefaultStatement> stmt) {
        return true;
    }
}