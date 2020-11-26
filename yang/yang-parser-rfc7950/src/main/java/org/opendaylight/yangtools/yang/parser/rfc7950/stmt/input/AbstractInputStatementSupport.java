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
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.SubstatementIndexingException;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

abstract class AbstractInputStatementSupport
        extends BaseOperationContainerStatementSupport<InputStatement, InputEffectiveStatement> {
    AbstractInputStatementSupport() {
        super(YangStmtMapping.INPUT, YangConstants::operationInputQName);
    }

    @Override
    protected final InputStatement createDeclared(final StmtContext<QName, InputStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        final StatementSource source = ctx.source();
        switch (source) {
            case CONTEXT:
                return new RegularUndeclaredInputStatement(ctx.getArgument(), substatements);
            case DECLARATION:
                return new RegularInputStatement(ctx.getArgument(), substatements);
            default:
                throw new IllegalStateException("Unhandled statement source " + source);
        }
    }

    @Override
    protected final InputStatement createEmptyDeclared(final StmtContext<QName, InputStatement, ?> ctx) {
        final StatementSource source = ctx.source();
        switch (source) {
            case CONTEXT:
                return new EmptyUndeclaredInputStatement(ctx.getArgument());
            case DECLARATION:
                return new EmptyInputStatement(ctx.getArgument());
            default:
                throw new IllegalStateException("Unhandled statement source " + source);
        }
    }

    @Override
    protected final InputEffectiveStatement createDeclaredEffective(final int flags,
            final Current<QName, InputStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new DeclaredInputEffectiveStatement(flags, stmt.declared(), substatements, stmt.wrapSchemaPath());
    }

    @Override
    protected final InputEffectiveStatement createUndeclaredEffective(final int flags,
            final Current<QName, InputStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        try {
            return new UndeclaredInputEffectiveStatement(flags, substatements, stmt.wrapSchemaPath());
        } catch (SubstatementIndexingException e) {
            throw new SourceException(e.getMessage(), stmt.sourceReference(), e);
        }
    }
}
