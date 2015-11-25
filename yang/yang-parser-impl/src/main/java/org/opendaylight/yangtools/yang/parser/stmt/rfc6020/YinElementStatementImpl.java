/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YinElementStatement;
import org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.YinElementEffectiveStatementImpl;

public class YinElementStatementImpl extends AbstractDeclaredStatement<Boolean>
        implements YinElementStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(Rfc6020Mapping
            .YIN_ELEMENT)
            .build();

    protected YinElementStatementImpl(
            StmtContext<Boolean, YinElementStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<Boolean, YinElementStatement, EffectiveStatement<Boolean, YinElementStatement>> {

        public Definition() {
            super(Rfc6020Mapping.YIN_ELEMENT);
        }

        @Override
        public Boolean parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) {
            return Boolean.valueOf(value);
        }

        @Override
        public YinElementStatement createDeclared(
                StmtContext<Boolean, YinElementStatement, ?> ctx) {
            return new YinElementStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<Boolean, YinElementStatement> createEffective(
                StmtContext<Boolean, YinElementStatement, EffectiveStatement<Boolean, YinElementStatement>> ctx) {
            return new YinElementEffectiveStatementImpl(ctx);
        }

        @Override
        public void onFullDefinitionDeclared(StmtContext.Mutable<Boolean, YinElementStatement,
                EffectiveStatement<Boolean, YinElementStatement>> stmt) throws SourceException {
            super.onFullDefinitionDeclared(stmt);
            SUBSTATEMENT_VALIDATOR.validate(stmt);
        }
    }

    @Override
    public Boolean getValue() {
        return argument();
    }

}
