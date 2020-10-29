/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.output;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseOperationContainerStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

abstract class AbstractOutputStatementSupport
        extends BaseOperationContainerStatementSupport<OutputStatement, OutputEffectiveStatement> {
    AbstractOutputStatementSupport() {
        super(YangStmtMapping.OUTPUT, YangConstants::operationOutputQName);
    }

    @Override
    protected final OutputStatement createDeclared(final StmtContext<QName, OutputStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        final StatementSource source = ctx.getStatementSource();
        switch (ctx.getStatementSource()) {
            case CONTEXT:
                return new RegularUndeclaredOutputStatement(ctx.coerceStatementArgument(), substatements);
            case DECLARATION:
                return new RegularOutputStatement(ctx.coerceStatementArgument(), substatements);
            default:
                throw new IllegalStateException("Unhandled statement source " + source);
        }
    }

    @Override
    protected final OutputStatement createEmptyDeclared(final StmtContext<QName, OutputStatement, ?> ctx) {
        final StatementSource source = ctx.getStatementSource();
        switch (ctx.getStatementSource()) {
            case CONTEXT:
                return new EmptyUndeclaredOutputStatement(ctx.coerceStatementArgument());
            case DECLARATION:
                return new EmptyOutputStatement(ctx.coerceStatementArgument());
            default:
                throw new IllegalStateException("Unhandled statement source " + source);
        }
    }

    @Override
    protected final OutputEffectiveStatement createDeclaredEffective(final int flags,
            final StmtContext<QName, OutputStatement, OutputEffectiveStatement> ctx,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements, final OutputStatement declared) {
        return new DeclaredOutputEffectiveStatement(declared, flags, ctx, substatements);
    }

    @Override
    protected final OutputEffectiveStatement createUndeclaredEffective(final int flags,
            final StmtContext<QName, OutputStatement, OutputEffectiveStatement> ctx,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new UndeclaredOutputEffectiveStatement(flags, ctx, substatements);
    }
}
