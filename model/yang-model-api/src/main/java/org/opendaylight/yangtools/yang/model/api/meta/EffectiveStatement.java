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
import java.util.NoSuchElementException;
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
     * <p>Implementations are required to return a {@link StatementOrigin}, consistent with {@link #getDeclared()}
     * nullness. This is what the default implementation does and hence this method should never be explicitly
     * implemented -- unless there is significant cost to the {@link #getDeclared()} implementation.
     *
     * @return statement origin.
     */
    default @NonNull StatementOrigin statementOrigin() {
        return declared() != null ? StatementOrigin.DECLARATION : StatementOrigin.CONTEXT;
    }

    /**
     * {@return the {@link DeclaredStatement} declaring of effective statement or {@code null} if this effective
     * statement was inferred}
     * @since 15.0.0
     */
    @Nullable D declared();

    /**
     * {@return the {@link DeclaredStatement} declaring of effective statement or empty if this effective statement was
     * inferred}
     * @since 15.0.0
     */
    default @NonNull Optional<D> findDeclared() {
        final var declared = declared();
        return declared == null ? Optional.empty() : Optional.of(declared);
    }

    /**
     * {@return the {@link DeclaredStatement} declaring of effective statement or {@code null} if this effective
     * statement was inferred}
     * @deprecated Use {@link #declared()} instead or {@link #requireDeclared()} instead.
     */
    @Deprecated(since = "15.0.0", forRemoval = true)
    default @Nullable D getDeclared() {
        return declared();
    }

    /**
     * {@return the {@link DeclaredStatement} declaring of effective statement}}
     * @throws NoSuchElementException if if this effective statement was inferred
     * @since 15.0.0
     */
    // FIXME: rename to getDeclared() once that name is available
    default @NonNull D requireDeclared() {
        final var declared = declared();
        if (declared == null) {
            throw new NoSuchElementException(this + " was not explicitly declared");
        }
        return declared;
    }

    /**
     * {@return a collection of all effective substatements}
     */
    @NonNull List<? extends @NonNull EffectiveStatement<?, ?>> effectiveSubstatements();

    /**
     * Find the first effective substatement of specified type.
     *
     * @param <T> substatement type
     * @param type substatement type
     * @return First effective substatement, or empty if no match is found.
     */
    default <T> @NonNull Optional<T> findFirstEffectiveSubstatement(final @NonNull Class<T> type) {
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
    default <V, T extends EffectiveStatement<V, ?>> @NonNull Optional<V> findFirstEffectiveSubstatementArgument(
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
    default <T extends EffectiveStatement<?, ?>> @NonNull Stream<T> streamEffectiveSubstatements(
            final @NonNull Class<T> type) {
        return effectiveSubstatements().stream().filter(type::isInstance).map(type::cast);
    }

    @Beta
    default <Z extends EffectiveStatement<?, ?>> @NonNull Collection<Z> collectEffectiveSubstatements(
            final @NonNull Class<Z> stmt) {
        return streamEffectiveSubstatements(stmt).collect(Collectors.toUnmodifiableList());
    }
}
