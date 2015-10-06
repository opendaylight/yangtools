/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;

final class SimpleNamespaceContext<K, V, N extends IdentifierNamespace<K, V>>
        extends NamespaceBehaviourWithListeners<K, V, N> {

    // FIXME: Change this to Multimap, once issue with modules
    // is resolved.
    private final List<NamespaceBehaviourWithListeners.ValueAddedListener<K>> listeners = new ArrayList<>();
    public SimpleNamespaceContext(NamespaceBehaviour<K, V, N> delegate) {
        super(delegate);
    }

    protected boolean isRequestedValue(NamespaceBehaviourWithListeners.ValueAddedListener<K> listener, NamespaceStorageNode storage, V value) {
        NamespaceStorageNode listenerCtx = listener.getCtxNode();
        return value == getFrom(listenerCtx, listener.getKey());
    }

    @Override
    protected void addListener(K key, NamespaceBehaviourWithListeners.ValueAddedListener<K> listener) {
        listeners.add(listener);
    }

    private Iterator<NamespaceBehaviourWithListeners.ValueAddedListener<K>> getMutableListeners(K key) {
        return listeners.iterator();
    }

    @Override
    public void addTo(final NamespaceStorageNode storage, final K key, final V value) {
        delegate.addTo(storage, key, value);
        notifyListeners(storage, getMutableListeners(key), value);
        notifyDerivedNamespaces(storage, key, value);
    }
}