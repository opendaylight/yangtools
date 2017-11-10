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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Effective model statement which should be used to derive application behaviour.
 *
 * @param <A>
 *            Argument type ({@link Void} if statement does not have argument.)
 * @param <S>
 *            Class representing declared version of this statement.
 */
public interface EffectiveStatement<A, S extends DeclaredStatement<A>> extends ModelStatement<A> {

    /**
     * Returns statement, which was explicit declaration of this effective
     * statement.
     *
     *
     * @return statement, which was explicit declaration of this effective
     *         statement or null if statement was inferred from context.
     */
    @Nullable
    S getDeclared();

    /**
     * Returns value associated with supplied identifier.
     *
     * @param <K>
     *            Identifier type
     * @param <V>
     *            Value type
     * @param <N>
     *            Namespace identifier type
     * @param namespace
     *            Namespace type
     * @param identifier
     *            Identifier of element.
     * @return Value if present, null otherwise.
     */
    //<K, V, N extends IdentifierNamespace<? super K, ? extends V>> V
    @Nullable
    <K,V,N extends IdentifierNamespace<K, V>> V get(@Nonnull Class<N> namespace,@Nonnull  K identifier);

    /**
     * Returns all local values from supplied namespace.
     *
     * @param <K>
     *            Identifier type
     * @param <V>
     *            Value type
     * @param <N>
     *            Namespace identifier type
     * @param namespace
     *            Namespace type
     * @return Value if present, null otherwise.
     */
    @Nullable
    <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAll(@Nonnull Class<N> namespace);

    /**
     * Returns a collection of all effective substatements.
     *
     * @return collection of all effective substatements.
     */
    @Nonnull Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements();

    /**
     * Finds the first effective substatement of specified type.
     *
     * @return First effective substatement, or empty if no match is found.
     */
    @Beta
    default <T extends EffectiveStatement<?, ?>> Optional<T> findFirstEffectiveSubstatement(
            @Nonnull final Class<T> type) {
        return effectiveSubstatements().stream().filter(type::isInstance).findFirst().map(type::cast);
    }

    /**
     * Finds the first effective substatement of specified type and return its value
     *
     * @return First effective substatement's argument, or empty if no match is found.
     */
    @Beta
    default <V, T extends EffectiveStatement<V, ?>> Optional<V> findFirstEffectiveSubstatementArgument(
            @Nonnull final Class<T> type) {
        return effectiveSubstatements().stream().filter(type::isInstance).findFirst().map(type::cast)
                .map(EffectiveStatement::argument);
    }

    /**
     * Find all effective substatements of specified type and return them as a stream.
     *
     * @return A stream of all effective substatements of specified type.
     */
    @Beta
    default <T extends EffectiveStatement<?, ?>> Stream<T> streamEffectiveSubstatements(@Nonnull final Class<T> type) {
        return effectiveSubstatements().stream().filter(type::isInstance).map(type::cast);
    }
}
