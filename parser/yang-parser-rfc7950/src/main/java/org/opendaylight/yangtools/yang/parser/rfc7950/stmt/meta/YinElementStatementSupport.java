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
import org.opendaylight.yangtools.yang.model.api.stmt.YinElementEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YinElementStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractBooleanStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class YinElementStatementSupport
        extends AbstractBooleanStatementSupport<YinElementStatement, YinElementEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
        SubstatementValidator.builder(YinElementStatement.DEFINITION).build();

    public YinElementStatementSupport(final YangParserConfiguration config) {
        super(YinElementStatement.DEFINITION,
            EffectiveStatements.createYinElement(DeclaredStatements.createYinElement(false)),
            EffectiveStatements.createYinElement(DeclaredStatements.createYinElement(true)),
            StatementPolicy.reject(), config, SUBSTATEMENT_VALIDATOR);
    }

    @Override
    protected YinElementStatement createDeclared(final Boolean argument,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createYinElement(argument, substatements);
    }

    @Override
    protected YinElementStatement attachDeclarationReference(final YinElementStatement stmt,
            final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateYinElement(stmt, reference);
    }

    @Override
    protected YinElementEffectiveStatement createEffective(final YinElementStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return EffectiveStatements.createYinElement(declared, substatements);
    }

    @Override
    protected YinElementEffectiveStatement createEmptyEffective(final YinElementStatement declared) {
        return EffectiveStatements.createYinElement(declared);
    }
}
