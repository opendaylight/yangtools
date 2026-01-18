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
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ConfigEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ConfigStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractBooleanStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class ConfigStatementSupport
        extends AbstractBooleanStatementSupport<ConfigStatement, ConfigEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
        SubstatementValidator.builder(YangStmtMapping.CONFIG).build();

    public ConfigStatementSupport(final YangParserConfiguration config) {
        super(YangStmtMapping.CONFIG,
            EffectiveStatements.createConfig(false), EffectiveStatements.createConfig(true),
            StatementPolicy.copyDeclared(
                (copy, current, substatements) -> copy.effectiveConfig() == current.effectiveConfig()),
            config, SUBSTATEMENT_VALIDATOR);
    }

    @Override
    public void onStatementAdded(final Mutable<Boolean, ConfigStatement, ConfigEffectiveStatement> stmt) {
        if (stmt.isTransientInstance()) {
            stmt.setUnsupported();
        }
    }

    @Override
    protected ConfigStatement createDeclared(final Boolean argument,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createConfig(argument, substatements);
    }

    @Override
    protected ConfigStatement attachDeclarationReference(final ConfigStatement stmt,
            final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateConfig(stmt, reference);
    }

    @Override
    protected ConfigEffectiveStatement createEffective(final ConfigStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return EffectiveStatements.createConfig(declared, substatements);
    }

    @Override
    protected ConfigEffectiveStatement createEmptyEffective(final ConfigStatement declared) {
        return EffectiveStatements.createConfig(declared);
    }
}
