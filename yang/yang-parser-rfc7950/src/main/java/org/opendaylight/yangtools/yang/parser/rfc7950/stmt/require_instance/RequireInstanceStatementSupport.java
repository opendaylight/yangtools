/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.require_instance;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RequireInstanceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RequireInstanceStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseBooleanStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class RequireInstanceStatementSupport
        extends BaseBooleanStatementSupport<RequireInstanceStatement, RequireInstanceEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.REQUIRE_INSTANCE).build();
    private static final RequireInstanceStatementSupport INSTANCE = new RequireInstanceStatementSupport();


    private RequireInstanceStatementSupport() {
        super(YangStmtMapping.REQUIRE_INSTANCE,
            new EmptyRequireInstanceEffectiveStatement(new EmptyRequireInstanceStatement(Boolean.FALSE)),
            new EmptyRequireInstanceEffectiveStatement(new EmptyRequireInstanceStatement(Boolean.TRUE)));
    }

    public static RequireInstanceStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected RequireInstanceStatement createDeclared(final StmtContext<Boolean, RequireInstanceStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularRequireInstanceStatement(ctx.coerceStatementArgument(), substatements);
    }

    @Override
    protected RequireInstanceEffectiveStatement createEffective(
            final StmtContext<Boolean, RequireInstanceStatement, RequireInstanceEffectiveStatement> ctx,
            final RequireInstanceStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new RegularRequireInstanceEffectiveStatement(declared, substatements);
    }

    @Override
    protected EmptyRequireInstanceEffectiveStatement createEmptyEffective(final RequireInstanceStatement declared) {
        return new EmptyRequireInstanceEffectiveStatement(declared);
    }
}