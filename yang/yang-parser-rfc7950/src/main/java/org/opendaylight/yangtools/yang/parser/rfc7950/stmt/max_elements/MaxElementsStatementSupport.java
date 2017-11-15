/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.max_elements;

import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MaxElementsStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class MaxElementsStatementSupport extends
        AbstractStatementSupport<String, MaxElementsStatement, EffectiveStatement<String, MaxElementsStatement>> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.MAX_ELEMENTS)
        .build();
    private static final MaxElementsStatementSupport INSTANCE = new MaxElementsStatementSupport();

    private MaxElementsStatementSupport() {
        super(YangStmtMapping.MAX_ELEMENTS);
    }

    public static MaxElementsStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return value;
    }

    @Override
    public MaxElementsStatement createDeclared(
            final StmtContext<String, MaxElementsStatement, ?> ctx) {
        return new MaxElementsStatementImpl(ctx);
    }

    @Override
    public EffectiveStatement<String, MaxElementsStatement> createEffective(
            final StmtContext<String, MaxElementsStatement, EffectiveStatement<String, MaxElementsStatement>> ctx) {
        return new MaxElementsEffectiveStatementImpl(ctx);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }
}