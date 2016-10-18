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
import org.opendaylight.yangtools.yang.model.api.stmt.FractionDigitsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RangeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.Decimal64Specification;
import org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.Decimal64SpecificationEffectiveStatementImpl;

public class Decimal64SpecificationImpl extends AbstractDeclaredStatement<String> implements Decimal64Specification {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(Rfc6020Mapping
            .TYPE)
            .addMandatory(Rfc6020Mapping.FRACTION_DIGITS)
            .addOptional(Rfc6020Mapping.RANGE)
            .build();

    protected Decimal64SpecificationImpl(final StmtContext<String, Decimal64Specification, ?> context) {
        super(context);
    }

    public static class Definition extends
            AbstractStatementSupport<String, Decimal64Specification, EffectiveStatement<String, Decimal64Specification>> {

        public Definition() {
            super(Rfc6020Mapping.TYPE);
        }

        @Override
        public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            return value;
        }

        @Override
        public Decimal64Specification createDeclared(final StmtContext<String, Decimal64Specification, ?> ctx) {
            return new Decimal64SpecificationImpl(ctx);
        }

        @Override
        public EffectiveStatement<String, Decimal64Specification> createEffective(
                final StmtContext<String, Decimal64Specification, EffectiveStatement<String, Decimal64Specification>> ctx) {
            return new Decimal64SpecificationEffectiveStatementImpl(ctx);
        }

        @Override
        public void onFullDefinitionDeclared(final StmtContext.Mutable<String, Decimal64Specification,
                EffectiveStatement<String, Decimal64Specification>> stmt) {
            super.onFullDefinitionDeclared(stmt);
            SUBSTATEMENT_VALIDATOR.validate(stmt);
        }
    }

    @Override
    public String getName() {
        return argument();
    }

    @Override
    public FractionDigitsStatement getFractionDigits() {
        return firstDeclared(FractionDigitsStatement.class);
    }

    @Override
    public RangeStatement getRange() {
        return firstDeclared(RangeStatement.class);
    }

}
