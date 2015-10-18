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
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.EnumStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.EnumEffectiveStatementImpl;

public class EnumStatementImpl extends AbstractDeclaredStatement<String> implements EnumStatement {

    protected EnumStatementImpl(final StmtContext<String, EnumStatement, ?> context) {
        super(context);
    }

    public static class Definition extends
            AbstractStatementSupport<String, EnumStatement, EffectiveStatement<String, EnumStatement>> {

        public Definition() {
            super(Rfc6020Mapping.ENUM);
        }

        @Override
        public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            // FIXME: Checks for real value
            return value;
        }

        @Override
        public EnumStatement createDeclared(final StmtContext<String, EnumStatement, ?> ctx) {
            return new EnumStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<String, EnumStatement> createEffective(
                final StmtContext<String, EnumStatement, EffectiveStatement<String, EnumStatement>> ctx) {
            return new EnumEffectiveStatementImpl(ctx);
        }
    }

    @Override
    public StatusStatement getStatus() {
        return firstDeclared(StatusStatement.class);
    }

    @Override
    public DescriptionStatement getDescription() {
        return firstDeclared(DescriptionStatement.class);
    }

    @Override
    public ReferenceStatement getReference() {
        return firstDeclared(ReferenceStatement.class);
    }

    @Override
    public String getName() {
        return argument();
    }

    @Override
    public ValueStatement getValue() {
        return firstDeclared(ValueStatement.class);
    }
}
