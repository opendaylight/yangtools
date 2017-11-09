/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.min_elements;

import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MinElementsStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class MinElementsStatementSupport extends
        AbstractStatementSupport<Integer, MinElementsStatement, EffectiveStatement<Integer, MinElementsStatement>> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.MIN_ELEMENTS)
        .build();

    public MinElementsStatementSupport() {
        super(YangStmtMapping.MIN_ELEMENTS);
    }

    @Override
    public Integer parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return Integer.parseInt(value);
    }

    @Override
    public MinElementsStatement createDeclared(
            final StmtContext<Integer, MinElementsStatement, ?> ctx) {
        return new MinElementsStatementImpl(ctx);
    }

    @Override
    public EffectiveStatement<Integer, MinElementsStatement> createEffective(
            final StmtContext<Integer, MinElementsStatement,
            EffectiveStatement<Integer, MinElementsStatement>> ctx) {
        return new MinElementsEffectiveStatementImpl(ctx);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }
}