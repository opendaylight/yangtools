/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import static java.util.Objects.requireNonNull;

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
     * @param first the first {@link StatementDefinition}s
     * @param second the second {@link StatementDefinition}s
     */
    default boolean producesAnyOf(final StatementDefinition<?, ?, ?> first, final StatementDefinition<?, ?, ?> second) {
        requireNonNull(second);
        final var def = publicDefinition();
        return first.equals(def) || second.equals(def);
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
     * {@return {@code true} if this context produces a statement whose declared representation is assignment-compatible
     * with the specified type}
     * @param <D> declared statement representation
     * @param type {@link DeclaredStatement} representation
     */
    default <D extends DeclaredStatement<?>> boolean producesDeclared(final Class<? super D> type) {
        return type.isAssignableFrom(publicDefinition().declaredRepresentation());
    }

    /**
     * {@return {@code true} if this context produces a statement whose effective representation is
     * assignment-compatible with the specified type}
     * @param <E> effective statement representation
     * @param type {@link EffectiveStatement} representation
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
