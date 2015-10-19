/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ArgumentEffectiveStatementImpl;

import org.opendaylight.yangtools.yang.model.api.stmt.YinElementStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ArgumentStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public class ArgumentStatementImpl extends AbstractDeclaredStatement<QName>
        implements ArgumentStatement {
    private static final long serialVersionUID = 1L;

    protected ArgumentStatementImpl(
            StmtContext<QName, ArgumentStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<QName, ArgumentStatement, EffectiveStatement<QName, ArgumentStatement>> {

        public Definition() {
            super(Rfc6020Mapping.ARGUMENT);
        }

        @Override
        public QName parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) {
            return Utils.qNameFromArgument(ctx, value);
        }

        @Override
        public ArgumentStatement createDeclared(
                StmtContext<QName, ArgumentStatement, ?> ctx) {
            return new ArgumentStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<QName, ArgumentStatement> createEffective(
                StmtContext<QName, ArgumentStatement, EffectiveStatement<QName, ArgumentStatement>> ctx) {
            return new ArgumentEffectiveStatementImpl(ctx);
        }

    }

    @Override
    public QName getName() {
        return argument();
    }

    @Override
    public YinElementStatement getYinElement() {
        return firstDeclared(YinElementStatement.class);
    }

}