/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ValueEffectiveStatementImpl;

import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public class ValueStatementImpl extends AbstractDeclaredStatement<String>
        implements ValueStatement {

    protected ValueStatementImpl(StmtContext<String, ValueStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<String, ValueStatement, EffectiveStatement<String, ValueStatement>> {

        public Definition() {
            super(Rfc6020Mapping.VALUE);
        }

        @Override
        public String parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) {
            return value;
        }

        @Override
        public ValueStatement createDeclared(
                StmtContext<String, ValueStatement, ?> ctx) {
            return new ValueStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<String, ValueStatement> createEffective(
                StmtContext<String, ValueStatement, EffectiveStatement<String, ValueStatement>> ctx) {
            return new ValueEffectiveStatementImpl(ctx);
        }

    }

    @Override
    public String getValue() {
        return argument();
    }

}
