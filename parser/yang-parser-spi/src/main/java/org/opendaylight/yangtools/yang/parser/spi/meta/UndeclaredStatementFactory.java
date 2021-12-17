/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.annotations.Beta;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.UndeclaredCurrent;

/**
 * An entity capable of creating undeclared {@link EffectiveStatement} instances for a particular type. Unlike
 * {@link StatementFactory}, effective statements created through this interface are expected to return a {@code null}
 * from {@link EffectiveStatement#getDeclared()}.
 *
 * @param <A> Argument type
 * @param <D> Declared Statement representation
 * @param <E> Effective Statement representation
 */
@Beta
public interface UndeclaredStatementFactory<A, D extends DeclaredStatement, E extends EffectiveStatement<A, D>> {
    /**
     * Create a {@link EffectiveStatement} for specified context. Implementations of this method must not access
     * {@link Current#declared()} or {@link Current#rawArgument()}.
     *
     * @param stmt Effective capture of this statement's significant state
     * @param effectiveSubstatements effectively-visible substatements
     * @return An effective statement instance
     */
    @NonNull E createUndeclaredEffective(@NonNull UndeclaredCurrent<A, D> stmt,
        @NonNull Stream<? extends StmtContext<?, ?, ?>> effectiveSubstatements);
}
