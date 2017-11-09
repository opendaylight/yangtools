/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.ordered_by;

import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrderedByStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class OrderedByStatementSupport
        extends AbstractStatementSupport<String, OrderedByStatement, EffectiveStatement<String, OrderedByStatement>> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.ORDERED_BY)
        .build();

    public OrderedByStatementSupport() {
        super(YangStmtMapping.ORDERED_BY);
    }

    @Override
    public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return value;
    }

    @Override
    public OrderedByStatement createDeclared(final StmtContext<String, OrderedByStatement, ?> ctx) {
        return new OrderedByStatementImpl(ctx);
    }

    @Override
    public EffectiveStatement<String, OrderedByStatement> createEffective(
            final StmtContext<String, OrderedByStatement, EffectiveStatement<String, OrderedByStatement>> ctx) {
        return new OrderedByEffectiveStatementImpl(ctx);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    public String internArgument(final String rawArgument) {
        if ("user".equals(rawArgument)) {
            return "user";
        } else if ("system".equals(rawArgument)) {
            return "system";
        } else {
            return rawArgument;
        }
    }
}