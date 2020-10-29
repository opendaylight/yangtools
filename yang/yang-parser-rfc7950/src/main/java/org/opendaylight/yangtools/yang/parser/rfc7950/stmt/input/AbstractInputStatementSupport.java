/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.input;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.model.api.stmt.InputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.InputStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseOperationContainerStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

abstract class AbstractInputStatementSupport
        extends BaseOperationContainerStatementSupport<InputStatement, InputEffectiveStatement> {
    AbstractInputStatementSupport() {
        super(YangStmtMapping.INPUT, YangConstants::operationInputQName);
    }

    @Override
    protected final InputStatement createDeclared(final StmtContext<QName, InputStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        final StatementSource source = ctx.getStatementSource();
        switch (ctx.getStatementSource()) {
            case CONTEXT:
                return new RegularUndeclaredInputStatement(ctx.coerceStatementArgument(), substatements);
            case DECLARATION:
                return new RegularInputStatement(ctx.coerceStatementArgument(), substatements);
            default:
                throw new IllegalStateException("Unhandled statement source " + source);
        }
    }

    @Override
    protected final InputStatement createEmptyDeclared(final StmtContext<QName, InputStatement, ?> ctx) {
        final StatementSource source = ctx.getStatementSource();
        switch (ctx.getStatementSource()) {
            case CONTEXT:
                return new EmptyUndeclaredInputStatement(ctx.coerceStatementArgument());
            case DECLARATION:
                return new EmptyInputStatement(ctx.coerceStatementArgument());
            default:
                throw new IllegalStateException("Unhandled statement source " + source);
        }
    }

    @Override
    protected final InputEffectiveStatement createDeclaredEffective(final int flags,
            final StmtContext<QName, InputStatement, InputEffectiveStatement> ctx,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements,
            final InputStatement declared) {
        return new DeclaredInputEffectiveStatement(declared, flags, ctx, substatements);
    }

    @Override
    protected final InputEffectiveStatement createUndeclaredEffective(final int flags,
            final StmtContext<QName, InputStatement, InputEffectiveStatement> ctx,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new UndeclaredInputEffectiveStatement(flags, ctx, substatements);
    }
}
