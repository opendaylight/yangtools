/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.NamespaceStorageNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.Registry;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceKeyCriterion;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceNotAvailableException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ParserNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class NamespaceStorageSupport implements NamespaceStorageNode {
    private static final Logger LOG = LoggerFactory.getLogger(NamespaceStorageSupport.class);

    private Map<Class<?>, Map<?, ?>> namespaces = ImmutableMap.of();

    /**
     * {@inheritDoc}
     *
     * <p>
     * This method override provides bimorphic invocation on this method invocation between
     * {@link SourceSpecificContext} and the more general {@link NamespaceStorageSupport}. We typically do not expect
     * the two accesses to overlap.
     */
    @Override
    public abstract NamespaceStorageNode getParentNamespaceStorage();

    /**
     * Return the registry of a source context.
     *
     * @return registry of source context
     */
    public abstract @NonNull Registry getBehaviourRegistry();

    // FIXME: 8.0.0: do we really need this method?
    final void checkLocalNamespaceAllowed(final Class<? extends ParserNamespace<?, ?>> type) {
        // Always no-op. We used to route this towards StatementDefinitionContext, but this method remained
        // unimplemented even there.
    }

    /**
     * Occurs when an item is added to model namespace.
     *
     * @throws SourceException instance of SourceException
     */
    protected <K, V, N extends ParserNamespace<K, V>> void onNamespaceElementAdded(final Class<N> type, final K key,
            final V value) {
        // NOOP
    }

    public final <K, V, N extends ParserNamespace<K, V>> Optional<Entry<K, V>> getFromNamespace(
            final Class<N> type, final NamespaceKeyCriterion<K> criterion) {
        return getBehaviourRegistry().getNamespaceBehaviour(type).getFrom(this, criterion);
    }

    public final <K, V, N extends ParserNamespace<K, V>> Map<K, V> getNamespace(final Class<N> type) {
        return getBehaviourRegistry().getNamespaceBehaviour(type).getAllFrom(this);
    }

    @SuppressWarnings("unchecked")
    final <K, V, N extends ParserNamespace<K, V>> Map<K, V> getLocalNamespace(final Class<N> type) {
        final var local = verifyNotNull(namespaces, "Attempted to access swept namespaces of %s", this);
        return (Map<K, V>) local.get(type);
    }

    final <K, V, T extends K, U extends V, N extends ParserNamespace<K, V>> void addToNamespace(
            final Class<N> type, final T key, final U value) {
        getBehaviourRegistry().getNamespaceBehaviour(type).addTo(this, key, value);
    }

    final <K, V, T extends K, U extends V, N extends ParserNamespace<K, V>> void addToNamespace(
            final Class<N> type, final Map<T, U> map) {
        final NamespaceBehaviour<K, V, N> behavior = getBehaviourRegistry().getNamespaceBehaviour(type);
        for (final Entry<T, U> validationBundle : map.entrySet()) {
            behavior.addTo(this, validationBundle.getKey(), validationBundle.getValue());
        }
    }

    /**
     * Associate a context with a key within a namespace.
     *
     * @param type Namespace type
     * @param key Key
     * @param value Context value
     * @param <K> namespace key type
     * @param <N> namespace type
     * @throws NamespaceNotAvailableException when the namespace is not available.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public final <K, N extends StatementNamespace<K, ?,?>> void addContextToNamespace(final Class<N> type, final K key,
            final StmtContext<?, ?, ?> value) {
        getBehaviourRegistry().getNamespaceBehaviour((Class)type).addTo(this, key, value);
    }

    @Override
    public <K, V, N extends ParserNamespace<K, V>> V getFromLocalStorage(final Class<N> type, final K key) {
        final var localNamespace = getLocalNamespace(type);
        return localNamespace == null ? null : localNamespace.get(key);
    }

    @Override
    public <K, V, N extends ParserNamespace<K, V>> Map<K, V> getAllFromLocalStorage(final Class<N> type) {
        return getLocalNamespace(type);
    }

    @Override
    public <K, V, N extends ParserNamespace<K, V>> V putToLocalStorage(final Class<N> type, final K key,
            final V value) {
        final V ret = ensureLocalNamespace(type).put(key, value);
        onNamespaceElementAdded(type, key, value);
        return ret;
    }

    @Override
    public <K, V, N extends ParserNamespace<K, V>> V putToLocalStorageIfAbsent(final Class<N> type, final K key,
            final V value) {
        final V ret = ensureLocalNamespace(type).putIfAbsent(key, value);
        if (ret == null) {
            onNamespaceElementAdded(type, key, value);
        }
        return ret;
    }

    void sweepNamespaces() {
        namespaces = null;
        LOG.trace("Swept namespace storages of {}", this);
    }

    void sweepNamespaces(final Map<Class<?>, SweptNamespace> toWipe) {
        switch (namespaces.size()) {
            case 0:
                namespaces = ImmutableMap.copyOf(toWipe);
                return;
            case 1:
                namespaces = new HashMap<>(namespaces);
                break;
            default:
                // No-op, we are ready
        }

        namespaces.putAll(toWipe);
        LOG.trace("Trimmed namespace storages of {} to {}", this, namespaces.keySet());
    }

    private <K, V, N extends ParserNamespace<K, V>> Map<K, V> ensureLocalNamespace(final Class<N> type) {
        var ret = getLocalNamespace(type);
        if (ret == null) {
            checkLocalNamespaceAllowed(type);
            ret = new HashMap<>(1);

            switch (namespaces.size()) {
                case 0:
                    // We typically have small population of namespaces, use a singleton map
                    namespaces = ImmutableMap.of(type, ret);
                    break;
                case 1:
                    // Alright, time to grow to a full HashMap
                    final Map<Class<?>, Map<?,?>> newNamespaces = new HashMap<>(4);
                    final Entry<Class<?>, Map<?, ?>> entry = namespaces.entrySet().iterator().next();
                    newNamespaces.put(entry.getKey(), entry.getValue());
                    namespaces = newNamespaces;
                    // fall through
                default:
                    // Already expanded, just put the new namespace
                    namespaces.put(type, ret);
            }
        }

        return ret;
    }
}
