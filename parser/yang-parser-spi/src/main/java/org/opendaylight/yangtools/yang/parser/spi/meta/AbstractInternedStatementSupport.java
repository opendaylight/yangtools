/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;

/**
 * A {@link AbstractStatementSupport} specialized for global interning. This base class is useful when the argument can
 * be reasonably interned and it dominates the {@link EffectiveStatement} implementation. Typical examples include
 * {@code position} and {@code value} statements, which typically do not have substatements and are based on simple
 * types.
 *
 * <p>Note: use of this base class implies context-independence.
 */
public abstract class AbstractInternedStatementSupport<A, D extends DeclaredStatement<A>,
        E extends EffectiveStatement<A, D>> extends AbstractStatementSupport<A, D, E> {
    private final LoadingCache<A, D> declaredCache = CacheBuilder.newBuilder().weakValues()
            .build(new CacheLoader<A, D>() {
                @Override
                public D load(final A key) {
                    return createEmptyDeclared(key);
                }
            });
    private final LoadingCache<D, E> effectiveCache = CacheBuilder.newBuilder().weakValues()
            .build(new CacheLoader<D, E>() {
                @Override
                public E load(final D key) {
                    return createEmptyEffective(key);
                }
            });

    protected AbstractInternedStatementSupport(final StatementDefinition publicDefinition,
            final StatementPolicy<A, D> policy, final YangParserConfiguration config,
            final @Nullable SubstatementValidator validator) {
        super(publicDefinition, policy, config, validator);
    }

    @Override
    protected final D createDeclared(final BoundStmtCtx<A> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        final A argument = ctx.getArgument();
        return substatements.isEmpty() ? declaredCache.getUnchecked(ctx.getArgument())
            : createDeclared(argument, substatements);
    }

    protected abstract @NonNull D createDeclared(@NonNull A argument,
            @NonNull ImmutableList<DeclaredStatement<?>> substatements);

    protected abstract @NonNull D createEmptyDeclared(@NonNull A argument);

    @Override
    protected final E createEffective(final Current<A, D> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? effectiveCache.getUnchecked(stmt.declared())
            : createEffective(stmt.declared(), substatements);
    }

    protected abstract @NonNull E createEffective(@NonNull D declared,
        @NonNull ImmutableList<? extends EffectiveStatement<?, ?>> substatements);

    protected abstract @NonNull E createEmptyEffective(@NonNull D declared);
}
