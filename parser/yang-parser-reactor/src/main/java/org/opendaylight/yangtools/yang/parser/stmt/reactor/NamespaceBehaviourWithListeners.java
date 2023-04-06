/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceKeyCriterion;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceStorage;

abstract class NamespaceBehaviourWithListeners<K, V> extends NamespaceAccess<K, V> {
    abstract static class ValueAddedListener<K> {
        private final NamespaceStorage ctxNode;

        ValueAddedListener(final NamespaceStorage ctxNode) {
            this.ctxNode = requireNonNull(ctxNode);
        }

        final NamespaceStorage getCtxNode() {
            return ctxNode;
        }
    }

    abstract static class KeyedValueAddedListener<K> extends ValueAddedListener<K> {
        private final K key;

        KeyedValueAddedListener(final NamespaceStorage contextNode, final K key) {
            super(contextNode);
            this.key = requireNonNull(key);
        }

        final K getKey() {
            return key;
        }

        final <V> boolean isRequestedValue(final NamespaceAccess<K, ?> access, final NamespaceStorage storage,
                final V value) {
            return value == access.valueFrom(getCtxNode(), key);
        }

        abstract void onValueAdded(Object value);
    }

    abstract static class PredicateValueAddedListener<K, V> extends ValueAddedListener<K> {
        PredicateValueAddedListener(final NamespaceStorage contextNode) {
            super(contextNode);
        }

        abstract boolean onValueAdded(@NonNull K key, @NonNull V value);
    }

    protected final NamespaceBehaviour<K, V> delegate;

    private List<VirtualNamespaceContext<?, V, K>> derivedNamespaces;

    protected NamespaceBehaviourWithListeners(final NamespaceStorage globalStorage,
            final NamespaceBehaviour<K, V> delegate) {
        super(globalStorage);
        this.delegate = requireNonNull(delegate);
    }

    abstract void addListener(KeyedValueAddedListener<K> listener);

    abstract void addListener(PredicateValueAddedListener<K, V> listener);

    protected void notifyListeners(final NamespaceStorage storage,
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

    protected void notifyDerivedNamespaces(final NamespaceStorage storage, final K key, final V value) {
        if (derivedNamespaces != null) {
            for (VirtualNamespaceContext<?, V, K> derived : derivedNamespaces) {
                derived.addedToSourceNamespace(storage, key, value);
            }
        }
    }

    final void addDerivedNamespace(final VirtualNamespaceContext<?, V, K> namespace) {
        if (derivedNamespaces == null) {
            derivedNamespaces = new ArrayList<>();
        }
        derivedNamespaces.add(namespace);
    }

    @Override
    final V valueFrom(final NamespaceStorage storage, final K key) {
        return delegate.getFrom(storage, key);
    }

    @Override
    final Map<K, V> allFrom(final NamespaceStorage storage) {
        return delegate.getAllFrom(storage);
    }

    @Override
    final Optional<Entry<K, V>> entryFrom(final NamespaceStorage storage, final NamespaceKeyCriterion<K> criterion) {
        return delegate.getFrom(storage, criterion);
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this).add("behaviour", delegate).toString();
    }
}
