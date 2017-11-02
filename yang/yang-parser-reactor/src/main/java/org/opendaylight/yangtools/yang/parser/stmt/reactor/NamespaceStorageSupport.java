/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.NamespaceStorageNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.Registry;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceKeyCriterion;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceNotAvailableException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

abstract class NamespaceStorageSupport implements NamespaceStorageNode {

    private Map<Class<?>, Map<?,?>> namespaces = ImmutableMap.of();

    @Override
    public abstract NamespaceStorageNode getParentNamespaceStorage();

    /**
     * Return the registry of a source context.
     *
     * @return registry of source context
     */
    public abstract Registry getBehaviourRegistry();

    protected void checkLocalNamespaceAllowed(final Class<? extends IdentifierNamespace<?, ?>> type) {
        // NOOP
    }

    /**
     * Occurs when an item is added to model namespace.
     *
     * @throws SourceException instance of SourceException
     */
    protected <K, V, N extends IdentifierNamespace<K, V>> void onNamespaceElementAdded(final Class<N> type, final K key,
            final V value) {
        // NOOP
    }

    @Nonnull
    public final <K, V, KT extends K, N extends IdentifierNamespace<K, V>> V getFromNamespace(final Class<N> type,
            final KT key) throws NamespaceNotAvailableException {
        return getBehaviourRegistry().getNamespaceBehaviour(type).getFrom(this, key);
    }

    public final <K, V, N extends IdentifierNamespace<K, V>> Optional<Entry<K, V>> getFromNamespace(
            final Class<N> type, final NamespaceKeyCriterion<K> criterion) {
        return getBehaviourRegistry().getNamespaceBehaviour(type).getFrom(this, criterion);
    }

    public final <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAllFromNamespace(final Class<N> type) {
        return getBehaviourRegistry().getNamespaceBehaviour(type).getAllFrom(this);
    }

    @SuppressWarnings("unchecked")
    public final <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAllFromCurrentStmtCtxNamespace(
            final Class<N> type) {
        return (Map<K, V>) namespaces.get(type);
    }

    public final <K,V, KT extends K, VT extends V,N extends IdentifierNamespace<K, V>> void addToNs(final Class<N> type,
            final KT key, final VT value) throws NamespaceNotAvailableException {
        getBehaviourRegistry().getNamespaceBehaviour(type).addTo(this,key,value);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public final <K, N extends StatementNamespace<K, ?,?>> void addContextToNamespace(final Class<N> type, final K key,
            final StmtContext<?, ?, ?> value) throws NamespaceNotAvailableException {
        getBehaviourRegistry().getNamespaceBehaviour((Class)type).addTo(this, key, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> V getFromLocalStorage(final Class<N> type, final K key) {
        final Map<K, V> localNamespace = (Map<K, V>) namespaces.get(type);
        return localNamespace == null ? null : localNamespace.get(key);
    }

    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAllFromLocalStorage(final Class<N> type) {
        @SuppressWarnings("unchecked")
        final Map<K, V> localNamespace = (Map<K, V>) namespaces.get(type);
        return localNamespace;
    }

    private <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> ensureLocalNamespace(final Class<N> type) {
        @SuppressWarnings("unchecked")
        Map<K, V> ret = (Map<K,V>) namespaces.get(type);
        if (ret == null) {
            checkLocalNamespaceAllowed(type);
            ret = new HashMap<>(1);

            if (namespaces.isEmpty()) {
                namespaces = new HashMap<>(1);
            }
            namespaces.put(type, ret);
        }

        return ret;
    }

    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> V putToLocalStorage(final Class<N> type, final K key,
            final V value) {
        final V ret = ensureLocalNamespace(type).put(key, value);
        onNamespaceElementAdded(type, key, value);
        return ret;
    }

    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> V putToLocalStorageIfAbsent(final Class<N> type, final K key,
            final V value) {
        final V ret = ensureLocalNamespace(type).putIfAbsent(key, value);
        if (ret == null) {
            onNamespaceElementAdded(type, key, value);
        }
        return ret;
    }
}
