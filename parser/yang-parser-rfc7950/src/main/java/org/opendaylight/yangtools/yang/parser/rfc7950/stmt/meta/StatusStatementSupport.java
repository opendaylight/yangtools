/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class StatusStatementSupport
        extends AbstractStatementSupport<Status, StatusStatement, StatusEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
        SubstatementValidator.builder(StatusStatement.DEF).build();
    private static final StatusArgumentSupport ARGUMENT_SUPPORT =
        new StatusArgumentSupport(StatusStatement.DEF.getArgumentDefinition());

    public StatusStatementSupport(final YangParserConfiguration config) {
        super(StatusStatement.DEF, ARGUMENT_SUPPORT, StatementPolicy.contextIndependent(), config,
            SUBSTATEMENT_VALIDATOR);
    }

    @Override
    protected StatusStatement createDeclared(final BoundStmtCtx<Status> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createStatus(ctx.getArgument(), substatements);
    }

    @Override
    protected StatusStatement attachDeclarationReference(final StatusStatement stmt,
            final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateStatus(stmt, reference);
    }

    @Override
    protected StatusEffectiveStatement createEffective(final Current<Status, StatusStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return EffectiveStatements.createStatus(stmt.declared(), substatements);
    }
}
