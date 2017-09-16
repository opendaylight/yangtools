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
import org.opendaylight.yangtools.yang.model.api.stmt.ContactStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ContactEffectiveStatementImpl;

public class ContactStatementImpl extends AbstractDeclaredStatement<String> implements ContactStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
            .CONTACT)
            .build();

    protected ContactStatementImpl(final StmtContext<String, ContactStatement,?> context) {
        super(context);
    }

    public static class Definition extends
        AbstractStatementSupport<String, ContactStatement,EffectiveStatement<String, ContactStatement>> {

        public Definition() {
            super(YangStmtMapping.CONTACT);
        }

        @Override
        public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            return value;
        }

        @Override
        public ContactStatement createDeclared(final StmtContext<String, ContactStatement, ?> ctx) {
            return new ContactStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<String, ContactStatement> createEffective(
                final StmtContext<String, ContactStatement, EffectiveStatement<String, ContactStatement>> ctx) {
            return new ContactEffectiveStatementImpl(ctx);
        }

        @Override
        protected SubstatementValidator getSubstatementValidator() {
            return SUBSTATEMENT_VALIDATOR;
        }
    }

    @Nonnull
    @Override
    public String getText() {
        return rawArgument();
    }
}
