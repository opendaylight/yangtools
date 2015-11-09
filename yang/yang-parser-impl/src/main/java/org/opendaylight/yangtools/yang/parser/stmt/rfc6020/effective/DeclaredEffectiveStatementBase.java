/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import com.google.common.base.Verify;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public abstract class DeclaredEffectiveStatementBase<A, D extends DeclaredStatement<A>>
        extends EffectiveStatementBase<A, D> {

    private final D declaredInstance;

    public DeclaredEffectiveStatementBase(final StmtContext<A, D, ?> ctx) {
        super(ctx);
        declaredInstance = Verify.verifyNotNull(ctx.buildDeclared(), "Statement %s failed to build declared statement",
            ctx);
    }

    @Override
    public final StatementDefinition statementDefinition() {
        return declaredInstance.statementDefinition();
    }

    @Override
    public final A argument() {
        return declaredInstance.argument();
    }

    @Override
    public final StatementSource getStatementSource() {
        return declaredInstance.getStatementSource();
    }

    @Override
    public final D getDeclared() {
        return declaredInstance;
    }
}
