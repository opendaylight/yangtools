/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.ordered_by;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrderedByEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrderedByStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStringStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class OrderedByStatementSupport
        extends BaseStringStatementSupport<OrderedByStatement, OrderedByEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
            SubstatementValidator.builder(YangStmtMapping.ORDERED_BY).build();
    private static final OrderedByStatementSupport INSTANCE = new OrderedByStatementSupport();

    private OrderedByStatementSupport() {
        super(YangStmtMapping.ORDERED_BY);
    }

    public static OrderedByStatementSupport getInstance() {
        return INSTANCE;
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
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected OrderedByEffectiveStatement createEffective(
            final StmtContext<String, OrderedByStatement, OrderedByEffectiveStatement> ctx,
            final OrderedByStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new RegularOrderedByEffectiveStatement(declared, substatements);
    }

    @Override
    protected OrderedByEffectiveStatement createEmptyEffective(
            final StmtContext<String, OrderedByStatement, OrderedByEffectiveStatement> ctx,
            final OrderedByStatement declared) {
        return new EmptyOrderedByEffectiveStatement(declared);
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