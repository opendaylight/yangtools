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
import com.google.common.collect.Collections2;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents declared statement.
 *
 * @param <A> Argument type ({@link Void} if statement does not have argument.)
 */
public interface DeclaredStatement<A> extends ModelStatement<A> {
    /**
     * Returns statement argument as was present in original source.
     *
     * @return statement argument as was present in original source or null, if statement does not take argument.
     */
    @Nullable String rawArgument();

    /**
     * Returns collection of explicitly declared child statements, while preserving its original ordering from original
     * source.
     *
     * @return Collection of statements, which were explicitly declared in source of model.
     */
    @NonNull Collection<? extends DeclaredStatement<?>> declaredSubstatements();

    /**
     * Returns collection of explicitly declared child statements, while preserving its original ordering from original
     * source.
     *
     * @param type {@link DeclaredStatement} type
     * @return Collection of statements, which were explicitly declared in source of model.
     * @throws NullPointerException if {@code type} is null
     */
    default <S extends DeclaredStatement<?>> @NonNull Collection<? extends S> declaredSubstatements(
            final Class<S> type) {
        requireNonNull(type);
        return Collections2.transform(Collections2.filter(declaredSubstatements(), type::isInstance), type::cast);
    }

    /**
     * Find the first effective substatement of specified type.
     *
     * @param type {@link DeclaredStatement} type
     * @return First declared substatement, or empty if no match is found.
     * @throws NullPointerException if {@code type} is null
     */
    @Beta
    default <T extends DeclaredStatement<?>> @NonNull Optional<T> findFirstDeclaredSubstatement(
            @NonNull final Class<T> type) {
        requireNonNull(type);
        return streamDeclaredSubstatements(type).filter(type::isInstance).findFirst().map(type::cast);
    }

    /**
     * Find the first declared substatement of specified type and return its value.
     *
     * @return First declared substatement's argument, or empty if no match is found.
     * @throws NullPointerException if {@code type} is null
     */
    @Beta
    default <V, T extends DeclaredStatement<V>> @NonNull Optional<V> findFirstDeclaredSubstatementArgument(
            @NonNull final Class<T> type) {
        // FIXME: YANGTOOLS-908: T should imply non-null argument
        return findFirstDeclaredSubstatement(type).map(stmt -> verifyNotNull(stmt.argument()));
    }

    /**
     * Find all declared substatements of specified type and return them as a stream.
     *
     * @return A stream of all declared substatements of specified type.
     * @throws NullPointerException if {@code type} is null
     */
    @Beta
    default <T extends DeclaredStatement<?>> @NonNull Stream<T> streamDeclaredSubstatements(
            @NonNull final Class<T> type) {
        requireNonNull(type);
        return declaredSubstatements().stream().filter(type::isInstance).map(type::cast);
    }
}
