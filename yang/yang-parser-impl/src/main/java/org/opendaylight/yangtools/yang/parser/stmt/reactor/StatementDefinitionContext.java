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

public class StatementDefinitionContext<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>> {
    private final StatementSupport<A, D, E> support;

    public StatementDefinitionContext(final StatementSupport<A, D, E> support) {
        this.support = Preconditions.checkNotNull(support);
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

    public QName getStatementName() {
        return support.getStatementName();
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this).omitNullValues()).toString();
    }

    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return toStringHelper.add("statement", getStatementName());
    }
}
