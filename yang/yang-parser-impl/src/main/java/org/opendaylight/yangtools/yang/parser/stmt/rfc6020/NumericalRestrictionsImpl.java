/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RangeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.NumericalRestrictionsEffectiveStatementImpl;

public class NumericalRestrictionsImpl extends AbstractDeclaredStatement<String> implements
        TypeStatement.NumericalRestrictions {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(Rfc6020Mapping
            .TYPE)
            .addMandatory(Rfc6020Mapping.RANGE)
            .build();

    protected NumericalRestrictionsImpl(final StmtContext<String, TypeStatement.NumericalRestrictions, ?> context) {
        super(context);
    }

    public static class Definition extends
            AbstractStatementSupport<String, TypeStatement.NumericalRestrictions, EffectiveStatement<String, TypeStatement.NumericalRestrictions>> {

        public Definition() {
            super(Rfc6020Mapping.TYPE);
        }

        @Override
        public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            return value;
        }

        @Override
        public TypeStatement.NumericalRestrictions createDeclared(
                final StmtContext<String, TypeStatement.NumericalRestrictions, ?> ctx) {
            return new NumericalRestrictionsImpl(ctx);
        }

        @Override
        public EffectiveStatement<String, TypeStatement.NumericalRestrictions> createEffective(
                final StmtContext<String, TypeStatement.NumericalRestrictions, EffectiveStatement<String, TypeStatement.NumericalRestrictions>> ctx) {
            return new NumericalRestrictionsEffectiveStatementImpl(ctx);
        }

        @Override
        public void onFullDefinitionDeclared(final StmtContext.Mutable<String, NumericalRestrictions,
                EffectiveStatement<String, NumericalRestrictions>> stmt) {
            super.onFullDefinitionDeclared(stmt);
            SUBSTATEMENT_VALIDATOR.validate(stmt);
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
