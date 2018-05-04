/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;

/**
 * An {@link NamespaceBehaviour} which derives keys from a different namespace.
 *
 * @param <K> Key type
 * @param <V> Value type
 * @param <N> Namespace type
 * @param <L> Original key type
 * @param <O> Original namespace type
 */
public abstract class DerivedNamespaceBehaviour<K, V, L, N extends IdentifierNamespace<K, V>,
       O extends IdentifierNamespace<L, ?>> extends NamespaceBehaviour<K, V, N> {

    private final Class<O> derivedFrom;

    protected DerivedNamespaceBehaviour(final Class<N> identifier, final Class<O> derivedFrom) {
        super(identifier);
        this.derivedFrom = requireNonNull(derivedFrom);
    }

    public Class<O> getDerivedFrom() {
        return derivedFrom;
    }

    @Override
    public Map<K, V> getAllFrom(final NamespaceStorageNode storage) {
        throw new UnsupportedOperationException("Virtual namespaces does not support provision of all items.");
    }

    @Override
    public abstract V getFrom(NamespaceBehaviour.NamespaceStorageNode storage, K key);

    @Override
    public void addTo(final NamespaceStorageNode storage, final K key, final V value) {
        // Intentional noop
    }

    public abstract L getSignificantKey(K key);
}
