/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceKeyCriterion;

abstract class NamespaceBehaviourWithListeners<K, V, N extends IdentifierNamespace<K, V>>
        extends NamespaceBehaviour<K, V, N> {

    abstract static class ValueAddedListener<K> {
        private final NamespaceStorageNode ctxNode;

        ValueAddedListener(final NamespaceStorageNode contextNode) {
            this.ctxNode = requireNonNull(contextNode);
        }

        final NamespaceStorageNode getCtxNode() {
            return ctxNode;
        }

        abstract <V> boolean isRequestedValue(NamespaceBehaviour<K, ? , ?> behavior, NamespaceStorageNode storage,
                V value);

        abstract void onValueAdded(Object value);
    }

    abstract static class KeyedValueAddedListener<K> extends ValueAddedListener<K> {
        private final K key;

        KeyedValueAddedListener(final NamespaceStorageNode contextNode, final K key) {
            super(contextNode);
            this.key = requireNonNull(key);
        }

        final K getKey() {
            return key;
        }

        @Override
        final <V> boolean isRequestedValue(final NamespaceBehaviour<K, ? , ?> behavior,
                final NamespaceStorageNode storage, final V value) {
            return value == behavior.getFrom(getCtxNode(), key);
        }
    }

    protected final NamespaceBehaviour<K, V, N> delegate;
    private final List<VirtualNamespaceContext<?, V, ?, K>> derivedNamespaces = new ArrayList<>();

    protected NamespaceBehaviourWithListeners(final NamespaceBehaviour<K, V, N> delegate) {
        super(delegate.getIdentifier());
        this.delegate = delegate;
    }

    abstract void addListener(KeyedValueAddedListener<K> listener);

    abstract void addListener(NamespaceKeyCriterion<K> criterion, ValueAddedListener<K> listener);

    @Override
    public abstract void addTo(NamespaceStorageNode storage, K key, V value);

    protected void notifyListeners(final NamespaceStorageNode storage,
            final Iterator<? extends ValueAddedListener<K>> keyListeners, final V value) {
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
