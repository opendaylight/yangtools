/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.value;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseInternedStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class ValueStatementSupport
        extends BaseInternedStatementSupport<Integer, ValueStatement, ValueEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.VALUE).build();
    private static final ValueStatementSupport INSTANCE = new ValueStatementSupport();

    private ValueStatementSupport() {
        super(YangStmtMapping.VALUE);
    }

    public static ValueStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public Integer parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            throw new SourceException(ctx.getStatementSourceReference(), e,
                "%s is not valid value statement integer argument in a range of -2147483648..2147483647", value);
        }
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected ValueStatement createDeclared(final Integer argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularValueStatement(argument, substatements);
    }

    @Override
    protected ValueStatement createEmptyDeclared(@NonNull final Integer argument) {
        return new EmptyValueStatement(argument);
    }

    @Override
    protected ValueEffectiveStatement createEmptyEffective(@NonNull final ValueStatement declared) {
        return new EmptyValueEffectiveStatement(declared);
    }

    @Override
    protected ValueEffectiveStatement createEffective(
            final StmtContext<Integer, ValueStatement, ValueEffectiveStatement> ctx, final ValueStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new RegularValueEffectiveStatement(declared, substatements);
    }
}
