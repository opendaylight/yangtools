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
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractBooleanStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class MandatoryStatementSupport extends
        AbstractBooleanStatementSupport<MandatoryStatement, MandatoryEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
        SubstatementValidator.builder(MandatoryStatement.DEFINITION).build();

    public MandatoryStatementSupport(final YangParserConfiguration config) {
        super(MandatoryStatement.DEFINITION,
            EffectiveStatements.createMandatory(DeclaredStatements.createMandatory(Boolean.FALSE)),
            EffectiveStatements.createMandatory(DeclaredStatements.createMandatory(Boolean.TRUE)),
            StatementPolicy.contextIndependent(), config, SUBSTATEMENT_VALIDATOR);
    }

    @Override
    protected MandatoryStatement createDeclared(final Boolean argument,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createMandatory(argument, substatements);
    }

    @Override
    protected MandatoryStatement attachDeclarationReference(final MandatoryStatement stmt,
            final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateMandatory(stmt, reference);
    }

    @Override
    protected MandatoryEffectiveStatement createEffective(final MandatoryStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return EffectiveStatements.createMandatory(declared, substatements);
    }

    @Override
    protected MandatoryEffectiveStatement createEmptyEffective(final MandatoryStatement declared) {
        return EffectiveStatements.createMandatory(declared);
    }
}
