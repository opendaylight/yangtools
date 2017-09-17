/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.DerivedNamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;

final class VirtualNamespaceContext<K, V, N extends IdentifierNamespace<K, V>, D>
        extends NamespaceBehaviourWithListeners<K, V, N> {

    private final Multimap<D, ValueAddedListener<K>> listeners = HashMultimap.create();
    private final DerivedNamespaceBehaviour<K, V, D, N, ?> derivedDelegate;

    VirtualNamespaceContext(final DerivedNamespaceBehaviour<K, V, D, N, ?> delegate) {
        super(delegate);
        this.derivedDelegate = delegate;
    }

    @Override
    protected boolean isRequestedValue(final ValueAddedListener<K> listener, final NamespaceStorageNode storage,
            final V value) {
        return value == getFrom(listener.getCtxNode(), listener.getKey());
    }

    @Override
    protected void addListener(final K key, final ValueAddedListener<K> listener) {
        listeners.put(derivedDelegate.getSignificantKey(key), listener);
    }

    void addedToSourceNamespace(final NamespaceBehaviour.NamespaceStorageNode storage, final D key, final V value) {
        notifyListeners(storage, listeners.get(key).iterator(), value);
    }

    @Override
    public void addTo(final NamespaceStorageNode storage, final K key, final V value) {
        delegate.addTo(storage, key, value);
        notifyListeners(storage, listeners.get(derivedDelegate.getSignificantKey(key)).iterator(), value);
        notifyDerivedNamespaces(storage, key, value);
    }
}
