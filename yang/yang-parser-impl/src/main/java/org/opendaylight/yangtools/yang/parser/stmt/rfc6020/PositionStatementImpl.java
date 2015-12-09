/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PositionStatement;
import org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.PositionEffectiveStatementImpl;

public class PositionStatementImpl extends AbstractDeclaredStatement<Long> implements PositionStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(Rfc6020Mapping
            .POSITION)
            .build();

    protected PositionStatementImpl(StmtContext<Long, PositionStatement, ?> context) {
        super(context);
    }

    public static class Definition extends AbstractStatementSupport<Long, PositionStatement,
            EffectiveStatement<Long, PositionStatement>> {

        public Definition() {
            super(Rfc6020Mapping.POSITION);
        }

        @Override
        public Long parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                throw new SourceException(String.format("Bit position value %s is not valid integer", value),
                        ctx.getStatementSourceReference(), e);
            }
        }

        @Override
        public PositionStatement createDeclared(
                StmtContext<Long, PositionStatement, ?> ctx) {
            return new PositionStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<Long, PositionStatement> createEffective(
                StmtContext<Long, PositionStatement, EffectiveStatement<Long, PositionStatement>> ctx) {
            return new PositionEffectiveStatementImpl(ctx);
        }

        @Override
        public void onFullDefinitionDeclared(StmtContext.Mutable<Long, PositionStatement,
                EffectiveStatement<Long, PositionStatement>> stmt) throws SourceException {
            super.onFullDefinitionDeclared(stmt);
            SUBSTATEMENT_VALIDATOR.validate(stmt);
        }
    }

    @Override
    public Long getValue() {
        return argument();
    }

}
