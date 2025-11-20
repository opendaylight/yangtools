/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.annotations.Beta;
import com.google.common.base.VerifyException;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * A {@link CommonStmtCtx} which has additionally been bound to a {@link StatementSupport}. It provides
 * {@link #argument()} as interpreted by that support.
 *
 * @param <A> Argument type
 */
@Beta
public interface BoundStmtCtx<A> extends CommonStmtCtx {
    /**
     * Return the statement argument.
     *
     * @return statement argument, or null if this statement does not have an argument
     */
    @Nullable A argument();

    /**
     * Return the statement argument in literal format.
     *
     * @return raw statement argument string
     * @throws VerifyException if this statement does not have an argument
     */
    default @NonNull A getArgument() {
        final var ret = argument();
        if (ret == null) {
            throw new VerifyException("Attempted to use non-existent argument of " + this);
        }
        return ret;
    }

    /**
     * Return the {@link YangVersion} associated with this statement.
     *
     * @return YANG version used to bind this statement
     */
    @NonNull YangVersion yangVersion();

    /**
     * Search of any child statement context of specified type and return its argument. If such a statement exists, it
     * is assumed to have the right argument. Users should be careful to use this method for statements which have
     * cardinality {@code 0..1}, otherwise this method can return any one of the statement's argument.
     *
     * @param <X> Substatement argument type
     * @param <Z> Substatement effective statement representation
     * @param type Effective statement representation being look up
     * @return {@link Optional#empty()} if no statement exists, otherwise the argument value
     */
    <X, Z extends EffectiveStatement<X, ?>> @NonNull Optional<X> findSubstatementArgument(@NonNull Class<Z> type);

    /**
     * Check if there is any child statement context of specified type.
     *
     * @param type Effective statement representation being look up
     * @return True if such a child statement exists, false otherwise
     */
    boolean hasSubstatement(@NonNull Class<? extends EffectiveStatement<?, ?>> type);
}
