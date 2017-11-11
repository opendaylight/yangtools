/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.require_instance;

import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RequireInstanceStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.ArgumentUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class RequireInstanceStatementSupport extends AbstractStatementSupport<Boolean, RequireInstanceStatement,
        EffectiveStatement<Boolean, RequireInstanceStatement>> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.REQUIRE_INSTANCE).build();

    public RequireInstanceStatementSupport() {
        super(YangStmtMapping.REQUIRE_INSTANCE);
    }

    @Override
    public Boolean parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return ArgumentUtils.parseBoolean(ctx, value);
    }

    @Override
    public RequireInstanceStatement createDeclared(final StmtContext<Boolean, RequireInstanceStatement, ?> ctx) {
        return new RequireInstanceStatementImpl(ctx);
    }

    @Override
    public EffectiveStatement<Boolean, RequireInstanceStatement> createEffective(
            final StmtContext<Boolean, RequireInstanceStatement,
            EffectiveStatement<Boolean, RequireInstanceStatement>> ctx) {
        return new RequireInstanceEffectiveStatementImpl(ctx);
    }

    @Override
    public String internArgument(final String rawArgument) {
        return ArgumentUtils.internBoolean(rawArgument);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }
}