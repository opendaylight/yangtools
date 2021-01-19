/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.reference;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStringStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class ReferenceStatementSupport
        extends AbstractStringStatementSupport<ReferenceStatement, ReferenceEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.REFERENCE)
        .build();
    private static final ReferenceStatementSupport INSTANCE = new ReferenceStatementSupport();

    private ReferenceStatementSupport() {
        super(YangStmtMapping.REFERENCE, StatementPolicy.contextIndependent());
    }

    public static ReferenceStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected ReferenceStatement createDeclared(final StmtContext<String, ReferenceStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularReferenceStatement(ctx.getRawArgument(), substatements);
    }

    @Override
    protected ReferenceStatement createEmptyDeclared(final StmtContext<String, ReferenceStatement, ?> ctx) {
        return new EmptyReferenceStatement(ctx.getRawArgument());
    }

    @Override
    protected ReferenceEffectiveStatement createEffective(final Current<String, ReferenceStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyReferenceEffectiveStatement(stmt.declared())
            : new RegularReferenceEffectiveStatement(stmt.declared(), substatements);
    }
}
