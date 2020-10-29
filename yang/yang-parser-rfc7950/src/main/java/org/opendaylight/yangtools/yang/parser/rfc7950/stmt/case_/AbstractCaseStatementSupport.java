/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.case_;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseImplicitStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.EffectiveStatementWithFlags.FlagsBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

abstract class AbstractCaseStatementSupport
        extends BaseImplicitStatementSupport<CaseStatement, CaseEffectiveStatement> {
    AbstractCaseStatementSupport() {
        super(YangStmtMapping.CASE);
    }

    @Override
    protected final CaseStatement createDeclared(final StmtContext<QName, CaseStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        final StatementSource source = ctx.getStatementSource();
        switch (ctx.getStatementSource()) {
            case CONTEXT:
                return new RegularUndeclaredCaseStatement(ctx.coerceStatementArgument(), substatements);
            case DECLARATION:
                return new RegularCaseStatement(ctx.coerceStatementArgument(), substatements);
            default:
                throw new IllegalStateException("Unhandled statement source " + source);
        }
    }

    @Override
    protected final CaseStatement createEmptyDeclared(final StmtContext<QName, CaseStatement, ?> ctx) {
        final StatementSource source = ctx.getStatementSource();
        switch (ctx.getStatementSource()) {
            case CONTEXT:
                return new EmptyUndeclaredCaseStatement(ctx.coerceStatementArgument());
            case DECLARATION:
                return new EmptyCaseStatement(ctx.coerceStatementArgument());
            default:
                throw new IllegalStateException("Unhandled statement source " + source);
        }
    }

    @Override
    protected final CaseEffectiveStatement createDeclaredEffective(
            final StmtContext<QName, CaseStatement, CaseEffectiveStatement> ctx,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements, final CaseStatement declared) {
        return new DeclaredCaseEffectiveStatement(declared, ctx, substatements, computeFlags(ctx, substatements),
            findOriginal(ctx));
    }

    @Override
    protected final CaseEffectiveStatement createUndeclaredEffective(
            final StmtContext<QName, CaseStatement, CaseEffectiveStatement> ctx,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new UndeclaredCaseEffectiveStatement(ctx, substatements, computeFlags(ctx, substatements),
            findOriginal(ctx));
    }

    private static @Nullable CaseSchemaNode findOriginal(final StmtContext<?, ?, ?> ctx) {
        return (CaseSchemaNode) ctx.getOriginalCtx().map(StmtContext::buildEffective).orElse(null);
    }

    private static int computeFlags(final StmtContext<?, ?, ?> ctx,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new FlagsBuilder()
                .setHistory(ctx.getCopyHistory())
                .setStatus(findFirstArgument(substatements, StatusEffectiveStatement.class, Status.CURRENT))
                .setConfiguration(ctx.isConfiguration()
                    && ctx.allSubstatementsStream().anyMatch(StmtContext::isConfiguration))
                .toFlags();
    }
}