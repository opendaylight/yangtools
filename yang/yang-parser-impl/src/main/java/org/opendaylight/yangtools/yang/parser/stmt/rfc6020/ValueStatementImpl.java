/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ValueEffectiveStatementImpl;

public class ValueStatementImpl extends AbstractDeclaredStatement<Integer> implements ValueStatement {

    protected ValueStatementImpl(StmtContext<Integer, ValueStatement, ?> context) {
        super(context);
    }

    public static class Definition extends
            AbstractStatementSupport<Integer, ValueStatement, EffectiveStatement<Integer, ValueStatement>> {

        public Definition() {
            super(Rfc6020Mapping.VALUE);
        }

        @Override
        public Integer parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) {
            int valueNum;

            try {
                valueNum = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        String.format(
                                "%s is not valid value statement integer argument in a range of -2147483648..2147483647",
                                value), e);
            }

            return valueNum;
        }

        @Override
        public ValueStatement createDeclared(StmtContext<Integer, ValueStatement, ?> ctx) {
            return new ValueStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<Integer, ValueStatement> createEffective(
                StmtContext<Integer, ValueStatement, EffectiveStatement<Integer, ValueStatement>> ctx) {
            return new ValueEffectiveStatementImpl(ctx);
        }

    }

    @Override
    public Integer getValue() {
        return argument();
    }

}
