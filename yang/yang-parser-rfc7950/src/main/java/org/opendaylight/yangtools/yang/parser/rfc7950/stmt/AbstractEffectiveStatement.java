/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;

/**
 * Baseline stateless implementation of an EffectiveStatement. This class adds a few default implementations and
 * namespace dispatch, but does not actually force any state on its subclasses. This approach is different from
 * {@link EffectiveStatementBase} in that it adds requirements for an implementation, but it leaves it up to the final
 * class to provide object layout.
 *
 * <p>
 * This finds immense value in catering the common case, for example effective statements which can, but typically
 * do not, contain substatements.
 *
 * @param <A> Argument type ({@link Void} if statement does not have argument.)
 * @param <D> Class representing declared version of this statement.
 */
@Beta
public abstract class AbstractEffectiveStatement<A, D extends DeclaredStatement<A>>
        implements EffectiveStatement<A, D> {
    @Override
    public final <K, V, N extends IdentifierNamespace<K, V>> Optional<? extends V> get(final Class<N> namespace,
            final K identifier) {
        return Optional.ofNullable(getAll(namespace).get(requireNonNull(identifier)));
    }

    @Override
    public final <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAll(final Class<N> namespace) {
        final Optional<? extends Map<K, V>> ret = getNamespaceContents(requireNonNull(namespace));
        return ret.isPresent() ? ret.get() : ImmutableMap.of();
    }

    @Override
    public Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        return ImmutableList.of();
    }

    /**
     * Return the statement-specific contents of specified namespace, if available.
     *
     * @param namespace Requested namespace
     * @return Namespace contents, if available.
     */
    protected <K, V, N extends IdentifierNamespace<K, V>> Optional<? extends Map<K, V>> getNamespaceContents(
            final @NonNull Class<N> namespace) {
        return Optional.empty();
    }
}
