/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractInternedStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class ValueStatementSupport
        extends AbstractInternedStatementSupport<Integer, ValueStatement, ValueEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
        SubstatementValidator.builder(ValueStatement.DEFINITION).build();

    public ValueStatementSupport(final YangParserConfiguration config) {
        super(ValueStatement.DEFINITION, StatementPolicy.contextIndependent(), config, SUBSTATEMENT_VALIDATOR);
    }

    @Override
    public Integer parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            throw new SourceException(ctx, e,
                "%s is not valid value statement integer argument in a range of -2147483648..2147483647", value);
        }
    }

    @Override
    protected ValueStatement createDeclared(final Integer argument,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createValue(argument, substatements);
    }

    @Override
    protected ValueStatement attachDeclarationReference(final ValueStatement stmt,
            final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateValue(stmt, reference);
    }

    @Override
    protected ValueStatement createEmptyDeclared(final Integer argument) {
        return DeclaredStatements.createValue(argument);
    }

    @Override
    protected ValueEffectiveStatement createEffective(final ValueStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return EffectiveStatements.createValue(declared, substatements);
    }

    @Override
    protected ValueEffectiveStatement createEmptyEffective(final ValueStatement declared) {
        return EffectiveStatements.createValue(declared);
    }
}
