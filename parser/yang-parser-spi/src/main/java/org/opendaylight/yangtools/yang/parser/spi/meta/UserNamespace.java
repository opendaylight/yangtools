/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * A writable {@link ParserNamespace}, which can be modified over the course of an execution.
 *
 * @param <K> key type
 * @param <V> value type
 */
@NonNullByDefault
public sealed interface UserNamespace<K, V> extends ParserNamespace<K, V> {
    /**
     * A generic {@link UserNamespace}.
     *
     * @param <K> key type
     * @param <V> value type
     * @since 16.2.0
     */
    sealed interface Generic<K, V> extends UserNamespace<K, V> permits UserNamespaceKV {
        // nothing else
    }

    /**
     * A {@link UserNamespace} containing at most one value.
     *
     * @param <V> value type
     * @since 16.2.0
     */
    sealed interface Singleton<V> extends UserNamespace<Empty, V> permits UserNamespaceEV {
        // nothing else
    }

    /**
     * A {@link UserNamespace} indexing {@link StmtContext}s by their argument.
     *
     * @param <A> Argument type
     * @param <D> Declared Statement representation
     * @param <E> Effective Statement representation
     * @since 16.2.0
     */
    sealed interface ArgumentToStatement<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
        extends UserNamespace<A, StmtContext<A, D, E>> permits UserNamespaceAS {
        // nothing else
    }

    /**
     * {@return a new {@link UserNamespace} namespace with specified name}
     *
     * @param <K> key type
     * @param <V> value type
     * @param name the name
     * @throws IllegalArgumentException if {@code name.isBlank()}
     * @see #generic(String)
     */
    static <K, V> UserNamespace<K, V> of(final String name) {
        return generic(name);
    }

    /**
     * {@return a new generic {@link UserNamespace} namespace with specified name}
     *
     * @param <K> key type
     * @param <V> value type
     * @param name the name
     * @throws IllegalArgumentException if {@code name.isBlank()}
     * @see #of(String)
     * @since 16.2.0
     */
    static <K, V> UserNamespace.Generic<K, V> generic(final String name) {
        return new UserNamespaceKV<>(name);
    }

    /**
     * {@return a new singleton {@link UserNamespace} namespace with specified name}
     *
     * @param <V> value type
     * @param name the name
     * @throws IllegalArgumentException if {@code name.isBlank()}
     * @since 16.2.0
     */
    static <V> UserNamespace.Singleton<V> singleton(final String name) {
        return new UserNamespaceEV<>(name);
    }

    static <A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
            UserNamespace.ArgumentToStatement<A, D, E> stmtContextByArgument(final String name) {
        return new UserNamespaceAS<>(name);
    }
}
