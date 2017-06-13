/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ValueEffectiveStatementImpl;

public class ValueStatementImpl extends AbstractDeclaredStatement<Integer> implements ValueStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.VALUE).build();

    protected ValueStatementImpl(final StmtContext<Integer, ValueStatement, ?> context) {
        super(context);
    }

    public static class Definition extends
            AbstractStatementSupport<Integer, ValueStatement, EffectiveStatement<Integer, ValueStatement>> {

        public Definition() {
            super(YangStmtMapping.VALUE);
        }

        @Override
        public Integer parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            try {
                return Integer.valueOf(value);
            } catch (NumberFormatException e) {
                throw new SourceException(ctx.getStatementSourceReference(), e,
                    "%s is not valid value statement integer argument in a range of -2147483648..2147483647", value);
            }
        }

        @Override
        public ValueStatement createDeclared(final StmtContext<Integer, ValueStatement, ?> ctx) {
            return new ValueStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<Integer, ValueStatement> createEffective(
                final StmtContext<Integer, ValueStatement, EffectiveStatement<Integer, ValueStatement>> ctx) {
            return new ValueEffectiveStatementImpl(ctx);
        }

        @Override
        protected SubstatementValidator getSubstatementValidator() {
            return SUBSTATEMENT_VALIDATOR;
        }
    }

    @Override
    public int getValue() {
        return argument();
    }
}
