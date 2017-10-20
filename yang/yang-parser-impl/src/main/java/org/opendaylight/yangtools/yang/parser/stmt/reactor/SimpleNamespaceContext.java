/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;

final class SimpleNamespaceContext<K, V, N extends IdentifierNamespace<K, V>>
        extends NamespaceBehaviourWithListeners<K, V, N> {

    // FIXME: Change this to Multimap, once issue with modules is resolved.
    private final List<KeyedValueAddedListener<K>> listeners = new ArrayList<>();

    private final Collection<PredicateValueAddedListener<K, V>> predicateListeners = new ArrayList<>();

    SimpleNamespaceContext(final NamespaceBehaviour<K, V, N> delegate) {
        super(delegate);
    }

    @Override
    void addListener(final KeyedValueAddedListener<K> listener) {
        listeners.add(listener);
    }

    @Override
    void addListener(final PredicateValueAddedListener<K, V> listener) {
        predicateListeners.add(listener);

        final Map<K, V> allItems = getAllFrom(listener.getCtxNode());
        if (allItems != null) {
            for (Entry<K, V> entry : allItems.entrySet()) {
                listener.onValueAdded(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public void addTo(final NamespaceStorageNode storage, final K key, final V value) {
        delegate.addTo(storage, key, value);
        notifyListeners(storage, listeners.iterator(), value);
        predicateListeners.forEach(listener -> listener.onValueAdded(key, value));
        notifyDerivedNamespaces(storage, key, value);
    }
}
