/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FractionDigitsStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public class FractionDigitsStatementImpl extends
        AbstractDeclaredStatement<String> implements FractionDigitsStatement {

    protected FractionDigitsStatementImpl(
            StmtContext<String, FractionDigitsStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<String, FractionDigitsStatement, EffectiveStatement<String, FractionDigitsStatement>> {

        public Definition() {
            super(Rfc6020Mapping.FRACTION_DIGITS);
        }

        @Override
        public String parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) {
            return value;
        }

        @Override
        public FractionDigitsStatement createDeclared(
                StmtContext<String, FractionDigitsStatement, ?> ctx) {
            return new FractionDigitsStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<String, FractionDigitsStatement> createEffective(
                StmtContext<String, FractionDigitsStatement, EffectiveStatement<String, FractionDigitsStatement>> ctx) {
            throw new UnsupportedOperationException();
        }

    }

    @Override
    public String getValue() {
        return argument();
    }
}
