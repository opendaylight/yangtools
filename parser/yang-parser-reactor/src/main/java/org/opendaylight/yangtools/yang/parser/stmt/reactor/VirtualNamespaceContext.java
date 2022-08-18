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
import org.opendaylight.yangtools.yang.parser.spi.meta.DerivedNamespaceBehaviour;

final class VirtualNamespaceContext<K, V, D> extends NamespaceBehaviourWithListeners<K, V> {
    private final Multimap<D, KeyedValueAddedListener<K>> listeners = HashMultimap.create();
    private final DerivedNamespaceBehaviour<K, V, D, ?> derivedDelegate;

    VirtualNamespaceContext(final DerivedNamespaceBehaviour<K, V, D, ?> delegate) {
        super(delegate);
        this.derivedDelegate = delegate;
    }

    @Override
    void addListener(final KeyedValueAddedListener<K> listener) {
        listeners.put(derivedDelegate.getSignificantKey(listener.getKey()), listener);
    }

    @Override
    void addListener(final PredicateValueAddedListener<K, V> listener) {
        throw new UnsupportedOperationException("Virtual namespaces support only exact lookups");
    }

    void addedToSourceNamespace(final NamespaceStorageNode storage, final D key, final V value) {
        notifyListeners(storage, listeners.get(key).iterator(), value);
    }

    @Override
    public void addTo(final NamespaceStorageNode storage, final K key, final V value) {
        delegate.addTo(storage, key, value);
        notifyListeners(storage, listeners.get(derivedDelegate.getSignificantKey(key)).iterator(), value);
        notifyDerivedNamespaces(storage, key, value);
    }
}
