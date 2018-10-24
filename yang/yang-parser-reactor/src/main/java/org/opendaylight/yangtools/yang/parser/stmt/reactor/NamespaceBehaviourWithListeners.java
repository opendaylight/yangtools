/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;

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

        final <V> boolean isRequestedValue(final NamespaceBehaviour<K, ? , ?> behavior,
                final NamespaceStorageNode storage, final V value) {
            return value == behavior.getFrom(getCtxNode(), key);
        }

        abstract void onValueAdded(Object value);
    }

    abstract static class PredicateValueAddedListener<K, V> extends ValueAddedListener<K> {
        PredicateValueAddedListener(final NamespaceStorageNode contextNode) {
            super(contextNode);
        }

        abstract boolean onValueAdded(@NonNull K key, @NonNull V value);
    }

    protected final NamespaceBehaviour<K, V, N> delegate;

    private List<VirtualNamespaceContext<?, V, ?, K>> derivedNamespaces;

    protected NamespaceBehaviourWithListeners(final NamespaceBehaviour<K, V, N> delegate) {
        super(delegate.getIdentifier());
        this.delegate = delegate;
    }

    abstract void addListener(KeyedValueAddedListener<K> listener);

    abstract void addListener(PredicateValueAddedListener<K, V> listener);

    @Override
    public abstract void addTo(NamespaceStorageNode storage, K key, V value);

    protected void notifyListeners(final NamespaceStorageNode storage,
            final Iterator<? extends KeyedValueAddedListener<K>> keyListeners, final V value) {
        List<KeyedValueAddedListener<K>> toNotify = new ArrayList<>();
        while (keyListeners.hasNext()) {
            final KeyedValueAddedListener<K> listener = keyListeners.next();
            if (listener.isRequestedValue(this, storage, value)) {
                keyListeners.remove();
                toNotify.add(listener);
            }
        }
        for (KeyedValueAddedListener<K> listener : toNotify) {
            listener.onValueAdded(value);
        }
    }

    protected void notifyDerivedNamespaces(final NamespaceStorageNode storage, final K key, final V value) {
        if (derivedNamespaces != null) {
            for (VirtualNamespaceContext<?, V, ?, K> derived : derivedNamespaces) {
                derived.addedToSourceNamespace(storage, key, value);
            }
        }
    }

    final void addDerivedNamespace(final VirtualNamespaceContext<?, V, ?, K> namespace) {
        if (derivedNamespaces == null) {
            derivedNamespaces = new ArrayList<>();
        }
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

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return super.addToStringAttributes(helper).add("delegate", delegate);
    }
}
