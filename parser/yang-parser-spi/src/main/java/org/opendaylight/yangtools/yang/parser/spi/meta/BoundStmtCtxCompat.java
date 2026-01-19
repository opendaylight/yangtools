/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;

/**
 * Intermediate compatibility interface between {@link StmtContext} and {@link EffectiveStmtCtx.Current}.
 *
 * @param <A> Argument type
 * @param <D> Class representing declared version of this statement
 */
@Beta
public sealed interface BoundStmtCtxCompat<A, D extends DeclaredStatement<A>>
        extends BoundStmtCtx<A>, StmtContextCompat
        permits EffectiveStmtCtx.Current, StmtContext {
    /**
     * Returns the {@link DeclaredStatement} view of this statement.
     */
    @NonNull D declared();

    /**
     * {@return this context iff it produces specified {@link DeclaredStatement} representation, {@code null} otherwise}
     * @param <X> expected argument type
     * @param <Y> expected declared statement representation
     * @param type the declared statement class
     * @since 15.0.0
     */
    <X, Y extends DeclaredStatement<X>> @Nullable BoundStmtCtxCompat<X, Y> tryDeclaring(@NonNull Class<Y> type);
}
