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
import org.opendaylight.yangtools.yang.model.api.stmt.MaxElementsStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.MaxElementsEffectiveStatementImpl;

public class MaxElementsStatementImpl extends AbstractDeclaredStatement<String>
        implements MaxElementsStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
            .MAX_ELEMENTS)
            .build();

    protected MaxElementsStatementImpl(
            final StmtContext<String, MaxElementsStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<String, MaxElementsStatement, EffectiveStatement<String, MaxElementsStatement>> {

        public Definition() {
            super(YangStmtMapping.MAX_ELEMENTS);
        }

        @Override
        public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            return value;
        }

        @Override
        public MaxElementsStatement createDeclared(
                final StmtContext<String, MaxElementsStatement, ?> ctx) {
            return new MaxElementsStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<String, MaxElementsStatement> createEffective(
                final StmtContext<String, MaxElementsStatement, EffectiveStatement<String, MaxElementsStatement>> ctx) {
            return new MaxElementsEffectiveStatementImpl(ctx);
        }

        @Override
        protected SubstatementValidator getSubstatementValidator() {
            return SUBSTATEMENT_VALIDATOR;
        }
    }

    @Nonnull
    @Override
    public String getValue() {
        return argument();
    }

}
