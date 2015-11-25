/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RequireInstanceStatement;
import org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.RequireInstanceEffectiveStatementImpl;

public class RequireInstanceStatementImpl extends
        AbstractDeclaredStatement<Boolean> implements RequireInstanceStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(Rfc6020Mapping
            .REQUIRE_INSTANCE)
            .build();

    protected RequireInstanceStatementImpl(
            StmtContext<Boolean, RequireInstanceStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<Boolean, RequireInstanceStatement, EffectiveStatement<Boolean, RequireInstanceStatement>> {

        public Definition() {
            super(Rfc6020Mapping.REQUIRE_INSTANCE);
        }

        @Override
        public Boolean parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) {
            return Boolean.valueOf(value);
        }

        @Override
        public RequireInstanceStatement createDeclared(
                StmtContext<Boolean, RequireInstanceStatement, ?> ctx) {
            return new RequireInstanceStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<Boolean, RequireInstanceStatement> createEffective(
                StmtContext<Boolean, RequireInstanceStatement, EffectiveStatement<Boolean, RequireInstanceStatement>> ctx) {
            return new RequireInstanceEffectiveStatementImpl(ctx);
        }

        @Override
        public void onFullDefinitionDeclared(StmtContext.Mutable<Boolean, RequireInstanceStatement,
                EffectiveStatement<Boolean, RequireInstanceStatement>> stmt) throws SourceException {
            super.onFullDefinitionDeclared(stmt);
            SUBSTATEMENT_VALIDATOR.validate(stmt);
        }
    }

    @Override
    public Boolean getValue() {
        return argument();
    }

}
