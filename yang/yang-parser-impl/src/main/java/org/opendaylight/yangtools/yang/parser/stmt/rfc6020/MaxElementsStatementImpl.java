/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.MaxElementsEffectiveStatementImpl;

import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MaxElementsStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public class MaxElementsStatementImpl extends AbstractDeclaredStatement<String>
        implements MaxElementsStatement {
    private static final long serialVersionUID = 1L;

    protected MaxElementsStatementImpl(
            StmtContext<String, MaxElementsStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<String, MaxElementsStatement, EffectiveStatement<String, MaxElementsStatement>> {

        public Definition() {
            super(Rfc6020Mapping.MAX_ELEMENTS);
        }

        @Override
        public String parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) {
            return value;
        }

        @Override
        public MaxElementsStatement createDeclared(
                StmtContext<String, MaxElementsStatement, ?> ctx) {
            return new MaxElementsStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<String, MaxElementsStatement> createEffective(
                StmtContext<String, MaxElementsStatement, EffectiveStatement<String, MaxElementsStatement>> ctx) {
            return new MaxElementsEffectiveStatementImpl(ctx);
        }

    }

    @Override
    public String getValue() {
        return argument();
    }

}
