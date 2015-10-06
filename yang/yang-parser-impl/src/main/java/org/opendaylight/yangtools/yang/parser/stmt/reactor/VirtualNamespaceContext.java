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

final class VirtualNamespaceContext<K, V, N extends IdentifierNamespace<K, V>, DK>
        extends NamespaceBehaviourWithListeners<K, V, N> {

    private final Multimap<DK, NamespaceBehaviourWithListeners.ValueAddedListener<K>> listeners = HashMultimap.create();
    private final DerivedNamespaceBehaviour<K, V, DK, N, ?> derivedDelegate;

    public VirtualNamespaceContext(DerivedNamespaceBehaviour<K, V, DK, N, ?> delegate) {
        super(delegate);
        this.derivedDelegate = delegate;
    }

    protected boolean isRequestedValue(NamespaceBehaviourWithListeners.ValueAddedListener<K> listener, NamespaceStorageNode storage, V value) {
        return value == getFrom(listener.getCtxNode(), listener.getKey());
    }

    @Override
    protected void addListener(K key, NamespaceBehaviourWithListeners.ValueAddedListener<K> listener) {
        listeners.put(derivedDelegate.getSignificantKey(key), listener);
    }


    void addedToSourceNamespace(NamespaceBehaviour.NamespaceStorageNode storage, DK key, V value) {
        notifyListeners(storage, listeners.get(key).iterator(), value);
    }

    @Override
    public void addTo(final NamespaceStorageNode storage, final K key, final V value) {
        delegate.addTo(storage, key, value);
        notifyListeners(storage, listeners.get(derivedDelegate.getSignificantKey(key)).iterator(), value);
        notifyDerivedNamespaces(storage, key, value);
    }
}