/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.Decimal64Specification;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

final class Decimal64SpecificationSupport extends AbstractStatementSupport<String, Decimal64Specification,
        EffectiveStatement<String, Decimal64Specification>> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.TYPE)
        .addMandatory(YangStmtMapping.FRACTION_DIGITS)
        .addOptional(YangStmtMapping.RANGE)
        .build();

    Decimal64SpecificationSupport() {
        super(YangStmtMapping.TYPE);
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
    public EffectiveStatement<String, Decimal64Specification> createEffective(final StmtContext<String,
            Decimal64Specification, EffectiveStatement<String, Decimal64Specification>> ctx) {
        return new Decimal64SpecificationEffectiveStatement(ctx);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }
}