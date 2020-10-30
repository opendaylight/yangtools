/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * Default implementations for various {@link StmtContext} methods. Code hosted here is meant for use by actual
 * {@link StmtContext} implementations, not for general use by others.
 */
public final class StmtContextDefaults {

    private StmtContextDefaults() {
        // Hidden on purpose
    }

    /**
     * Default implementation of {@link StmtContext#findSubstatementArgument(Class)}. See that method for API contract.
     *
     * @param <A> Substatement argument type
     * @param <E> Substatement effective statement representation
     * @param stmt Statement context to search
     * @param type Effective statement representation being look up
     * @return Effective statement argument, if found
     */
    public static <A, E extends EffectiveStatement<A, ?>> @NonNull Optional<A> findSubstatementArgument(
            final @NonNull StmtContext<?, ?, ?> stmt, final @NonNull Class<E> type) {
        return stmt.allSubstatementsStream()
                .filter(ctx -> ((StmtContext) ctx).producesEffective(type))
                .findAny()
                .map(ctx -> (A) ctx.coerceStatementArgument());
    }

    /**
     * Default implementation of {@link StmtContext#hasSubstatement(Class)}. See that method for API contract.
     *
     * @param stmt Statement context to search
     * @param type Effective statement representation being look up
     * @return True if a match is found, false otherwise
     */
    public static boolean hasSubstatement(final @NonNull StmtContext<?, ?, ?> stmt,
            final @NonNull Class<? extends EffectiveStatement> type) {
        return stmt.allSubstatementsStream()
            .anyMatch(ctx -> ((StmtContext<?, ?, ?>) ctx).producesEffective(type));
    }

}
