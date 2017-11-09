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
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.InstanceIdentifierSpecification;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

final class InstanceIdentifierSpecificationSupport extends AbstractStatementSupport<String,
        InstanceIdentifierSpecification, EffectiveStatement<String, InstanceIdentifierSpecification>> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.TYPE)
        .addOptional(YangStmtMapping.REQUIRE_INSTANCE)
        .build();

    InstanceIdentifierSpecificationSupport() {
        super(YangStmtMapping.TYPE);
    }

    @Override
    public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return value;
    }

    @Override
    public InstanceIdentifierSpecification createDeclared(
            final StmtContext<String, InstanceIdentifierSpecification, ?> ctx) {
        return new InstanceIdentifierSpecificationImpl(ctx);
    }

    @Override
    public EffectiveStatement<String, InstanceIdentifierSpecification> createEffective(
            final StmtContext<String, InstanceIdentifierSpecification,
            EffectiveStatement<String, InstanceIdentifierSpecification>> ctx) {
        return new InstanceIdentifierSpecificationEffectiveStatement(ctx);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }
}