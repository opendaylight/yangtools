/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.extension;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.ArgumentDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.UnrecognizedStatement;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.OverrideChildStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

final class UnrecognizedStatementSupport
        extends AbstractStatementSupport<Object, UnrecognizedStatement, UnrecognizedEffectiveStatement>
        implements OverrideChildStatementSupport {
    private final YangParserConfiguration config;

    UnrecognizedStatementSupport(final StatementDefinition publicDefinition, final YangParserConfiguration config) {
        // We have no idea about the statement's semantics, hence there should be noone interested in its semantics.
        // Nevertheless it may be of interest for various hacks to understand there was an extension involved.
        super(publicDefinition, StatementPolicy.exactReplica(), config);
        this.config = requireNonNull(config);
    }

    @Override
    public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return value;
    }

    @Override
    public UnrecognizedStatementSupport statementDefinitionOverrideOf(final StatementDefinition childDef) {
        /*
         * This code wraps statements encountered inside an extension so they do not get confused with regular
         * statements.
         */
        final QName baseQName = getStatementName();
        final QName statementName = QName.create(baseQName, childDef.getStatementName().getLocalName());

        final ModelDefinedStatementDefinition def;
        final Optional<ArgumentDefinition> optArgDef = childDef.getArgumentDefinition();
        if (optArgDef.isPresent()) {
            final ArgumentDefinition argDef = optArgDef.get();
            def = new ModelDefinedStatementDefinition(statementName, argDef.getArgumentName(), argDef.isYinElement());
        } else {
            def = new ModelDefinedStatementDefinition(statementName);
        }
        return new UnrecognizedStatementSupport(def, config);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        // We know nothing about this statement
        return null;
    }

    @Override
    protected UnrecognizedStatement createDeclared(final StmtContext<Object, UnrecognizedStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new UnrecognizedStatementImpl(ctx.rawArgument(), ctx.publicDefinition(), substatements);
    }

    @Override
    protected UnrecognizedStatement attachDeclarationReference(final UnrecognizedStatement stmt,
            final DeclarationReference reference) {
        return new RefUnrecognizedStatement(stmt, reference);
    }

    // createEffective() should never be called, ensure that for each declared statement

    @Override
    public void onStatementAdded(final Mutable<Object, UnrecognizedStatement, UnrecognizedEffectiveStatement> stmt) {
        stmt.setIsSupportedToBuildEffective(false);
    }

    @Override
    protected UnrecognizedEffectiveStatement createEffective(final Current<Object, UnrecognizedStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        throw new InferenceException(stmt, "Attempted to instantiate unrecognized effective statement %s",
            stmt.publicDefinition());
    }
}