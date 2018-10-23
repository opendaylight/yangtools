/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import com.google.common.base.Verify;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public abstract class UndeclaredEffectiveStatementBase<A, D extends DeclaredStatement<A>>
        extends EffectiveStatementBase<A, D> {

    private final @NonNull StatementSource statementSource;
    private final @NonNull StatementDefinition statementDefinition;
    private final A argument;

    protected UndeclaredEffectiveStatementBase(final StmtContext<A, D, ?> ctx) {
        super(ctx);
        this.statementDefinition = ctx.getPublicDefinition();
        this.argument = ctx.getStatementArgument();
        this.statementSource = ctx.getStatementSource();

        final D declareInstance = ctx.buildDeclared();
        Verify.verify(declareInstance == null, "Statement %s resulted in declared statement %s", declareInstance);
    }

    @Override
    public final StatementDefinition statementDefinition() {
        return statementDefinition;
    }

    @Override
    public final A argument() {
        return argument;
    }

    @Override
    public final StatementSource getStatementSource() {
        return statementSource;
    }

    @Override
    public final D getDeclared() {
        return null;
    }
}
