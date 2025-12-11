/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Empty;

/**
 * Abstract base class for {@link EffectiveStatement} implementations.
 *
 * @param <A> Argument type ({@link Empty} if statement does not have argument.)
 * @param <D> Class representing declared version of this statement.
 */
public abstract non-sealed class AbstractEffectiveStatement<A, D extends DeclaredStatement<A>>
        extends AbstractModelStatement<A> implements EffectiveStatement<A, D> {
    /**
     * Utility method for recovering singleton lists squashed by
     * {@link AbstractEffectiveStatement#maskList(ImmutableList)}.
     *
     * @param masked list to unmask
     * @return Unmasked list
     * @throws NullPointerException if masked is null
     * @throws ClassCastException if masked object does not match EffectiveStatement
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected static final @NonNull ImmutableList<? extends @NonNull EffectiveStatement<?, ?>> unmaskList(
            final @NonNull Object masked) {
        return (ImmutableList) unmaskList(masked, EffectiveStatement.class);
    }

    protected static final <E> @NonNull Optional<E> filterOptional(final @NonNull Optional<?> optional,
            final @NonNull Class<E> type) {
        return optional.filter(type::isInstance).map(type::cast);
    }

    protected static final <K, V> @NonNull Optional<V> findValue(final Map<K, V> map, final K key) {
        return Optional.ofNullable(map.get(requireNonNull(key)));
    }
}
