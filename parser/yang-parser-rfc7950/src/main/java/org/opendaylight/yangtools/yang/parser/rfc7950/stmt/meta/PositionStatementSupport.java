/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PositionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PositionStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractInternedStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class PositionStatementSupport
        extends AbstractInternedStatementSupport<Uint32, PositionStatement, PositionEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
        SubstatementValidator.builder(PositionStatement.DEFINITION).build();

    public PositionStatementSupport(final YangParserConfiguration config) {
        super(PositionStatement.DEFINITION, StatementPolicy.contextIndependent(), config, SUBSTATEMENT_VALIDATOR);
    }

    @Override
    public Uint32 parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        try {
            return Uint32.valueOf(value).intern();
        } catch (IllegalArgumentException e) {
            throw new SourceException(ctx, e, "Bit position value %s is not valid integer", value);
        }
    }

    @Override
    protected PositionStatement createDeclared(final Uint32 argument,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createPosition(argument, substatements);
    }

    @Override
    protected PositionStatement createEmptyDeclared(final Uint32 argument) {
        return DeclaredStatements.createPosition(argument, ImmutableList.of());
    }

    @Override
    protected PositionStatement attachDeclarationReference(final PositionStatement stmt,
            final DeclarationReference reference) {
        return DeclaredStatementDecorators.decoratePosition(stmt, reference);
    }

    @Override
    protected PositionEffectiveStatement createEffective(final PositionStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return EffectiveStatements.createPosition(declared, substatements);
    }

    @Override
    protected PositionEffectiveStatement createEmptyEffective(final PositionStatement declared) {
        return EffectiveStatements.createPosition(declared);
    }
}
