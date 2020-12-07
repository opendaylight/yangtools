/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.output;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.OutputSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseOperationContainerStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.SubstatementIndexingException;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

@Beta
public final class OutputStatementSupport
        extends BaseOperationContainerStatementSupport<OutputStatement, OutputEffectiveStatement> {
    private static final @NonNull OutputStatementSupport RFC6020_INSTANCE = new OutputStatementSupport(
        SubstatementValidator.builder(YangStmtMapping.OUTPUT)
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
    private static final @NonNull OutputStatementSupport RFC7950_INSTANCE = new OutputStatementSupport(
        SubstatementValidator.builder(YangStmtMapping.OUTPUT)
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

    private OutputStatementSupport(final SubstatementValidator validator) {
        super(YangStmtMapping.OUTPUT, YangConstants::operationOutputQName, CopyPolicy.DECLARED_COPY);
        this.validator = requireNonNull(validator);
    }

    public static @NonNull OutputStatementSupport rfc6020Instance() {
        return RFC6020_INSTANCE;
    }

    public static @NonNull OutputStatementSupport rfc7950Instance() {
        return RFC7950_INSTANCE;
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return validator;
    }

    @Override
    protected OutputStatement createDeclared(final StmtContext<QName, OutputStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        final StatementSource source = ctx.source();
        switch (source) {
            case CONTEXT:
                return new RegularUndeclaredOutputStatement(ctx.getArgument(), substatements);
            case DECLARATION:
                return new RegularOutputStatement(ctx.getArgument(), substatements);
            default:
                throw new IllegalStateException("Unhandled statement source " + source);
        }
    }

    @Override
    protected OutputStatement createEmptyDeclared(final StmtContext<QName, OutputStatement, ?> ctx) {
        final StatementSource source = ctx.source();
        switch (source) {
            case CONTEXT:
                return new EmptyUndeclaredOutputStatement(ctx.getArgument());
            case DECLARATION:
                return new EmptyOutputStatement(ctx.getArgument());
            default:
                throw new IllegalStateException("Unhandled statement source " + source);
        }
    }

    @Override
    protected OutputEffectiveStatement createDeclaredEffective(final int flags,
            final Current<QName, OutputStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new DeclaredOutputEffectiveStatement(flags, stmt.declared(), substatements, stmt.wrapSchemaPath());
    }

    @Override
    protected OutputEffectiveStatement createUndeclaredEffective(final int flags,
            final Current<QName, OutputStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        try {
            return new UndeclaredOutputEffectiveStatement(flags, substatements, stmt.wrapSchemaPath());
        } catch (SubstatementIndexingException e) {
            throw new SourceException(e.getMessage(), stmt, e);
        }
    }

    @Override
    public @NonNull boolean copyEffective(final OutputEffectiveStatement original,
                                          final Current<QName, OutputStatement> stmt) {
        if (((OutputSchemaNode) original).isAddedByUses()
                != stmt.history().contains(CopyType.ADDED_BY_USES)) {
            return false;
        }
        if (((OutputSchemaNode) original).isAugmenting()
                != stmt.history().contains(CopyType.ADDED_BY_AUGMENTATION)) {
            return false;
        }
        return ((SchemaNode) original).getPath().equals(stmt.wrapSchemaPath());
    }
}
