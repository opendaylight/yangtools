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
import java.util.Map;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;

abstract class NamespaceBehaviourWithListeners<K, V, N extends IdentifierNamespace<K, V>>
        extends NamespaceBehaviour<K, V, N> {

    abstract static class ValueAddedListener<K> {
        private final NamespaceStorageNode ctxNode;
        private final K key;

        ValueAddedListener(final NamespaceStorageNode contextNode, final K key) {
            this.ctxNode = contextNode;
            this.key = key;
        }

        NamespaceStorageNode getCtxNode() {
            return ctxNode;
        }

        final <V> boolean isRequestedValue(final NamespaceBehaviour<K, ? , ?> behavior,
                final NamespaceStorageNode storage, final V value) {
            return value == behavior.getFrom(ctxNode, key);
        }

        abstract void onValueAdded(Object value);
    }

    protected final NamespaceBehaviour<K, V, N> delegate;
    private final List<VirtualNamespaceContext<?, V, ?, K>> derivedNamespaces = new ArrayList<>();

    protected NamespaceBehaviourWithListeners(final NamespaceBehaviour<K, V, N> delegate) {
        super(delegate.getIdentifier());
        this.delegate = delegate;
    }

    abstract void addListener(K key, ValueAddedListener<K> listener);

    @Override
    public abstract void addTo(NamespaceStorageNode storage, K key, V value);

    protected void notifyListeners(final NamespaceStorageNode storage,
            final Iterator<ValueAddedListener<K>> keyListeners, final V value) {
        List<ValueAddedListener<K>> toNotify = new ArrayList<>();
        while (keyListeners.hasNext()) {
            final ValueAddedListener<K> listener = keyListeners.next();
            if (listener.isRequestedValue(this, storage, value)) {
                keyListeners.remove();
                toNotify.add(listener);
            }
        }
        for (ValueAddedListener<K> listener : toNotify) {
            listener.onValueAdded(value);
        }
    }

    protected void notifyDerivedNamespaces(final NamespaceStorageNode storage, final K key, final V value) {
        for (VirtualNamespaceContext<?, V, ?, K> derived : derivedNamespaces) {
            derived.addedToSourceNamespace(storage, key, value);
        }
    }

    final void addDerivedNamespace(final VirtualNamespaceContext<?, V, ?, K> namespace) {
        derivedNamespaces.add(namespace);
    }

    @Override
    public V getFrom(final NamespaceStorageNode storage, final K key) {
        return delegate.getFrom(storage, key);
    }

    @Override
    public Map<K, V> getAllFrom(final NamespaceStorageNode storage) {
        return delegate.getAllFrom(storage);
    }
}
