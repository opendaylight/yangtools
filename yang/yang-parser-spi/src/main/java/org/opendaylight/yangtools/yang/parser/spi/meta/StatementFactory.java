/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * An entity capable of creating {@link DeclaredStatement} and {@link EffectiveStatement} instances for a particular
 * type. This interface is usually realized as an implementation-specific combination with {@link StatementSupport}.
 *
 * @param <A> Argument type
 * @param <D> Declared Statement representation
 * @param <E> Effective Statement representation
 */
public interface StatementFactory<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>> {
    /**
     * Create a {@link DeclaredStatement} for specified context.
     *
     * @param ctx Statement context
     * @return A declared statement instance.
     */
    @NonNull D createDeclared(@NonNull StmtContext<A, D, ?> ctx);

    /**
     * Create a {@link EffectiveStatement} for specified context.
     *
     * @param stmt Effective capture of this statement's significant state
     * @return An effective statement instance
     */
    @NonNull E createEffective(EffectiveStmtCtx.@NonNull Current<A, D> stmt,
        Stream<? extends StmtContext<?, ?, ?>> declaredSubstatements,
        Stream<? extends StmtContext<?, ?, ?>> effectiveSubstatements);


    // FIXME: YANGTOOLS-1195: make this non-default
    default @NonNull boolean copyEffective(final @NonNull E original,
                                           final EffectiveStmtCtx.@NonNull Current<A, D> stmt) {
        return false;
    }
}
