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
import org.opendaylight.yangtools.yang.model.api.stmt.RequireInstanceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RequireInstanceStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractBooleanStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class RequireInstanceStatementSupport
        extends AbstractBooleanStatementSupport<RequireInstanceStatement, RequireInstanceEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
        SubstatementValidator.builder(RequireInstanceStatement.DEFINITION).build();

    public RequireInstanceStatementSupport(final YangParserConfiguration config) {
        super(RequireInstanceStatement.DEFINITION,
            EffectiveStatements.createRequireInstance(false), EffectiveStatements.createRequireInstance(true),
            StatementPolicy.contextIndependent(), config, SUBSTATEMENT_VALIDATOR);
    }

    @Override
    protected RequireInstanceStatement createDeclared(final Boolean argument,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createRequireInstance(argument, substatements);
    }

    @Override
    protected RequireInstanceStatement attachDeclarationReference(final RequireInstanceStatement stmt,
            final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateRequireInstance(stmt, reference);
    }

    @Override
    protected RequireInstanceEffectiveStatement createEffective(final RequireInstanceStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return EffectiveStatements.createRequireInstance(declared, substatements);
    }

    @Override
    protected RequireInstanceEffectiveStatement createEmptyEffective(final RequireInstanceStatement declared) {
        return EffectiveStatements.createRequireInstance(declared);
    }
}
