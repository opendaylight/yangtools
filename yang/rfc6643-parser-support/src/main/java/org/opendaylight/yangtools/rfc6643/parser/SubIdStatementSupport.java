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
import org.opendaylight.yangtools.rfc6643.model.api.IetfYangSmiv2ExtensionsMapping;
import org.opendaylight.yangtools.rfc6643.model.api.SubIdEffectiveStatement;
import org.opendaylight.yangtools.rfc6643.model.api.SubIdStatement;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

@Beta
public final class SubIdStatementSupport
        extends AbstractStatementSupport<Uint32, SubIdStatement, SubIdEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
            SubstatementValidator.builder(IetfYangSmiv2ExtensionsMapping.SUB_ID).build();
    private static final SubIdStatementSupport INSTANCE = new SubIdStatementSupport();

    private SubIdStatementSupport() {
        super(IetfYangSmiv2ExtensionsMapping.SUB_ID, CopyPolicy.CONTEXT_INDEPENDENT);
    }

    public static SubIdStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public Uint32 parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return Uint32.valueOf(value);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected SubIdStatement createDeclared(final StmtContext<Uint32, SubIdStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new SubIdStatementImpl(ctx.getArgument(), substatements);
    }

    @Override
    protected SubIdStatement createEmptyDeclared(final StmtContext<Uint32, SubIdStatement, ?> ctx) {
        return createDeclared(ctx, ImmutableList.of());
    }

    @Override
    protected SubIdEffectiveStatement createEffective(final Current<Uint32, SubIdStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new SubIdEffectiveStatementImpl(stmt, substatements);
    }
}
