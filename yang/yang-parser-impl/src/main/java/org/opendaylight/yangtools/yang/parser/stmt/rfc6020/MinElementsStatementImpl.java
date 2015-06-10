/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.MinElementsEffectiveStatementImpl;

import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MinElementsStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public class MinElementsStatementImpl extends
        AbstractDeclaredStatement<Integer> implements MinElementsStatement {

    protected MinElementsStatementImpl(
            StmtContext<Integer, MinElementsStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<Integer, MinElementsStatement, EffectiveStatement<Integer, MinElementsStatement>> {

        public Definition() {
            super(Rfc6020Mapping.MIN_ELEMENTS);
        }

        @Override
        public Integer parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) {
            return Integer.parseInt(value);
        }

        @Override
        public MinElementsStatement createDeclared(
                StmtContext<Integer, MinElementsStatement, ?> ctx) {
            return new MinElementsStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<Integer, MinElementsStatement> createEffective(
                StmtContext<Integer, MinElementsStatement, EffectiveStatement<Integer, MinElementsStatement>> ctx) {
            return new MinElementsEffectiveStatementImpl(ctx);
        }
    }

    @Override
    public Integer getValue() {
        return argument();
    }

}
