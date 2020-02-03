/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.min_elements;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MinElementsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MinElementsStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseInternedStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class MinElementsStatementSupport
        extends BaseInternedStatementSupport<Integer, MinElementsStatement, MinElementsEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.MIN_ELEMENTS)
        .build();
    private static final MinElementsStatementSupport INSTANCE = new MinElementsStatementSupport();

    private MinElementsStatementSupport() {
        super(YangStmtMapping.MIN_ELEMENTS);
    }

    public static MinElementsStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public Integer parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            throw new SourceException("Invalid min-elements argument", ctx.getStatementSourceReference(), e);
        }
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected MinElementsStatement createDeclared(final Integer argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularMinElementsStatement(argument, substatements);
    }

    @Override
    protected MinElementsStatement createEmptyDeclared(final Integer argument) {
        return new EmptyMinElementsStatement(argument);
    }

    @Override
    protected MinElementsEffectiveStatement createEffective(
            final StmtContext<Integer, MinElementsStatement, MinElementsEffectiveStatement> ctx,
            final MinElementsStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new RegularMinElementsEffectiveStatement(declared, substatements);
    }

    @Override
    protected MinElementsEffectiveStatement createEmptyEffective(final MinElementsStatement declared) {
        return new EmptyMinElementsEffectiveStatement(declared);
    }
}