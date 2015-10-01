/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import com.google.common.base.Optional;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.omg.CORBA.CTX_RESTRICT_SCOPE;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.parser.builder.impl.ModuleIdentifierImpl;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;

final class NamespaceBehaviourWithListeners<K,V, N extends IdentifierNamespace<K, V>> extends NamespaceBehaviour<K, V, N> {

    abstract static class ValueAddedListener<K> {
        private final NamespaceStorageNode ctxNode;
        private K key;

        public ValueAddedListener(final NamespaceStorageNode contextNode, K key) {
            this.ctxNode = contextNode;
            this.key = key;
        }

        abstract void onValueAdded(Object key, Object value);
    }

    private final NamespaceBehaviour<K, V, N> delegate;
    private final List<ValueAddedListener<K>> listeners = new ArrayList<>(20);

    protected NamespaceBehaviourWithListeners(final NamespaceBehaviour<K, V, N> delegate) {
        super(delegate.getIdentifier());
        this.delegate = delegate;
    }

    @Override
    public void addTo(final NamespaceStorageNode storage, final K key, final V value) {
        delegate.addTo(storage, key, value);

        Iterator<ValueAddedListener<K>> keyListeners = listeners.iterator();
        List<ValueAddedListener<K>> toNotify = new ArrayList<>();
        while (keyListeners.hasNext()) {
            ValueAddedListener<K> listener = keyListeners.next();
            if (pointsToSameValue(listener,value)) {
                keyListeners.remove();
                toNotify.add(listener);
            }
        }
        for(ValueAddedListener<K> listener : toNotify) {
            listener.onValueAdded(key, value);
        }
    }

    private boolean pointsToSameValue(ValueAddedListener<K> listener, V value) {
        return value == getFrom(listener.ctxNode, listener.key);
    }

    void addValueListener(final ValueAddedListener<K> listener) {
        listeners.add(listener);
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
