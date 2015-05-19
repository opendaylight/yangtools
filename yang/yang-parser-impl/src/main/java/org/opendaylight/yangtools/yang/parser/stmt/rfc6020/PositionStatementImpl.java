/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.PositionEffectiveStatementImpl;

import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PositionStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public class PositionStatementImpl extends AbstractDeclaredStatement<Long>
        implements PositionStatement {

    protected PositionStatementImpl(
            StmtContext<Long, PositionStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<Long, PositionStatement, EffectiveStatement<Long, PositionStatement>> {

        public Definition() {
            super(Rfc6020Mapping.POSITION);
        }

        @Override
        public Long parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(String.format("Position value %s is not valid integer", value), e);
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

    }

    @Override
    public Long getValue() {
        return argument();
    }

}
