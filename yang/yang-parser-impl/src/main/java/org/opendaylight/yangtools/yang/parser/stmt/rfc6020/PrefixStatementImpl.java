/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.PrefixEffectiveStatementImpl;

public class PrefixStatementImpl extends AbstractDeclaredStatement<String> implements PrefixStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(Rfc6020Mapping
            .PREFIX)
            .build();

    public static class Definition extends AbstractStatementSupport<String,PrefixStatement,EffectiveStatement<String,PrefixStatement>> {

        public Definition() {
            super(Rfc6020Mapping.PREFIX);
        }

        @Override
        public String parseArgumentValue(StmtContext<?, ?,?> ctx, String value) {
            return value;
        }

        @Override
        public PrefixStatement createDeclared(StmtContext<String, PrefixStatement,?> ctx) {
            return new PrefixStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<String,PrefixStatement> createEffective(StmtContext<String, PrefixStatement,EffectiveStatement<String,PrefixStatement>> ctx) {
            return new PrefixEffectiveStatementImpl(ctx);
        }

        @Override
        public void onFullDefinitionDeclared(StmtContext.Mutable<String, PrefixStatement,
                EffectiveStatement<String, PrefixStatement>> stmt) {
            super.onFullDefinitionDeclared(stmt);
            SUBSTATEMENT_VALIDATOR.validate(stmt);
        }
    }

    PrefixStatementImpl(StmtContext<String, PrefixStatement,?> context) {
        super(context);
    }

    @Nonnull
    @Override
    public String getValue() {
        return rawArgument();
    }
}
