/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.extension;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.ArgumentDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.UnrecognizedEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnrecognizedStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

final class UnrecognizedStatementSupport
        extends BaseStatementSupport<String, UnrecognizedStatement, UnrecognizedEffectiveStatement> {
    UnrecognizedStatementSupport(final StatementDefinition publicDefinition) {
        super(publicDefinition);
    }

    @Override
    public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return value;
    }

    @Override
    public Optional<StatementSupport<?, ?, ?>> getUnknownStatementDefinitionOf(
            final StatementDefinition yangStmtDef) {
        final QName baseQName = getStatementName();
        final QName statementName = QName.create(baseQName, yangStmtDef.getStatementName().getLocalName());

        final ModelDefinedStatementDefinition def;
        final Optional<ArgumentDefinition> optArgDef = yangStmtDef.getArgumentDefinition();
        if (optArgDef.isPresent()) {
            final ArgumentDefinition argDef = optArgDef.get();
            def = new ModelDefinedStatementDefinition(statementName, argDef.getArgumentName(), argDef.isYinElement());
        } else {
            def = new ModelDefinedStatementDefinition(statementName);
        }
        return Optional.of(new ModelDefinedStatementSupport(def));
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return null;
    }

    @Override
    protected UnrecognizedStatement createDeclared(final StmtContext<String, UnrecognizedStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new UnrecognizedStatementImpl(ctx.rawStatementArgument(), ctx.getPublicDefinition(), substatements);
    }

    @Override
    protected UnrecognizedStatement createEmptyDeclared(final StmtContext<String, UnrecognizedStatement, ?> ctx) {
        return createDeclared(ctx, ImmutableList.of());
    }

    @Override
    protected UnrecognizedEffectiveStatement createEffective(
            final StmtContext<String, UnrecognizedStatement, UnrecognizedEffectiveStatement> ctx,
            final UnrecognizedStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new UnrecognizedEffectiveStatementImpl(declared, substatements, ctx);
    }

    @Override
    protected UnrecognizedEffectiveStatement createEmptyEffective(
            final StmtContext<String, UnrecognizedStatement, UnrecognizedEffectiveStatement> ctx,
            final UnrecognizedStatement declared) {
        return createEffective(ctx, declared, ImmutableList.of());
    }

}