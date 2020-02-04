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
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.namespace.ChildSchemaNodeNamespace;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.EffectiveStatementWithFlags.FlagsBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;

/**
 * Specialization of {@link BaseQNameStatementSupport} for {@code input} and {@code output} statements.
 *
 * @param <D> Declared Statement representation
 * @param <E> Effective Statement representation
 */
@Beta
public abstract class BaseOperationContainerStatementSupport<D extends DeclaredStatement<QName>,
        E extends EffectiveStatement<QName, D>> extends BaseQNameStatementSupport<D, E> {
    protected BaseOperationContainerStatementSupport(final StatementDefinition publicDefinition) {
        super(publicDefinition);
    }

    @Override
    public final void onStatementAdded(final Mutable<QName, D, E> stmt) {
        stmt.coerceParentContext().addToNs(ChildSchemaNodeNamespace.class, stmt.coerceStatementArgument(), stmt);
    }

    @Override
    protected final E createEffective(
            final StmtContext<QName, D, E> ctx,
            final D declared, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final StatementSource source = ctx.getStatementSource();
        final int flags = computeFlags(ctx, substatements);
        switch (source) {
            case CONTEXT:
                return createUndeclaredEffective(flags, ctx, substatements);
            case DECLARATION:
                return createDeclaredEffective(flags, ctx, substatements, declared);
            default:
                throw new IllegalStateException("Unhandled statement source " + source);
        }
    }

    @Override
    protected final E createEmptyEffective(final StmtContext<QName, D, E> ctx, final D declared) {
        return createEffective(ctx, declared, ImmutableList.of());
    }

    protected abstract @NonNull E createDeclaredEffective(int flags, @NonNull StmtContext<QName, D, E> ctx,
            @NonNull ImmutableList<? extends EffectiveStatement<?, ?>> substatements, @NonNull D declared);

    protected abstract @NonNull E createUndeclaredEffective(int flags, @NonNull StmtContext<QName, D, E> ctx,
            @NonNull ImmutableList<? extends EffectiveStatement<?, ?>> substatements);

    private static int computeFlags(final StmtContext<?, ?, ?> ctx,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new FlagsBuilder()
                .setHistory(ctx.getCopyHistory())
                .setStatus(findFirstArgument(substatements, StatusEffectiveStatement.class, Status.CURRENT))
                .toFlags();
    }
}
