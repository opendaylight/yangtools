/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;

final class VirtualNamespaceContext<K, V, N extends IdentifierNamespace<K, V>>
        extends NamespaceBehaviourWithListeners<K, V, N> {

    private final List<NamespaceBehaviourWithListeners.ValueAddedListener<K>> listeners = new ArrayList<>(20);

    public VirtualNamespaceContext(NamespaceBehaviour<K, V, N> delegate) {
        super(delegate);
    }

    protected boolean isRequestedValue(NamespaceBehaviourWithListeners.ValueAddedListener<K> listener, NamespaceStorageNode storage, V value) {
        return value == getFrom(listener.getCtxNode(), listener.getKey());
    }

    @Override
    protected void addListener(K key, NamespaceBehaviourWithListeners.ValueAddedListener<K> listener) {
        listeners.add(listener);
    }

    @Override
    protected Iterator<NamespaceBehaviourWithListeners.ValueAddedListener<K>> getMutableListeners(K key) {
        return listeners.iterator();
    }
}