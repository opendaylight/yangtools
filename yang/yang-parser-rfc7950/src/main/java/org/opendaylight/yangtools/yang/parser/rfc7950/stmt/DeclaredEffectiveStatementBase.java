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

public abstract class DeclaredEffectiveStatementBase<A, D extends DeclaredStatement<A>> extends
        EffectiveStatementBase<A, D> {

    private final @NonNull StatementSource statementSource;
    private final A argument;
    private final @NonNull D declaredInstance;

    /**
     * Constructor.
     *
     * @param ctx
     *            context of statement.
     */
    protected DeclaredEffectiveStatementBase(final StmtContext<A, D, ?> ctx) {
        super(ctx);

        this.argument = ctx.getStatementArgument();
        this.statementSource = ctx.getStatementSource();

        /*
         * Share original instance of declared statement between all effective
         * statements which have been copied or derived from this original
         * declared statement.
         */
        @SuppressWarnings("unchecked")
        final StmtContext<?, D, ?> lookupCtx = (StmtContext<?, D, ?>) ctx.getOriginalCtx().orElse(ctx);
        declaredInstance = Verify.verifyNotNull(lookupCtx.buildDeclared(),
            "Statement %s failed to build declared statement", lookupCtx);
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