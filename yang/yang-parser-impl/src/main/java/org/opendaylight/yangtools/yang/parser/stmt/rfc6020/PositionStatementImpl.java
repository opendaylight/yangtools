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
import org.opendaylight.yangtools.yang.model.api.stmt.PositionStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.PositionEffectiveStatementImpl;

public class PositionStatementImpl extends AbstractDeclaredStatement<Long> implements PositionStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.POSITION).build();

    protected PositionStatementImpl(final StmtContext<Long, PositionStatement, ?> context) {
        super(context);
    }

    public static class Definition extends AbstractStatementSupport<Long, PositionStatement,
            EffectiveStatement<Long, PositionStatement>> {

        public Definition() {
            super(YangStmtMapping.POSITION);
        }

        @Override
        public Long parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                throw new SourceException(String.format("Bit position value %s is not valid integer", value),
                        ctx.getStatementSourceReference(), e);
            }
        }

        @Override
        public PositionStatement createDeclared(final StmtContext<Long, PositionStatement, ?> ctx) {
            return new PositionStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<Long, PositionStatement> createEffective(
                final StmtContext<Long, PositionStatement, EffectiveStatement<Long, PositionStatement>> ctx) {
            return new PositionEffectiveStatementImpl(ctx);
        }

        @Override
        protected SubstatementValidator getSubstatementValidator() {
            return SUBSTATEMENT_VALIDATOR;
        }
    }

    @Override
    public long getValue() {
        return argument();
    }
}
