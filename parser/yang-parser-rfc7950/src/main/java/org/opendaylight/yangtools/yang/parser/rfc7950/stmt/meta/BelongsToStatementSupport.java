/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractUnqualifiedStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class BelongsToStatementSupport
        extends AbstractUnqualifiedStatementSupport<BelongsToStatement, BelongsToEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
            SubstatementValidator.builder(YangStmtMapping.BELONGS_TO).addMandatory(YangStmtMapping.PREFIX).build();

    public BelongsToStatementSupport(final YangParserConfiguration config) {
        super(YangStmtMapping.BELONGS_TO, StatementPolicy.reject(), config, SUBSTATEMENT_VALIDATOR);
    }

    @Override
    protected BelongsToStatement createDeclared(final BoundStmtCtx<Unqualified> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createBelongsTo(ctx.getArgument(), substatements);
    }

    @Override
    protected BelongsToStatement attachDeclarationReference(final BelongsToStatement stmt,
            final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateBelongsTo(stmt, reference);
    }

    @Override
    protected BelongsToEffectiveStatement createEffective(final Current<Unqualified, BelongsToStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return EffectiveStatements.createBelongsTo(stmt.declared(), substatements);
    }
}
