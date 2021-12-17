/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.Collections2;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents declared statement.
 */
public interface DeclaredStatement extends ModelStatement {
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
    @NonNull List<? extends DeclaredStatement> declaredSubstatements();

    /**
     * Returns collection of explicitly declared child statements, while preserving its original ordering from original
     * source.
     *
     * @param <T> substatement type
     * @param type {@link DeclaredStatement} type
     * @return Collection of statements, which were explicitly declared in source of model.
     * @throws NullPointerException if {@code type} is null
     */
    default <T extends DeclaredStatement> @NonNull Collection<? extends T> declaredSubstatements(
            final Class<T> type) {
        requireNonNull(type);
        return Collections2.transform(Collections2.filter(declaredSubstatements(), type::isInstance), type::cast);
    }

    /**
     * Returns a {@link DeclarationReference} associated with this statement, if available.
     *
     * @apiNote
     *     This method does not contribute any semantic information and is provided purely as a conduit for
     *     implementation-specific information where a statement instance came from.
     *
     * @implSpec
     *     The default implementation returns {@link Optional#empty()}.
     *
     * @return A {@link DeclarationReference} associated with this statement or {@link Optional#empty()}.
     */
    @Beta
    default @NonNull Optional<DeclarationReference> declarationReference() {
        return Optional.empty();
    }

    /**
     * Find the first effective substatement of specified type.
     *
     * @param <T> substatement type
     * @param type {@link DeclaredStatement} type
     * @return First declared substatement, or empty if no match is found.
     * @throws NullPointerException if {@code type} is null
     */
    @Beta
    default <T extends DeclaredStatement> @NonNull Optional<T> findFirstDeclaredSubstatement(
            @NonNull final Class<T> type) {
        requireNonNull(type);
        return streamDeclaredSubstatements(type).filter(type::isInstance).findFirst().map(type::cast);
    }

    /**
     * Find all declared substatements of specified type and return them as a stream.
     *
     * @param <T> substatement type
     * @param type {@link DeclaredStatement} type
     * @return A stream of all declared substatements of specified type.
     * @throws NullPointerException if {@code type} is null
     */
    @Beta
    default <T extends DeclaredStatement> @NonNull Stream<T> streamDeclaredSubstatements(
            @NonNull final Class<T> type) {
        requireNonNull(type);
        return declaredSubstatements().stream().filter(type::isInstance).map(type::cast);
    }
}
