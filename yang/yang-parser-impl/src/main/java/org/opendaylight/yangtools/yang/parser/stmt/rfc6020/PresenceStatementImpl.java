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
import org.opendaylight.yangtools.yang.model.api.stmt.PresenceStatement;
import org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.PresenceEffectiveStatementImpl;

public class PresenceStatementImpl extends AbstractDeclaredStatement<String> implements PresenceStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(Rfc6020Mapping
            .PRESENCE)
            .build();

    protected PresenceStatementImpl(StmtContext<String, PresenceStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<String, PresenceStatement, EffectiveStatement<String, PresenceStatement>> {

        public Definition() {
            super(Rfc6020Mapping.PRESENCE);
        }

        @Override
        public String parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) {
            return value;
        }

        @Override
        public PresenceStatement createDeclared(StmtContext<String, PresenceStatement, ?> ctx) {
            return new PresenceStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<String, PresenceStatement> createEffective(
                StmtContext<String, PresenceStatement, EffectiveStatement<String, PresenceStatement>> ctx) {
            return new PresenceEffectiveStatementImpl(ctx);
        }

        @Override
        public void onFullDefinitionDeclared(StmtContext.Mutable<String, PresenceStatement,
                EffectiveStatement<String, PresenceStatement>> stmt) {
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
