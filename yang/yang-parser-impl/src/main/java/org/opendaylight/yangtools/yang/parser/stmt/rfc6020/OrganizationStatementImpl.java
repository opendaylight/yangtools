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
import org.opendaylight.yangtools.yang.model.api.stmt.OrganizationStatement;
import org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.OrganizationEffectiveStatementImpl;

public class OrganizationStatementImpl extends
        AbstractDeclaredStatement<String> implements OrganizationStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
            .ORGANIZATION)
            .build();

    protected OrganizationStatementImpl(
            StmtContext<String, OrganizationStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<String, OrganizationStatement, EffectiveStatement<String, OrganizationStatement>> {

        public Definition() {
            super(YangStmtMapping.ORGANIZATION);
        }

        @Override
        public String parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) {
            return value;
        }

        @Override
        public OrganizationStatement createDeclared(
                StmtContext<String, OrganizationStatement, ?> ctx) {
            return new OrganizationStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<String, OrganizationStatement> createEffective(
                StmtContext<String, OrganizationStatement, EffectiveStatement<String, OrganizationStatement>> ctx) {
            return new OrganizationEffectiveStatementImpl(ctx);
        }

        @Override
        public void onFullDefinitionDeclared(StmtContext.Mutable<String, OrganizationStatement,
                EffectiveStatement<String, OrganizationStatement>> stmt) {
            super.onFullDefinitionDeclared(stmt);
            SUBSTATEMENT_VALIDATOR.validate(stmt);
        }
    }

    @Nonnull
    @Override
    public String getText() {
        return argument();
    }

}
