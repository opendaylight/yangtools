/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.EffectiveStatementWithFlags.FlagsBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

/**
 * Specialization of {@link BaseStatementSupport} for QName statement arguments.
 *
 * @param <D> Declared Statement representation
 * @param <E> Effective Statement representation
 */
@Beta
public abstract class BaseQNameStatementSupport<D extends DeclaredStatement<QName>,
        E extends EffectiveStatement<QName, D>> extends AbstractQNameStatementSupport<D, E> {
    protected BaseQNameStatementSupport(final StatementDefinition publicDefinition) {
        super(publicDefinition);
    }

    protected BaseQNameStatementSupport(final StatementDefinition publicDefinition, final CopyPolicy copyPolicy) {
        super(publicDefinition, copyPolicy);
    }

    @Override
    public final D createDeclared(final StmtContext<QName, D, ?> ctx) {
        final ImmutableList<? extends DeclaredStatement<?>> substatements = ctx.declaredSubstatements().stream()
                .map(StmtContext::buildDeclared)
                .collect(ImmutableList.toImmutableList());
        return substatements.isEmpty() ? createEmptyDeclared(ctx) : createDeclared(ctx, substatements);
    }

    protected abstract @NonNull D createDeclared(@NonNull StmtContext<QName, D, ?> ctx,
            @NonNull ImmutableList<? extends DeclaredStatement<?>> substatements);

    protected abstract @NonNull D createEmptyDeclared(@NonNull StmtContext<QName, D, ?> ctx);

    @Override
    public E createEffective(final StmtContext<QName, D, E> ctx) {
        final D declared = ctx.buildDeclared();
        final ImmutableList<? extends EffectiveStatement<?, ?>> substatements =
                BaseStatementSupport.buildEffectiveSubstatements(ctx);
        return substatements.isEmpty() ? createEmptyEffective(ctx, declared)
                : createEffective(ctx, declared, substatements);
    }

    protected abstract @NonNull E createEffective(@NonNull StmtContext<QName, D, E> ctx, @NonNull D declared,
            @NonNull ImmutableList<? extends EffectiveStatement<?, ?>> substatements);

    protected abstract @NonNull E createEmptyEffective(@NonNull StmtContext<QName, D, E> ctx, @NonNull D declared);

    protected static final <E extends EffectiveStatement<?, ?>> @Nullable E findFirstStatement(
            final ImmutableList<? extends EffectiveStatement<?, ?>> statements, final Class<E> type) {
        return BaseStatementSupport.findFirstStatement(statements, type);
    }

    protected static final <A, E extends EffectiveStatement<A, ?>> A findFirstArgument(
            final ImmutableList<? extends EffectiveStatement<?, ?>> statements, final Class<E> type, final A defValue) {
        return BaseStatementSupport.findFirstArgument(statements, type, defValue);
    }

    protected static final int historyAndStatusFlags(final StmtContext<?, ?, ?> ctx,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new FlagsBuilder()
                .setHistory(ctx.getCopyHistory())
                .setStatus(findFirstArgument(substatements, StatusEffectiveStatement.class, Status.CURRENT))
                .toFlags();
    }
}
