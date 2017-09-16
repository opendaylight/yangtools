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
import org.opendaylight.yangtools.yang.model.api.stmt.RangeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.NumericalRestrictions;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.NumericalRestrictionsEffectiveStatementImpl;

public class NumericalRestrictionsImpl extends AbstractDeclaredStatement<String> implements NumericalRestrictions {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
            .TYPE)
            .addMandatory(YangStmtMapping.RANGE)
            .build();

    protected NumericalRestrictionsImpl(final StmtContext<String, NumericalRestrictions, ?> context) {
        super(context);
    }

    public static class Definition extends
            AbstractStatementSupport<String, NumericalRestrictions, EffectiveStatement<String, NumericalRestrictions>> {

        public Definition() {
            super(YangStmtMapping.TYPE);
        }

        @Override
        public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            return value;
        }

        @Override
        public NumericalRestrictions createDeclared(final StmtContext<String, NumericalRestrictions, ?> ctx) {
            return new NumericalRestrictionsImpl(ctx);
        }

        @Override
        public EffectiveStatement<String, NumericalRestrictions> createEffective(
                final StmtContext<String, NumericalRestrictions,
                EffectiveStatement<String, NumericalRestrictions>> ctx) {
            return new NumericalRestrictionsEffectiveStatementImpl(ctx);
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

    @Nonnull
    @Override
    public RangeStatement getRange() {
        return firstDeclared(RangeStatement.class);
    }
}
