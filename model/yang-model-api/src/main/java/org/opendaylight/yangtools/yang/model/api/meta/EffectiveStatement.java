/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import com.google.common.annotations.Beta;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.Empty;

/**
 * Effective model statement which should be used to derive application behaviour.
 *
 * @param <A> Argument type ({@link Empty} if statement does not have argument.)
 * @param <D> Class representing declared version of this statement.
 */
public non-sealed interface EffectiveStatement<A, D extends DeclaredStatement<A>> extends ModelStatement<A> {
    /**
     * Returns {@link StatementOrigin}, which denotes if statement was explicitly declared in original model or inferred
     * during semantic processing of model.
     *
     * <p>
     * Implementations are required to return a {@link StatementOrigin}, consistent with {@link #getDeclared()}
     * nullness. This is what the default implementation does and hence this method should never be explicitly
     * implemented -- unless there is significant cost to the {@link #getDeclared()} implementation.
     *
     * @return statement origin.
     */
    default @NonNull StatementOrigin statementOrigin() {
        return getDeclared() != null ? StatementOrigin.DECLARATION : StatementOrigin.CONTEXT;
    }

    /**
     * Returns statement, which was explicit declaration of this effective
     * statement.
     *
     * @return statement, which was explicit declaration of this effective
     *         statement or null if statement was inferred from context.
     */
    @Nullable D getDeclared();

    /**
     * Returns a collection of all effective substatements.
     *
     * @return collection of all effective substatements.
     */
    @NonNull List<? extends @NonNull EffectiveStatement<?, ?>> effectiveSubstatements();

    /**
     * Find the first effective substatement of specified type.
     *
     * @param <T> substatement type
     * @param type substatement type
     * @return First effective substatement, or empty if no match is found.
     */
    @Beta
    default <T> Optional<T> findFirstEffectiveSubstatement(final @NonNull Class<T> type) {
        return effectiveSubstatements().stream().filter(type::isInstance).findFirst().map(type::cast);
    }

    /**
     * Find the first effective substatement of specified type and return its value.
     *
     * @param <T> substatement type
     * @param <V> substatement argument type
     * @param type substatement type
     * @return First effective substatement's argument, or empty if no match is found.
     */
    @Beta
    default <V, T extends EffectiveStatement<V, ?>> Optional<V> findFirstEffectiveSubstatementArgument(
            final @NonNull Class<T> type) {
        return findFirstEffectiveSubstatement(type).map(EffectiveStatement::argument);
    }

    /**
     * Find all effective substatements of specified type and return them as a stream.
     *
     * @param <T> substatement type
     * @param type substatement type
     * @return A stream of all effective substatements of specified type.
     */
    @Beta
    default <T extends EffectiveStatement<?, ?>> Stream<T> streamEffectiveSubstatements(final @NonNull Class<T> type) {
        return effectiveSubstatements().stream().filter(type::isInstance).map(type::cast);
    }

    @Beta
    default <Z extends EffectiveStatement<?, ?>> @NonNull Collection<Z> collectEffectiveSubstatements(
            final @NonNull Class<Z> stmt) {
        return streamEffectiveSubstatements(stmt).collect(Collectors.toUnmodifiableList());
    }
}
