/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.config;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ConfigEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ConfigStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseBooleanStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class ConfigStatementSupport
        extends BaseBooleanStatementSupport<ConfigStatement, ConfigEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.CONFIG).build();
    private static final ConfigStatementSupport INSTANCE = new ConfigStatementSupport();

    private ConfigStatementSupport() {
        super(YangStmtMapping.CONFIG, new EmptyConfigEffectiveStatement(new EmptyConfigStatement(Boolean.FALSE)),
            new EmptyConfigEffectiveStatement(new EmptyConfigStatement(Boolean.TRUE)));
    }

    public static ConfigStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected ConfigStatement createDeclared(final StmtContext<Boolean, ConfigStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularConfigStatement(ctx.coerceStatementArgument(), substatements);
    }

    @Override
    protected ConfigEffectiveStatement createEffective(
            final StmtContext<Boolean, ConfigStatement, ConfigEffectiveStatement> ctx,
            final ConfigStatement declared, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new RegularConfigEffectiveStatement(declared, substatements);
    }

    @Override
    protected EmptyConfigEffectiveStatement createEmptyEffective(final ConfigStatement declared) {
        return new EmptyConfigEffectiveStatement(declared);
    }
}