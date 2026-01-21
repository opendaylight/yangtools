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
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractInternedStringStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class ReferenceStatementSupport
        extends AbstractInternedStringStatementSupport<ReferenceStatement, ReferenceEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
        SubstatementValidator.builder(ReferenceStatement.DEFINITION).build();

    public ReferenceStatementSupport(final YangParserConfiguration config) {
        super(ReferenceStatement.DEFINITION, StatementPolicy.contextIndependent(), config, SUBSTATEMENT_VALIDATOR);
    }

    @Override
    protected ReferenceStatement createDeclared(final String argument,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createReference(argument, substatements);
    }

    @Override
    protected ReferenceStatement attachDeclarationReference(final ReferenceStatement stmt,
            final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateReference(stmt, reference);
    }

    @Override
    protected ReferenceStatement createEmptyDeclared(final String argument) {
        return DeclaredStatements.createReference(argument);
    }

    @Override
    protected ReferenceEffectiveStatement createEffective(final ReferenceStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return EffectiveStatements.createReference(declared, substatements);
    }

    @Override
    protected ReferenceEffectiveStatement createEmptyEffective(final ReferenceStatement declared) {
        return EffectiveStatements.createReference(declared);
    }
}
