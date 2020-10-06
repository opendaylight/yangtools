/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.units;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnitsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnitsStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStringStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class UnitsStatementSupport
        extends BaseStringStatementSupport<UnitsStatement, UnitsEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.UNITS)
        .build();
    private static final UnitsStatementSupport INSTANCE = new UnitsStatementSupport();

    private UnitsStatementSupport() {
        super(YangStmtMapping.UNITS);
    }

    public static UnitsStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected UnitsStatement createDeclared(final StmtContext<String, UnitsStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularUnitsStatement(ctx.coerceRawStatementArgument(), substatements);
    }

    @Override
    protected UnitsStatement createEmptyDeclared(final StmtContext<String, UnitsStatement, ?> ctx) {
        return new EmptyUnitsStatement(ctx.coerceRawStatementArgument());
    }

    @Override
    protected UnitsEffectiveStatement createEffective(
            final StmtContext<String, UnitsStatement, UnitsEffectiveStatement> ctx,
            final UnitsStatement declared, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new RegularUnitsEffectiveStatement(declared, substatements);
    }

    @Override
    protected UnitsEffectiveStatement createEmptyEffective(
            final StmtContext<String, UnitsStatement, UnitsEffectiveStatement> ctx,
            final UnitsStatement declared) {
        return new EmptyUnitsEffectiveStatement(declared);
    }
}
