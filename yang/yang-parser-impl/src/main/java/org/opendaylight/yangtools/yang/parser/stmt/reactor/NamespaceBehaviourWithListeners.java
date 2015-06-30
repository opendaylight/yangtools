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
import java.util.Iterator;
import java.util.Map;

import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;

final class NamespaceBehaviourWithListeners<K,V, N extends IdentifierNamespace<K, V>> extends NamespaceBehaviour<K, V, N> {

    static abstract class ValueAddedListener {

        private org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.NamespaceStorageNode ctxNode;

        public ValueAddedListener(NamespaceStorageNode contextNode) {
            this.ctxNode = contextNode;
        }

        abstract void onValueAdded(Object key, Object value);

    }

    private final NamespaceBehaviour<K, V, N> delegate;
    private final Multimap<K, ValueAddedListener> listeners = HashMultimap.create();

    protected NamespaceBehaviourWithListeners(NamespaceBehaviour<K, V, N> delegate) {
        super(delegate.getIdentifier());
        this.delegate = delegate;
    }

    @Override
    public void addTo(NamespaceBehaviour.NamespaceStorageNode storage,
            K key, V value) {
        delegate.addTo(storage, key, value);

        Iterator<ValueAddedListener> keyListeners = listeners.get(key).iterator();
        while(keyListeners.hasNext()) {
            ValueAddedListener listener = keyListeners.next();
            if(listener.ctxNode == storage || hasIdentiticalValue(listener.ctxNode,key,value)) {
                keyListeners.remove();
                listener.onValueAdded(key, value);
            }
        }
    }

    private boolean hasIdentiticalValue(NamespaceBehaviour.NamespaceStorageNode ctxNode, K key, V value) {
        return getFrom(ctxNode, key) == value;
    }

    void addValueListener(K key, ValueAddedListener listener) {
        listeners.put(key, listener);
    }

    @Override
    public V getFrom(NamespaceBehaviour.NamespaceStorageNode storage,
            K key) {
        return delegate.getFrom(storage, key);
    }

    @Override
    public Map<K, V> getAllFrom(final NamespaceStorageNode storage) {
        return delegate.getAllFrom(storage);
    }
}
