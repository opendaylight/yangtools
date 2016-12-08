/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorAppTagStatement;
import org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ErrorAppTagEffectiveStatementImpl;

public class ErrorAppTagStatementImpl extends AbstractDeclaredStatement<String>
        implements ErrorAppTagStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
            .ERROR_APP_TAG)
            .build();

    protected ErrorAppTagStatementImpl(
            StmtContext<String, ErrorAppTagStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<String, ErrorAppTagStatement, EffectiveStatement<String, ErrorAppTagStatement>> {

        public Definition() {
            super(YangStmtMapping.ERROR_APP_TAG);
        }

        @Override
        public String parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) {
            return value;
        }

        @Override
        public ErrorAppTagStatement createDeclared(
                StmtContext<String, ErrorAppTagStatement, ?> ctx) {
            return new ErrorAppTagStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<String, ErrorAppTagStatement> createEffective(
                StmtContext<String, ErrorAppTagStatement, EffectiveStatement<String, ErrorAppTagStatement>> ctx) {
            return new ErrorAppTagEffectiveStatementImpl(ctx);
        }

        @Override
        public void onFullDefinitionDeclared(StmtContext.Mutable<String, ErrorAppTagStatement,
                EffectiveStatement<String, ErrorAppTagStatement>> stmt) {
            super.onFullDefinitionDeclared(stmt);
            SUBSTATEMENT_VALIDATOR.validate(stmt);
        }
    }

    @Nonnull
    @Override
    public String getValue() {
        return argument();
    }
}
