/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RequireInstanceStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.RequireInstanceEffectiveStatementImpl;

public class RequireInstanceStatementImpl extends
        AbstractDeclaredStatement<Boolean> implements RequireInstanceStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.REQUIRE_INSTANCE).build();

    protected RequireInstanceStatementImpl(final StmtContext<Boolean, RequireInstanceStatement, ?> context) {
        super(context);
    }

    public static class Definition extends
            AbstractStatementSupport<Boolean, RequireInstanceStatement, EffectiveStatement<Boolean, RequireInstanceStatement>> {

        public Definition() {
            super(YangStmtMapping.REQUIRE_INSTANCE);
        }

        @Override
        public Boolean parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            return Utils.parseBoolean(ctx, value);
        }

        @Override
        public RequireInstanceStatement createDeclared(final StmtContext<Boolean, RequireInstanceStatement, ?> ctx) {
            return new RequireInstanceStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<Boolean, RequireInstanceStatement> createEffective(
                final StmtContext<Boolean, RequireInstanceStatement, EffectiveStatement<Boolean, RequireInstanceStatement>> ctx) {
            return new RequireInstanceEffectiveStatementImpl(ctx);
        }

        @Override
        public String internArgument(final String rawArgument) {
            return Utils.internBoolean(rawArgument);
        }

        @Override
        protected SubstatementValidator getSubstatementValidator() {
            return SUBSTATEMENT_VALIDATOR;
        }
    }

    @Override
    public boolean getValue() {
        return argument().booleanValue();
    }
}
