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
import org.opendaylight.yangtools.yang.model.api.stmt.OrderedByStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.OrderedByEffectiveStatementImpl;

public class OrderedByStatementImpl extends AbstractDeclaredStatement<String> implements OrderedByStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
            .ORDERED_BY)
            .build();

    protected OrderedByStatementImpl(
            final StmtContext<String, OrderedByStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<String, OrderedByStatement, EffectiveStatement<String, OrderedByStatement>> {

        public Definition() {
            super(YangStmtMapping.ORDERED_BY);
        }

        @Override
        public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            return value;
        }

        @Override
        public OrderedByStatement createDeclared(final StmtContext<String, OrderedByStatement, ?> ctx) {
            return new OrderedByStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<String, OrderedByStatement> createEffective(
                final StmtContext<String, OrderedByStatement, EffectiveStatement<String, OrderedByStatement>> ctx) {
            return new OrderedByEffectiveStatementImpl(ctx);
        }

        @Override
        protected SubstatementValidator getSubstatementValidator() {
            return SUBSTATEMENT_VALIDATOR;
        }

        @Override
        public String internArgument(final String rawArgument) {
            if ("user".equals(rawArgument)) {
                return "user";
            } else if ("system".equals(rawArgument)) {
                return "system";
            } else {
                return rawArgument;
            }
        }
    }

    @Nonnull
    @Override
    public String getValue() {
        return argument();
    }

}
