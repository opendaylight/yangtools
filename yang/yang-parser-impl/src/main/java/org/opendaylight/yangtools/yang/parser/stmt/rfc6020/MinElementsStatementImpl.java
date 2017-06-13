/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MinElementsStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.MinElementsEffectiveStatementImpl;

public class MinElementsStatementImpl extends
        AbstractDeclaredStatement<Integer> implements MinElementsStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
            .MIN_ELEMENTS)
            .build();

    protected MinElementsStatementImpl(
            final StmtContext<Integer, MinElementsStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<Integer, MinElementsStatement, EffectiveStatement<Integer, MinElementsStatement>> {

        public Definition() {
            super(YangStmtMapping.MIN_ELEMENTS);
        }

        @Override
        public Integer parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            return Integer.parseInt(value);
        }

        @Override
        public MinElementsStatement createDeclared(
                final StmtContext<Integer, MinElementsStatement, ?> ctx) {
            return new MinElementsStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<Integer, MinElementsStatement> createEffective(
                final StmtContext<Integer, MinElementsStatement, EffectiveStatement<Integer, MinElementsStatement>> ctx) {
            return new MinElementsEffectiveStatementImpl(ctx);
        }

        @Override
        protected SubstatementValidator getSubstatementValidator() {
            return SUBSTATEMENT_VALIDATOR;
        }
    }

    @Nonnull
    @Override
    public Integer getValue() {
        return argument();
    }

}
