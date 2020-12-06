/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.case_;

import com.google.common.collect.ImmutableList;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
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
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.SubstatementIndexingException;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Parent.EffectiveConfig;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

abstract class AbstractCaseStatementSupport
        extends BaseImplicitStatementSupport<CaseStatement, CaseEffectiveStatement> {
    AbstractCaseStatementSupport() {
        super(YangStmtMapping.CASE, CopyPolicy.DECLARED_COPY);
    }

    @Override
    protected final CaseStatement createDeclared(final StmtContext<QName, CaseStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        final StatementSource source = ctx.source();
        switch (source) {
            case CONTEXT:
                return new RegularUndeclaredCaseStatement(ctx.getArgument(), substatements);
            case DECLARATION:
                return new RegularCaseStatement(ctx.getArgument(), substatements);
            default:
                throw new IllegalStateException("Unhandled statement source " + source);
        }
    }

    @Override
    protected final CaseStatement createEmptyDeclared(final StmtContext<QName, CaseStatement, ?> ctx) {
        final StatementSource source = ctx.source();
        switch (source) {
            case CONTEXT:
                return new EmptyUndeclaredCaseStatement(ctx.getArgument());
            case DECLARATION:
                return new EmptyCaseStatement(ctx.getArgument());
            default:
                throw new IllegalStateException("Unhandled statement source " + source);
        }
    }

    @Override
    protected final CaseEffectiveStatement createDeclaredEffective(final Current<QName, CaseStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        try {
            return new DeclaredCaseEffectiveStatement(stmt.declared(), substatements, computeFlags(stmt, substatements),
                stmt.wrapSchemaPath(), findOriginal(stmt));
        } catch (SubstatementIndexingException e) {
            throw new SourceException(e.getMessage(), stmt, e);
        }
    }

    @Override
    protected final CaseEffectiveStatement createUndeclaredEffective(final Current<QName, CaseStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        try {
            return new UndeclaredCaseEffectiveStatement(substatements, computeFlags(stmt, substatements),
                stmt.wrapSchemaPath(), findOriginal(stmt));
        } catch (SubstatementIndexingException e) {
            throw new SourceException(e.getMessage(), stmt, e);
        }
    }

    private static @Nullable CaseSchemaNode findOriginal(final Current<?, ?> stmt) {
        return (CaseSchemaNode) stmt.original();
    }

    private static int computeFlags(final Current<?, ?> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final Boolean config;
        final EffectiveConfig effective = stmt.effectiveConfig();
        switch (effective) {
            case FALSE:
                config = Boolean.FALSE;
                break;
            case IGNORED:
                config = null;
                break;
            case TRUE:
                final Boolean sub = substatementEffectiveConfig(substatements);
                config = sub != null ? sub : Boolean.TRUE;
                break;
            case UNDETERMINED:
                config = substatementEffectiveConfig(substatements);
                break;
            default:
                throw new IllegalStateException("Unhandled effective config " + effective);
        }

        return new FlagsBuilder()
                .setHistory(stmt.history())
                .setStatus(findFirstArgument(substatements, StatusEffectiveStatement.class, Status.CURRENT))
                .setConfiguration(config)
                .toFlags();
    }

    @SuppressFBWarnings(value = "NP_BOOLEAN_RETURN_NULL", justification = "Internal use tagged with @Nullable")
    private static @Nullable Boolean substatementEffectiveConfig(
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        for (EffectiveStatement<?, ?> stmt : substatements) {
            if (stmt instanceof DataSchemaNode) {
                final Optional<Boolean> opt = ((DataSchemaNode) stmt).effectiveConfig();
                if (opt.isPresent()) {
                    return opt.orElseThrow();
                }
            }
        }
        return null;
    }
}
