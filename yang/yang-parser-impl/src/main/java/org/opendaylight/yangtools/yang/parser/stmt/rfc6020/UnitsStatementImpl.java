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
import org.opendaylight.yangtools.yang.model.api.stmt.UnitsStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.UnitsEffectiveStatementImpl;

public class UnitsStatementImpl extends AbstractDeclaredStatement<String>
        implements UnitsStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
            .UNITS)
            .build();

    protected UnitsStatementImpl(final StmtContext<String, UnitsStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<String, UnitsStatement, EffectiveStatement<String, UnitsStatement>> {

        public Definition() {
            super(YangStmtMapping.UNITS);
        }

        @Override
        public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            return value;
        }

        @Override
        public UnitsStatement createDeclared(
                final StmtContext<String, UnitsStatement, ?> ctx) {
            return new UnitsStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<String, UnitsStatement> createEffective(
                final StmtContext<String, UnitsStatement, EffectiveStatement<String, UnitsStatement>> ctx) {
            return new UnitsEffectiveStatementImpl(ctx);
        }

        @Override
        protected SubstatementValidator getSubstatementValidator() {
            return SUBSTATEMENT_VALIDATOR;
        }
    }

    @Nonnull
    @Override
    public String getName() {
        return argument();
    }

}
