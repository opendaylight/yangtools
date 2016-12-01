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
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.DescriptionEffectiveStatementImpl;

public class DescriptionStatementImpl extends AbstractDeclaredStatement<String> implements DescriptionStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
            .DESCRIPTION)
            .build();

    protected DescriptionStatementImpl(
            StmtContext<String, DescriptionStatement, ?> context) {
        super(context);
    }

    public static class Definition extends AbstractStatementSupport<String,DescriptionStatement,EffectiveStatement<String,DescriptionStatement>> {

        public Definition() {
            super(YangStmtMapping.DESCRIPTION);
        }

        @Override
        public String parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) {
            return value;
        }

        @Override
        public DescriptionStatement createDeclared(StmtContext<String, DescriptionStatement, ?> ctx) {
            return new DescriptionStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<String, DescriptionStatement> createEffective(StmtContext<String, DescriptionStatement, EffectiveStatement<String, DescriptionStatement>> ctx) {
            return new DescriptionEffectiveStatementImpl(ctx);
        }

        @Override
        public void onFullDefinitionDeclared(StmtContext.Mutable<String, DescriptionStatement,
                EffectiveStatement<String, DescriptionStatement>> stmt) {
            super.onFullDefinitionDeclared(stmt);
            SUBSTATEMENT_VALIDATOR.validate(stmt);
        }
    }

    @Nonnull @Override
    public String getText() {
        return rawArgument();
    }
}
