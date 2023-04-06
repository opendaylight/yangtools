/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.opendaylight.yangtools.yang.parser.spi.meta.DerivedNamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceStorage;

final class VirtualNamespaceContext<K, V, D> extends BehaviourNamespaceAccess<K, V> {
    private final Multimap<D, KeyedValueAddedListener<K>> listeners = HashMultimap.create();
    private final DerivedNamespaceBehaviour<K, V, D, ?> derivedDelegate;

    VirtualNamespaceContext(final AbstractNamespaceStorage globalContext,
            final DerivedNamespaceBehaviour<K, V, D, ?> behaviour) {
        super(globalContext, behaviour);
        derivedDelegate = requireNonNull(behaviour);
    }

    @Override
    void addListener(final KeyedValueAddedListener<K> listener) {
        listeners.put(derivedDelegate.getSignificantKey(listener.getKey()), listener);
    }

    @Override
    void addListener(final PredicateValueAddedListener<K, V> listener) {
        throw new UnsupportedOperationException("Virtual namespaces support only exact lookups");
    }

    void addedToSourceNamespace(final NamespaceStorage storage, final D key, final V value) {
        notifyListeners(storage, listeners.get(key).iterator(), value);
    }

    @Override
    void onValueTo(final NamespaceStorage storage, final K key, final V value) {
        notifyListeners(storage, listeners.get(derivedDelegate.getSignificantKey(key)).iterator(), value);
        notifyDerivedNamespaces(storage, key, value);
    }
}
