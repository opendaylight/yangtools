/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MaxElementsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MaxElementsStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStringStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class MaxElementsStatementSupport
        extends AbstractStringStatementSupport<MaxElementsStatement, MaxElementsEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.MAX_ELEMENTS)
        .build();
    private static final MaxElementsStatementSupport INSTANCE = new MaxElementsStatementSupport();

    private MaxElementsStatementSupport() {
        super(YangStmtMapping.MAX_ELEMENTS, StatementPolicy.contextIndependent());
    }

    public static MaxElementsStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public String internArgument(final String rawArgument) {
        return "unbounded".equals(rawArgument) ? "unbounded" : rawArgument;
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected MaxElementsStatement createDeclared(final StmtContext<String, MaxElementsStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createMaxElements(ctx.getRawArgument(), ctx.getArgument(), substatements);
    }

    @Override
    protected MaxElementsEffectiveStatement createEffective(final Current<String, MaxElementsStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return EffectiveStatements.createMaxElements(stmt.declared(), substatements);
    }
}
