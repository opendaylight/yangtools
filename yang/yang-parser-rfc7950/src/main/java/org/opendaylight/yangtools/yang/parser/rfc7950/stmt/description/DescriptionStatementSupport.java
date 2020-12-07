/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.description;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStringStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class DescriptionStatementSupport
        extends BaseStringStatementSupport<DescriptionStatement, DescriptionEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.DESCRIPTION).build();
    private static final DescriptionStatementSupport INSTANCE = new DescriptionStatementSupport();

    private DescriptionStatementSupport() {
        super(YangStmtMapping.DESCRIPTION, CopyPolicy.CONTEXT_INDEPENDENT);
    }

    public static DescriptionStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected DescriptionStatement createDeclared(final StmtContext<String, DescriptionStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularDescriptionStatement(ctx.getRawArgument(), substatements);
    }

    @Override
    protected DescriptionStatement createEmptyDeclared(final StmtContext<String, DescriptionStatement, ?> ctx) {
        return new EmptyDescriptionStatement(ctx.getRawArgument());
    }

    @Override
    protected DescriptionEffectiveStatement createEffective(final Current<String, DescriptionStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyDescriptionEffectiveStatement(stmt.declared())
            : new RegularDescriptionEffectiveStatement(stmt.declared(), substatements);
    }
}
