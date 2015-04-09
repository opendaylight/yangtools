/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceNotAvailableException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementFactory;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public class StatementDefinitionContext<A,D extends DeclaredStatement<A>,E extends EffectiveStatement<A,D>> {
    private final StatementSupport<A,D,E> support;
    public StatementDefinitionContext(StatementSupport<A,D,E> support) {
        this.support= support;
    }


    public StatementFactory<A,D,E> getFactory() {
        return support;
    }

    public A parseArgumentValue(StmtContext<A,D,E> context, String value) throws SourceException {
        return support.parseArgumentValue(context,value);
    }


    public void checkNamespaceAllowed(Class<? extends IdentifierNamespace<?,?>> namespace) throws NamespaceNotAvailableException {
        // Noop
    }

    public StatementDefinition getPublicView() {
        return support.getPublicView();
    }

    public boolean onStatementAdded(Mutable<A,D,E> stmt) {
        return false;
    }


    public void onDeclarationFinished(Mutable<A,D,E> statement, ModelProcessingPhase phase) throws SourceException {
        switch (phase) {
        case SourceLinkage:
            support.onLinkageDeclared(statement);
            break;
        case StatementDefinition:
            support.onStatementDefinitionDeclared(statement);
            break;
        case FullDeclaration:
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

}
