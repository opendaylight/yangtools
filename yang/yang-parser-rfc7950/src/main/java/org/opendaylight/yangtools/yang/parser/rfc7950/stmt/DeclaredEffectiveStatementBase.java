/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

/**
 * A declared {@link EffectiveStatementBase}.
 *
 * @deprecated Use {@link AbstractDeclaredEffectiveStatement} and its subclasses instead.
 */
@Deprecated(forRemoval = true)
// FIXME: 6.0.0: fold this into AbstractEffectiveDocumentedNodeWithStatus
public abstract class DeclaredEffectiveStatementBase<A, D extends DeclaredStatement<A>> extends
        EffectiveStatementBase<A, D> {

    private final @NonNull StatementSource statementSource;
    private final A argument;
    private final @NonNull D declaredInstance;

    /**
     * Constructor.
     *
     * @param ctx context of statement.
     */
    protected DeclaredEffectiveStatementBase(final StmtContext<A, D, ?> ctx) {
        super(ctx);
        argument = ctx.getStatementArgument();
        statementSource = ctx.getStatementSource();
        declaredInstance = ctx.buildDeclared();
    }

    @Override
    public final StatementDefinition statementDefinition() {
        return declaredInstance.statementDefinition();
    }

    @Override
    public A argument() {
        return argument;
    }

    @Override
    public final StatementSource getStatementSource() {
        return statementSource;
    }

    @Override
    public final D getDeclared() {
        return declaredInstance;
    }
}