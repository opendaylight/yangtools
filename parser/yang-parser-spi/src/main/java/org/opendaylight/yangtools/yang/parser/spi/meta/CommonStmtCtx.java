/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.base.VerifyException;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;

/**
 * Common interface for all statement contexts, exposing information which is always available. Note this includes only
 * stateless information -- hence we have {@link #rawArgument()} but do not have an equivalent {@code argument()}.
 */
public interface CommonStmtCtx {
    /**
     * {@return the {@link StatementDefinition} corresponding to the statement this context produces}
     */
    @NonNull StatementDefinition<?, ?, ?> publicDefinition();

    /**
     * {@return {@code true} if this context produces the statement corresponding to the specified
     * {@link StatementDefinition}}
     * @param def the {@link StatementDefinition}
     */
    default boolean produces(final StatementDefinition<?, ?, ?> def) {
        return def.equals(publicDefinition());
    }

    /**
     * {@return {@code true} if this context produces a statement corresponding to any of the specified
     * {@link StatementDefinition}s}
     * @param defs the {@link StatementDefinition}s
     */
    default boolean producesAnyOf(final StatementDefinition<?, ?, ?>... defs) {
        final var myDef = publicDefinition();
        for (var def : defs) {
            if (def.equals(myDef)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@return {@code true} if this context produces a statement corresponding to any of the specified
     * {@link StatementDefinition}s}
     * @param defs the {@link StatementDefinition}s
     */
    default boolean producesAnyOf(final Collection<? extends StatementDefinition<?, ?, ?>> defs) {
        return defs.contains(publicDefinition());
    }

    /**
     * Return true if this context produces specified {@link DeclaredStatement} representation.
     *
     * @param <D> Declared Statement representation
     * @param type DeclaredStatement representation
     * @return True if this context results in specified {@link DeclaredStatement} representation
     */
    default <D extends DeclaredStatement<?>> boolean producesDeclared(final Class<? super D> type) {
        return type.isAssignableFrom(publicDefinition().declaredRepresentation());
    }

    /**
     * Return true if this context produces specified {@link EffectiveStatement} representation.
     *
     * @param <E> Effective Statement representation
     * @param type EffectiveStatement representation
     * @return True if this context results in specified {@link EffectiveStatement} representation
     */
    default <E extends EffectiveStatement<?, ?>> boolean producesEffective(final Class<? super E> type) {
        return type.isAssignableFrom(publicDefinition().effectiveRepresentation());
    }

    /**
     * Returns a reference to statement source.
     *
     * @return reference of statement source
     */
    @NonNull StatementSourceReference sourceReference();

    /**
     * Return the statement argument in literal format.
     *
     * @return raw statement argument string, or null if this statement does not have an argument.
     */
    @Nullable String rawArgument();

    /**
     * Return the statement argument in literal format.
     *
     * @return raw statement argument string
     * @throws VerifyException if this statement does not have an argument
     */
    default @NonNull String getRawArgument() {
        final var ret = rawArgument();
        if (ret == null) {
            throw new VerifyException("Statement context " + this + " does not have an argument");
        }
        return ret;
    }
}
