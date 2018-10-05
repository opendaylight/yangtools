/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Effective model statement which should be used to derive application behaviour.
 *
 * @param <A> Argument type ({@link Void} if statement does not have argument.)
 * @param <D> Class representing declared version of this statement.
 */
public interface EffectiveStatement<A, D extends DeclaredStatement<A>> extends ModelStatement<A> {
    /**
     * Returns statement, which was explicit declaration of this effective statement.
     *
     * @return statement, which was explicit declaration of this effective statement or null if statement was inferred
     *         from context.
     */
    @Nullable D getDeclared();

    /**
     * Returns value associated with supplied identifier.
     *
     * @param <K> Identifier type
     * @param <V> Value type
     * @param <N> Namespace identifier type
     * @param namespace Namespace type
     * @param identifier Identifier of element.
     * @return Value if present, null otherwise.
     */
    //<K, V, N extends IdentifierNamespace<? super K, ? extends V>> V
    // FIXME: 3.0.0: make this return an Optional, not a nullable
    <K, V, N extends IdentifierNamespace<K, V>> @Nullable V get(@NonNull Class<N> namespace, @NonNull K identifier);

    /**
     * Returns all local values from supplied namespace.
     *
     * @param <K> Identifier type
     * @param <V> Value type
     * @param <N> Namespace identifier type
     * @param namespace Namespace type
     * @return Value if present, null otherwise.
     */
    // FIXME: 3.0.0: make this contract return empty maps on non-presence
    <K, V, N extends IdentifierNamespace<K, V>> @Nullable Map<K, V> getAll(@NonNull Class<N> namespace);

    /**
     * Returns all local values from supplied namespace.
     *
     * @param <K> Identifier type
     * @param <V> Value type
     * @param <N> Namespace identifier type
     * @param namespace Namespace type
     * @return Key-value mappings, empty if the namespace does not exist.
     */
    // FIXME: 3.0.0: remove this in favor of fixed getAll()
    default <K, V, N extends IdentifierNamespace<K, V>> @NonNull Map<K, V> findAll(final @NonNull Class<N> namespace) {
        final Map<K, V> map = getAll(requireNonNull(namespace));
        return map == null ? ImmutableMap.of() : map;
    }

    /**
     * Returns a collection of all effective substatements.
     *
     * @return collection of all effective substatements.
     */
    @NonNull Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements();

    /**
     * Find the first effective substatement of specified type.
     *
     * @return First effective substatement, or empty if no match is found.
     */
    @Beta
    default <T extends EffectiveStatement<?, ?>> Optional<T> findFirstEffectiveSubstatement(
            final @NonNull Class<T> type) {
        return effectiveSubstatements().stream().filter(type::isInstance).findFirst().map(type::cast);
    }

    /**
     * Find the first effective substatement of specified type and return its value.
     *
     * @return First effective substatement's argument, or empty if no match is found.
     */
    @Beta
    default <V, T extends EffectiveStatement<V, ?>> Optional<V> findFirstEffectiveSubstatementArgument(
            final @NonNull Class<T> type) {
        return findFirstEffectiveSubstatement(type).map(stmt -> verifyNotNull(stmt.argument()));
    }

    /**
     * Find all effective substatements of specified type and return them as a stream.
     *
     * @return A stream of all effective substatements of specified type.
     */
    @Beta
    default <T extends EffectiveStatement<?, ?>> Stream<T> streamEffectiveSubstatements(final @NonNull Class<T> type) {
        return effectiveSubstatements().stream().filter(type::isInstance).map(type::cast);
    }
}
