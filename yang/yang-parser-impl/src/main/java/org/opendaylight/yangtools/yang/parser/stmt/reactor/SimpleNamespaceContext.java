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
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceKeyCriterion;

final class SimpleNamespaceContext<K, V, N extends IdentifierNamespace<K, V>>
        extends NamespaceBehaviourWithListeners<K, V, N> {

    // FIXME: Change this to Multimap, once issue with modules is resolved.
    private final List<ValueAddedListener<K>> listeners = new ArrayList<>();

    SimpleNamespaceContext(final NamespaceBehaviour<K, V, N> delegate) {
        super(delegate);
    }

    @Override
    void addListener(final KeyedValueAddedListener<K> listener) {
        listeners.add(listener);
    }

    @Override
    void addListener(final NamespaceKeyCriterion<K> criterion, final ValueAddedListener<K> listener) {
        // FIXME: implement this

        throw new UnsupportedOperationException("Not implemented");
    }

    private Iterator<ValueAddedListener<K>> getMutableListeners(final K key) {
        return listeners.iterator();
    }

    @Override
    public void addTo(final NamespaceStorageNode storage, final K key, final V value) {
        delegate.addTo(storage, key, value);
        notifyListeners(storage, getMutableListeners(key), value);
        notifyDerivedNamespaces(storage, key, value);
    }
}
