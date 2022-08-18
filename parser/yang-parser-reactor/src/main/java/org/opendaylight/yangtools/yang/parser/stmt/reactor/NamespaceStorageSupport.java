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
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.NamespaceStorageNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.Registry;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceKeyCriterion;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceNotAvailableException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ParserNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class NamespaceStorageSupport implements NamespaceStorageNode {
    private static final Logger LOG = LoggerFactory.getLogger(NamespaceStorageSupport.class);

    private Map<ParserNamespace<?, ?>, Map<?, ?>> namespaces = ImmutableMap.of();

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
    final void checkLocalNamespaceAllowed(final ParserNamespace<?, ?> type) {
        // Always no-op. We used to route this towards StatementDefinitionContext, but this method remained
        // unimplemented even there.
    }

    /**
     * Occurs when an item is added to model namespace.
     *
     * @throws SourceException instance of SourceException
     */
    protected <K, V> void onNamespaceElementAdded(final ParserNamespace<K, V> type, final K key, final V value) {
        // NOOP
    }

    public final <K, V> Optional<Entry<K, V>> getFromNamespace(final ParserNamespace<K, V> type,
            final NamespaceKeyCriterion<K> criterion) {
        return getBehaviourRegistry().getNamespaceBehaviour(type).getFrom(this, criterion);
    }

    public final <K, V> Map<K, V> getNamespace(final ParserNamespace<K, V> type) {
        return getBehaviourRegistry().getNamespaceBehaviour(type).getAllFrom(this);
    }

    @SuppressWarnings("unchecked")
    final <K, V> Map<K, V> getLocalNamespace(final ParserNamespace<K, V> type) {
        return (Map<K, V>) accessNamespaces().get(type);
    }

    final <K, V, T extends K, U extends V> void addToNamespace(final ParserNamespace<K, V> type, final T key,
            final U value) {
        getBehaviourRegistry().getNamespaceBehaviour(type).addTo(this, key, value);
    }

    final <K, V, T extends K, U extends V> void addToNamespace(final ParserNamespace<K, V> type, final Map<T, U> map) {
        final NamespaceBehaviour<K, V> behavior = getBehaviourRegistry().getNamespaceBehaviour(type);
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
     * @param <D> declared statement type
     * @param <E> effective statement type
     * @throws NamespaceNotAvailableException when the namespace is not available.
     */
    public final <K, D extends DeclaredStatement<?>, E extends EffectiveStatement<?, D>> void addContextToNamespace(
            final ParserNamespace<K, StmtContext<?, D, E>> type, final K key, final StmtContext<?, D, E> value) {
        getBehaviourRegistry().getNamespaceBehaviour(type).addTo(this, key, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <K, V> V getFromLocalStorage(final ParserNamespace<K, V> type, final K key) {
        final Map<K, V> localNamespace = (Map<K, V>) accessNamespaces().get(type);
        return localNamespace == null ? null : localNamespace.get(key);
    }

    @Override
    public <K, V> Map<K, V> getAllFromLocalStorage(final ParserNamespace<K, V> type) {
        @SuppressWarnings("unchecked")
        final Map<K, V> localNamespace = (Map<K, V>) accessNamespaces().get(type);
        return localNamespace;
    }

    @Override
    public <K, V> V putToLocalStorage(final ParserNamespace<K, V> type, final K key, final V value) {
        final V ret = ensureLocalNamespace(type).put(key, value);
        onNamespaceElementAdded(type, key, value);
        return ret;
    }

    @Override
    public <K, V> V putToLocalStorageIfAbsent(final ParserNamespace<K, V> type, final K key, final V value) {
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

    void sweepNamespaces(final Map<ParserNamespace<?, ?>, SweptNamespace> toWipe) {
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

    private Map<ParserNamespace<?, ?>, Map<?, ?>> accessNamespaces() {
        return verifyNotNull(namespaces, "Attempted to access swept namespaces of %s", this);
    }

    private <K, V> Map<K, V> ensureLocalNamespace(final ParserNamespace<K, V> type) {
        @SuppressWarnings("unchecked")
        Map<K, V> ret = (Map<K,V>) accessNamespaces().get(type);
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
                    final Map<ParserNamespace<?, ?>, Map<?,?>> newNamespaces = new HashMap<>(4);
                    final Entry<ParserNamespace<?, ?>, Map<?, ?>> entry = namespaces.entrySet().iterator().next();
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
