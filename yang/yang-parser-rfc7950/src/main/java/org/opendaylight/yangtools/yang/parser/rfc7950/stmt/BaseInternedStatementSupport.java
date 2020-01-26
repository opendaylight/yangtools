/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import com.google.common.annotations.Beta;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import java.util.concurrent.ExecutionException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

/**
 * A {@link BaseStatementSupport} specialized for global interning. This base class is useful when the argument can be
 * reasonably interned and it dominates the {@link EffectiveStatement} implementation. Typical examples include
 * {@code position} and {@code value} statements, which typically do not have substatements and are based on simple
 * types.
 *
 * <p>
 * Note: use of this base class implies context-independence.
 */
@Beta
public abstract class BaseInternedStatementSupport<A, D extends DeclaredStatement<A>,
        E extends EffectiveStatement<A, D>> extends BaseStatementSupport<A, D, E> {
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
                public E load(final D key) throws ExecutionException {
                    return createEmptyEffective(key);
                }
            });

    protected BaseInternedStatementSupport(final StatementDefinition publicDefinition) {
        super(publicDefinition, true);
    }

    @Override
    protected final D createEmptyDeclared(final StmtContext<A, D, ?> ctx) {
        return declaredCache.getUnchecked(ctx.coerceStatementArgument());
    }

    protected abstract @NonNull D createEmptyDeclared(@NonNull A argument);

    @Override
    protected final E createEmptyEffective(final StmtContext<A, D, E> ctx, final D declared) {
        return effectiveCache.getUnchecked(declared);
    }

    protected abstract @NonNull E createEmptyEffective(@NonNull D declared);

    @Override
    protected final D createDeclared(final StmtContext<A, D, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return createDeclared(ctx.coerceStatementArgument(), substatements);
    }

    protected abstract @NonNull D createDeclared(@NonNull A argument,
            ImmutableList<? extends DeclaredStatement<?>> substatements);
}
