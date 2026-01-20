/*
 * Copyright (c) 2016, 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6643.parser;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.rfc6643.model.api.SubIdEffectiveStatement;
import org.opendaylight.yangtools.rfc6643.model.api.SubIdStatement;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

@Beta
public final class SubIdStatementSupport
        extends AbstractStatementSupport<Uint32, SubIdStatement, SubIdEffectiveStatement> {
    private static final SubstatementValidator VALIDATOR =
            SubstatementValidator.builder(SubIdStatement.DEFINITION).build();

    public SubIdStatementSupport(final YangParserConfiguration config) {
        super(SubIdStatement.DEFINITION, StatementPolicy.contextIndependent(), config, VALIDATOR);
    }

    @Override
    public Uint32 parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return Uint32.valueOf(value);
    }

    @Override
    protected SubIdStatement createDeclared(final BoundStmtCtx<Uint32> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return new SubIdStatementImpl(ctx.getArgument(), substatements);
    }

    @Override
    protected SubIdStatement attachDeclarationReference(final SubIdStatement stmt,
            final DeclarationReference reference) {
        return new RefSubIdStatement(stmt, reference);
    }

    @Override
    protected SubIdEffectiveStatement createEffective(final Current<Uint32, SubIdStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new SubIdEffectiveStatementImpl(stmt, substatements);
    }
}
