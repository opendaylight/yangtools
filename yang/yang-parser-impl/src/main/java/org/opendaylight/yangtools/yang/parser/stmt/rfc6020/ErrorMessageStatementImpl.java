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
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorMessageStatement;
import org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ErrorMessageEffectiveStatementImpl;

public class ErrorMessageStatementImpl extends
        AbstractDeclaredStatement<String> implements ErrorMessageStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
            .ERROR_MESSAGE)
            .build();

    protected ErrorMessageStatementImpl(
            StmtContext<String, ErrorMessageStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<String, ErrorMessageStatement, EffectiveStatement<String, ErrorMessageStatement>> {

        public Definition() {
            super(YangStmtMapping.ERROR_MESSAGE);
        }

        @Override
        public String parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) {
            return value;
        }

        @Override
        public ErrorMessageStatement createDeclared(
                StmtContext<String, ErrorMessageStatement, ?> ctx) {
            return new ErrorMessageStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<String, ErrorMessageStatement> createEffective(
                StmtContext<String, ErrorMessageStatement, EffectiveStatement<String, ErrorMessageStatement>> ctx) {
            return new ErrorMessageEffectiveStatementImpl(ctx);
        }

        @Override
        public void onFullDefinitionDeclared(StmtContext.Mutable<String, ErrorMessageStatement,
                EffectiveStatement<String, ErrorMessageStatement>> stmt) {
            super.onFullDefinitionDeclared(stmt);
            getSubstatementValidator().validate(stmt);
        }

        @Override
        protected SubstatementValidator getSubstatementValidator() {
            return SUBSTATEMENT_VALIDATOR;
        }
    }

    @Nonnull
    @Override
    public String getValue() {
        return argument();
    }
}
