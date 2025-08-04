/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.meta.ArgumentDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.ImplicitParentAwareStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.OverrideChildStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementFactory;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;

final class StatementDefinitionContext<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>> {
    private final @NonNull StatementSupport<A, D, E> support;
    private final Map<String, StatementDefinitionContext<?, ?, ?>> argumentSpecificSubDefinitions;

    private Map<StatementDefinitionContext<?, ?, ?>, StatementDefinitionContext<?,?,?>> unknownStmtDefsOfYangStmts;

    StatementDefinitionContext(final StatementSupport<A, D, E> support) {
        this.support = requireNonNull(support);
        argumentSpecificSubDefinitions = support.hasArgumentSpecificSupports() ? new HashMap<>() : null;
    }

    @NonNull StatementFactory<A, D, E> getFactory() {
        return support;
    }

    A parseArgumentValue(final @NonNull StmtContext<A, D, E> context, final String value) {
        return support.parseArgumentValue(context, value);
    }

    A adaptArgumentValue(final @NonNull StmtContext<A, D, E> context, final QNameModule targetModule) {
        return support.adaptArgumentValue(context, targetModule);
    }

    @NonNull StatementDefinition getPublicView() {
        return support.getPublicView();
    }

    Optional<StatementSupport<?, ?, ?>> getImplicitParentFor(final NamespaceStmtCtx parent,
            final StatementDefinition stmtDef) {
        return support instanceof ImplicitParentAwareStatementSupport implicit
                ? implicit.getImplicitParentFor(parent, stmtDef) : Optional.empty();
    }

    void onStatementAdded(final @NonNull Mutable<A, D, E> stmt) {
        support.onStatementAdded(stmt);
    }

    void onDeclarationFinished(final @NonNull Mutable<A, D, E> statement, final ModelProcessingPhase phase) {
        switch (phase) {
            case STATEMENT_DEFINITION -> support.onStatementDefinitionDeclared(statement);
            case FULL_DECLARATION -> support.onFullDefinitionDeclared(statement);
            default -> {
                // No-op
            }
        }
    }

    @NonNull Optional<ArgumentDefinition> getArgumentDefinition() {
        return support.getArgumentDefinition();
    }

    @NonNull QName getStatementName() {
        return support.statementName();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("statement", getStatementName()).toString();
    }

    @NonNull StatementDefinitionContext<?, ?, ?> getSubDefinitionSpecificForArgument(final String argument) {
        if (!hasArgumentSpecificSubDefinitions()) {
            return this;
        }

        StatementDefinitionContext<?, ?, ?> potential = argumentSpecificSubDefinitions.get(argument);
        if (potential == null) {
            final StatementSupport<?, ?, ?> argumentSpecificSupport = support.getSupportSpecificForArgument(argument);
            potential = argumentSpecificSupport != null ? new StatementDefinitionContext<>(argumentSpecificSupport)
                    : this;
            argumentSpecificSubDefinitions.put(argument, potential);
        }

        return potential;
    }

    StatementSupport<A, D, E> support() {
        return support;
    }

    boolean hasArgumentSpecificSubDefinitions() {
        return support.hasArgumentSpecificSupports();
    }

    @NonNull StatementDefinitionContext<?, ?, ?> overrideDefinition(
            final @NonNull StatementDefinitionContext<?, ?, ?> def) {
        if (!(support instanceof OverrideChildStatementSupport overrideSupport)) {
            return def;
        }

        if (unknownStmtDefsOfYangStmts != null) {
            final StatementDefinitionContext<?, ?, ?> existing = unknownStmtDefsOfYangStmts.get(def);
            if (existing != null) {
                return existing;
            }
        } else {
            unknownStmtDefsOfYangStmts = new HashMap<>(4);
        }

        final var override = overrideSupport.statementDefinitionOverrideOf(def.getPublicView());
        final StatementDefinitionContext<?, ?, ?> ret;
        if (override != null) {
            ret = new StatementDefinitionContext<>(override);
        } else {
            ret = def;
        }
        unknownStmtDefsOfYangStmts.put(def, ret);
        return ret;
    }
}
