/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementFactory;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;

public class StatementDefinitionContext<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>> {
    private final StatementSupport<A, D, E> support;
    private final Map<String, StatementDefinitionContext<?, ?, ?>> argumentSpecificSubDefinitions;
    private Map<StatementDefinitionContext<?,?,?>, StatementDefinitionContext<?,?,?>> unknownStmtDefsOfYangStmts;

    public StatementDefinitionContext(final StatementSupport<A, D, E> support) {
        this.support = Preconditions.checkNotNull(support);
        this.argumentSpecificSubDefinitions = support.hasArgumentSpecificSupports() ? new HashMap<>() : null;
    }

    public StatementFactory<A,D,E> getFactory() {
        return support;
    }

    public A parseArgumentValue(final StmtContext<A, D, E> context, final String value) {
        return support.parseArgumentValue(context,value);
    }

    public void checkNamespaceAllowed(final Class<? extends IdentifierNamespace<?,?>> namespace) {
        // Noop
    }

    public StatementDefinition getPublicView() {
        return support.getPublicView();
    }

    public Optional<StatementContextBase<?, ?, ?>> beforeSubStatementCreated(final Mutable<?, ?, ?> stmt, final int offset, final StatementDefinitionContext<?, ?, ?> def, final StatementSourceReference ref,
            final String argument) {
        return support.beforeSubStatementCreated(stmt, offset, def, ref, argument);
    }

    public boolean onStatementAdded(final Mutable<A, D, E> stmt) {
        support.onStatementAdded(stmt);
        return false;
    }


    public void onDeclarationFinished(final Mutable<A, D, E> statement, final ModelProcessingPhase phase) {
        switch (phase) {
        case SOURCE_PRE_LINKAGE:
            support.onPreLinkageDeclared(statement);
            break;
        case SOURCE_LINKAGE:
            support.onLinkageDeclared(statement);
            break;
        case STATEMENT_DEFINITION:
            support.onStatementDefinitionDeclared(statement);
            break;
        case FULL_DECLARATION:
            support.onFullDefinitionDeclared(statement);
            break;
        default:
            break;
        }
    }

    public Class<?> getRepresentingClass() {
        return support.getDeclaredRepresentationClass();
    }

    public boolean hasArgument() {
        return support.getArgumentName() != null;
    }

    public boolean isArgumentYinElement() {
        return support.isArgumentYinElement();
    }

    public QName getStatementName() {
        return support.getStatementName();
    }

    public QName getArgumentName() {
        return support.getArgumentName();
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this).omitNullValues()).toString();
    }

    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return toStringHelper.add("statement", getStatementName());
    }

    @Nonnull
    StatementDefinitionContext<?, ?, ?> getSubDefinitionSpecificForArgument(final String argument) {
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

    boolean hasArgumentSpecificSubDefinitions() {
        return support.hasArgumentSpecificSupports();
    }

    String internArgument(final String rawArgument) {
        return support.internArgument(rawArgument);
    }

    StatementDefinitionContext<?, ?, ?> getAsUnknownStatementDefinition(
            final StatementDefinitionContext<?, ?, ?> yangStmtDef) {
        if (unknownStmtDefsOfYangStmts == null) {
            unknownStmtDefsOfYangStmts = new HashMap<>();
        }

        StatementDefinitionContext<?, ?, ?> ret = unknownStmtDefsOfYangStmts.get(yangStmtDef);
        if (ret != null) {
            return ret;
        }

        ret = support.getUnknownStatementDefinitionOf(yangStmtDef).orElse(null);

        if (ret != null) {
            unknownStmtDefsOfYangStmts.put(yangStmtDef, ret);
        }
        return ret;
    }

    boolean isIgnoringIfFeatures() {
        return support.isIgnoringIfFeatures();
    }

    public boolean isIgnoringConfig() {
        return support.isIgnoringConfig();
    }
}
