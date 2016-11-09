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
import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.NamespaceStorageNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceNotAvailableException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;

abstract class NamespaceStorageSupport implements NamespaceStorageNode {

    private Map<Class<?>, Map<?,?>> namespaces = ImmutableMap.of();

    @Override
    public abstract NamespaceStorageNode getParentNamespaceStorage();

    public abstract NamespaceBehaviour.Registry getBehaviourRegistry();

    protected void checkLocalNamespaceAllowed(final Class<? extends IdentifierNamespace<?, ?>> type) {
        // NOOP
    }

    protected <K, V, N extends IdentifierNamespace<K, V>> void onNamespaceElementAdded(final Class<N> type, final K key, final V value) {
        // NOOP
    }

    public final <K,V, KT extends K, N extends IdentifierNamespace<K, V>> V getFromNamespace(final Class<N> type, final KT key)
            throws NamespaceNotAvailableException {
        return getBehaviourRegistry().getNamespaceBehaviour(type).getFrom(this,key);
    }

    public final <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAllFromNamespace(final Class<N> type){
        return getBehaviourRegistry().getNamespaceBehaviour(type).getAllFrom(this);
    }

    @SuppressWarnings("unchecked")
    public final <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAllFromCurrentStmtCtxNamespace(final Class<N> type){
        return (Map<K, V>) namespaces.get(type);
    }

    public final <K,V, KT extends K, VT extends V,N extends IdentifierNamespace<K, V>> void addToNs(final Class<N> type, final KT key, final VT value)
            throws NamespaceNotAvailableException {
        getBehaviourRegistry().getNamespaceBehaviour(type).addTo(this,key,value);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public final <K, N extends StatementNamespace<K, ?,?>> void addContextToNamespace(final Class<N> type, final K key, final StmtContext<?, ?, ?> value)
            throws NamespaceNotAvailableException {
        getBehaviourRegistry().getNamespaceBehaviour((Class)type).addTo(this, key, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> V getFromLocalStorage(final Class<N> type, final K key) {
        final Map<K, V> localNamespace = (Map<K,V>) namespaces.get(type);

        V potential = null;
        if (localNamespace != null) {
            potential = localNamespace.get(key);
        }

        if (potential == null && Utils.isModuleIdentifierWithoutSpecifiedRevision(key)) {
            potential = getRegardlessOfRevision((ModuleIdentifier)key,(Map<ModuleIdentifier,V>)localNamespace);
        }

        return potential;
    }

    private static <K, V, N extends IdentifierNamespace<K, V>> V getRegardlessOfRevision(final ModuleIdentifier key,
            final Map<ModuleIdentifier, V> localNamespace) {

        if (localNamespace == null) {
            return null;
        }

        final Set<Entry<ModuleIdentifier, V>> entrySet = localNamespace.entrySet();
        for (final Entry<ModuleIdentifier, V> entry : entrySet) {
            final ModuleIdentifier moduleIdentifierInMap = entry.getKey();
            if (moduleIdentifierInMap.getName().equals(key.getName())) {
                return entry.getValue();
            }
        }

        return null;
    }

    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAllFromLocalStorage(final Class<N> type) {
        @SuppressWarnings("unchecked")
        final Map<K, V> localNamespace = (Map<K, V>) namespaces.get(type);
        return localNamespace;
    }

    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> void addToLocalStorage(final Class<N> type, final K key, final V value) {
        @SuppressWarnings("unchecked")
        Map<K, V> localNamespace = (Map<K,V>) namespaces.get(type);
        if (localNamespace == null) {
            checkLocalNamespaceAllowed(type);
            localNamespace = new HashMap<>(1);

            if (namespaces.isEmpty()) {
                namespaces = new HashMap<>(1);
            }
            namespaces.put(type, localNamespace);
        }
        localNamespace.put(key,value);
        onNamespaceElementAdded(type,key,value);
    }

}
