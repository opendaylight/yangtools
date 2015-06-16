/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import java.util.HashMap;
import java.util.Map;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.NamespaceStorageNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceNotAvailableException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

abstract class NamespaceStorageSupport implements NamespaceStorageNode {

    private final Map<Class<?>,Map<?,?>> namespaces = new HashMap<>();


    @Override
    public abstract NamespaceStorageNode getParentNamespaceStorage();

    public abstract NamespaceBehaviour.Registry getBehaviourRegistry();

    protected void checkLocalNamespaceAllowed(Class<? extends IdentifierNamespace<?, ?>> type) {
        // NOOP
    }

    protected <K, V, N extends IdentifierNamespace<K, V>> void onNamespaceElementAdded(Class<N> type, K key, V value) {
        // NOOP
    }

    //<K,V,N extends IdentifierNamespace<K, V>> V
    //public final <K, VT, V extends VT ,N extends IdentifierNamespace<K, V>> VT getFromNamespace(Class<N> type, K key)
    public final <K,V, KT extends K, N extends IdentifierNamespace<K, V>> V getFromNamespace(Class<N> type, KT key)
            throws NamespaceNotAvailableException {
        return getBehaviourRegistry().getNamespaceBehaviour(type).getFrom(this,key);
    }

    public final <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAllFromNamespace(Class<N> type){
        return getBehaviourRegistry().getNamespaceBehaviour(type).getAllFrom(this);
    }

    public final <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAllFromCurrentStmtCtxNamespace(Class<N> type){
        return (Map<K, V>) namespaces.get(type);
    }

    public final <K,V, KT extends K, VT extends V,N extends IdentifierNamespace<K, V>> void addToNs(Class<N> type, KT key, VT value)
            throws NamespaceNotAvailableException {
        getBehaviourRegistry().getNamespaceBehaviour(type).addTo(this,key,value);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public final <K, N extends StatementNamespace<K, ?,?>> void addContextToNamespace(Class<N> type, K key, StmtContext<?, ?, ?> value)
            throws NamespaceNotAvailableException {
        getBehaviourRegistry().getNamespaceBehaviour((Class)type).addTo(this, key, value);
    }

    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> V getFromLocalStorage(Class<N> type, K key) {
        @SuppressWarnings("unchecked")
        Map<K, V> localNamespace = (Map<K,V>) namespaces.get(type);
        if(localNamespace != null) {
            return localNamespace.get(key);
        }
        return null;
    }

    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAllFromLocalStorage(Class<N> type) {
        @SuppressWarnings("unchecked")
        Map<K, V> localNamespace = (Map<K, V>) namespaces.get(type);
        return localNamespace;
    }

    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> void addToLocalStorage(Class<N> type, K key, V value) {
        @SuppressWarnings("unchecked")
        Map<K, V> localNamespace = (Map<K,V>) namespaces.get(type);
        if(localNamespace == null) {
            checkLocalNamespaceAllowed(type);
            localNamespace = new HashMap<>();
            namespaces.put(type, localNamespace);
        }
        localNamespace.put(key,value);
        onNamespaceElementAdded(type,key,value);
    }

}
