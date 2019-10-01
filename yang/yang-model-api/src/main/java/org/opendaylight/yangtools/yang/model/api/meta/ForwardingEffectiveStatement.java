/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import com.google.common.annotations.Beta;
import com.google.common.collect.ForwardingObject;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Utility forwarding implementation of {@link EffectiveStatement} contract. This class is useful for implementing
 * wrapped statements.
 *
 * @author Robert Varga
 *
 * @param <A> Argument type
 * @param <D> Declared Statement representation
 * @param <E> Effective Statement representation
 */
@Beta
public abstract class ForwardingEffectiveStatement<A, D extends DeclaredStatement<A>,
        E extends EffectiveStatement<A, D>> extends ForwardingObject implements EffectiveStatement<A, D> {

    @Override
    protected abstract E delegate();


    @Override
    public D getDeclared() {
        return delegate().getDeclared();
    }

    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> Optional<? extends V> get(final Class<N> namespace,
            final K identifier) {
        return delegate().get(namespace, identifier);
    }

    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAll(final Class<N> namespace) {
        return delegate().getAll(namespace);
    }

    @Override
    public Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        return delegate().effectiveSubstatements();
    }

    @Override
    public StatementDefinition statementDefinition() {
        return delegate().statementDefinition();
    }

    @Override
    public A argument() {
        return delegate().argument();
    }

    @Override
    public StatementSource getStatementSource() {
        return delegate().getStatementSource();
    }
}
