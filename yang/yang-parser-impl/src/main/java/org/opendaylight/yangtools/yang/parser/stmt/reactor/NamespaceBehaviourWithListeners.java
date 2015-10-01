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
        private K key;

        public ValueAddedListener(final NamespaceStorageNode contextNode, K key) {
            this.ctxNode = contextNode;
            this.key = key;
        }

        public NamespaceStorageNode getCtxNode() {
            return ctxNode;
        }

        public K getKey() {
            return key;
        }

        abstract void onValueAdded(Object key, Object value);
    }

    private final NamespaceBehaviour<K, V, N> delegate;
    private final List<VirtualNamespaceContext<?, V, ?>> derivedNamespaces = new ArrayList<>();


    protected NamespaceBehaviourWithListeners(final NamespaceBehaviour<K, V, N> delegate) {
        super(delegate.getIdentifier());
        this.delegate = delegate;
    }

    protected abstract void addListener(K key, ValueAddedListener<K> listener);

    protected abstract Iterator<ValueAddedListener<K>> getMutableListeners(K key);

    protected abstract boolean isRequestedValue(ValueAddedListener<K> listener, NamespaceStorageNode storage, V value);

    @Override
    public void addTo(final NamespaceStorageNode storage, final K key, final V value) {
        delegate.addTo(storage, key, value);

        Iterator<ValueAddedListener<K>> keyListeners = getMutableListeners(key);
        List<ValueAddedListener<K>> toNotify = new ArrayList<>();
        while (keyListeners.hasNext()) {
            ValueAddedListener<K> listener = keyListeners.next();
            if (isRequestedValue(listener, storage, value)) {
                keyListeners.remove();
                toNotify.add(listener);
            }
        }
        for(ValueAddedListener<K> listener : toNotify) {
            listener.onValueAdded(key, value);
        }
        for (VirtualNamespaceContext<?, V, ?> derived : derivedNamespaces) {
            derived.addTo(storage, null, value);
        }
    }

    final void addValueListener(final ValueAddedListener<K> listener) {
        addListener(listener.key, listener);
    }

    final void addDerivedNamespace(VirtualNamespaceContext<?, V, ?> namespace) {
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
