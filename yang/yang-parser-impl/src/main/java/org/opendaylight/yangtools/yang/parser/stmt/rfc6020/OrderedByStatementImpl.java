/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.OrderedByEffectiveStatementImpl;

import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrderedByStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public class OrderedByStatementImpl extends AbstractDeclaredStatement<String>
        implements OrderedByStatement {

    protected OrderedByStatementImpl(
            StmtContext<String, OrderedByStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<String, OrderedByStatement, EffectiveStatement<String, OrderedByStatement>> {

        public Definition() {
            super(Rfc6020Mapping.ORDERED_BY);
        }

        @Override
        public String parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) {
            return value;
        }

        @Override
        public OrderedByStatement createDeclared(
                StmtContext<String, OrderedByStatement, ?> ctx) {
            return new OrderedByStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<String, OrderedByStatement> createEffective(
                StmtContext<String, OrderedByStatement, EffectiveStatement<String, OrderedByStatement>> ctx) {
            return new OrderedByEffectiveStatementImpl(ctx);
        }

    }

    @Override
    public String getValue() {
        return argument();
    }

}
