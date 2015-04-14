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

public class PositionStatementImpl extends AbstractDeclaredStatement<String>
        implements PositionStatement {

    protected PositionStatementImpl(
            StmtContext<String, PositionStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<String, PositionStatement, EffectiveStatement<String, PositionStatement>> {

        public Definition() {
            super(Rfc6020Mapping.Position);
        }

        @Override
        public String parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) {
            return value;
        }

        @Override
        public PositionStatement createDeclared(
                StmtContext<String, PositionStatement, ?> ctx) {
            return new PositionStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<String, PositionStatement> createEffective(
                StmtContext<String, PositionStatement, EffectiveStatement<String, PositionStatement>> ctx) {
            return new PositionEffectiveStatementImpl(ctx);
        }

    }

    @Override
    public String getValue() {
        return argument();
    }

}
