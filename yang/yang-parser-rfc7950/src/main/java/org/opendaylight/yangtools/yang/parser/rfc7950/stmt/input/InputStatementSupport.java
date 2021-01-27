/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.input;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
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
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

@Beta
public final class InputStatementSupport
        extends BaseOperationContainerStatementSupport<InputStatement, InputEffectiveStatement> {
    private static final @NonNull InputStatementSupport RFC6020_INSTANCE = new InputStatementSupport(
        SubstatementValidator.builder(YangStmtMapping.INPUT)
            .addAny(YangStmtMapping.ANYXML)
            .addAny(YangStmtMapping.CHOICE)
            .addAny(YangStmtMapping.CONTAINER)
            .addAny(YangStmtMapping.GROUPING)
            .addAny(YangStmtMapping.LEAF)
            .addAny(YangStmtMapping.LEAF_LIST)
            .addAny(YangStmtMapping.LIST)
            .addAny(YangStmtMapping.TYPEDEF)
            .addAny(YangStmtMapping.USES)
            .build());
    private static final @NonNull InputStatementSupport RFC7950_INSTANCE = new InputStatementSupport(
        SubstatementValidator.builder(YangStmtMapping.INPUT)
            .addAny(YangStmtMapping.ANYDATA)
            .addAny(YangStmtMapping.ANYXML)
            .addAny(YangStmtMapping.CHOICE)
            .addAny(YangStmtMapping.CONTAINER)
            .addAny(YangStmtMapping.GROUPING)
            .addAny(YangStmtMapping.LEAF)
            .addAny(YangStmtMapping.LEAF_LIST)
            .addAny(YangStmtMapping.LIST)
            .addAny(YangStmtMapping.MUST)
            .addAny(YangStmtMapping.TYPEDEF)
            .addAny(YangStmtMapping.USES)
            .build());

    private final SubstatementValidator validator;

    private InputStatementSupport(final SubstatementValidator validator) {
        super(YangStmtMapping.INPUT, YangConstants::operationInputQName);
        this.validator = requireNonNull(validator);
    }

    public static @NonNull InputStatementSupport rfc6020Instance() {
        return RFC6020_INSTANCE;
    }

    public static @NonNull InputStatementSupport rfc7950Instance() {
        return RFC7950_INSTANCE;
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return validator;
    }

    @Override
    protected InputStatement createDeclared(final StmtContext<QName, InputStatement, ?> ctx,
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
    protected InputStatement createEmptyDeclared(final StmtContext<QName, InputStatement, ?> ctx) {
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
    protected InputEffectiveStatement copyDeclaredEffective(final int flags,
            final Current<QName, InputStatement> stmt, final InputEffectiveStatement original) {
        return new DeclaredInputEffectiveStatement(flags, (DeclaredInputEffectiveStatement) original,
            stmt.wrapSchemaPath());
    }

    @Override
    protected InputEffectiveStatement copyUndeclaredEffective(final int flags,
            final Current<QName, InputStatement> stmt, final InputEffectiveStatement original) {
        return new UndeclaredInputEffectiveStatement(flags, (UndeclaredInputEffectiveStatement) original,
            stmt.wrapSchemaPath());
    }

    @Override
    protected InputEffectiveStatement createDeclaredEffective(final int flags,
            final Current<QName, InputStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new DeclaredInputEffectiveStatement(flags, stmt.declared(), substatements, stmt.wrapSchemaPath());
    }

    @Override
    protected InputEffectiveStatement createUndeclaredEffective(final int flags,
            final Current<QName, InputStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        try {
            return new UndeclaredInputEffectiveStatement(flags, substatements, stmt.wrapSchemaPath());
        } catch (SubstatementIndexingException e) {
            throw new SourceException(e.getMessage(), stmt, e);
        }
    }
}
