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
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceNotAvailableException;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceStorage;
import org.opendaylight.yangtools.yang.parser.spi.meta.ParserNamespace;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for our {@link NamespaceStorage} implementations. There are two of those:
 * <ol>
 *   <li>{@link BuildGlobalContext}, servicing all global namespaces</li>
 *   <li>{@link ReactorStmtCtx}, servicing all other namespaces</li>
 * </ol>
 */
abstract sealed class AbstractNamespaceStorage implements NamespaceStorage permits BuildGlobalContext, ReactorStmtCtx {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractNamespaceStorage.class);

    private Map<ParserNamespace<?, ?>, Map<?, ?>> namespaces = ImmutableMap.of();

    /**
     * Get access to a {@link ParserNamespace}.
     *
     * @param <K> key type
     * @param <V> value type
     * @param namespace Namespace type
     * @return Namespace behaviour
     * @throws NamespaceNotAvailableException when the namespace is not available
     * @throws NullPointerException if {@code namespace} is {@code null}
     */
    abstract <K, V> @NonNull NamespaceAccess<K, V> accessNamespace(ParserNamespace<K, V> namespace);

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
    <K, V> void onNamespaceElementAdded(final ParserNamespace<K, V> type, final K key, final V value) {
        // NOOP
    }

    final <K, V> Map<K, V> getNamespace(final ParserNamespace<K, V> type) {
        return accessNamespace(type).allFrom(this);
    }

    @SuppressWarnings("unchecked")
    final <K, V> Map<K, V> getLocalNamespace(final ParserNamespace<K, V> type) {
        final var local = verifyNotNull(namespaces, "Attempted to access swept namespaces of %s", this);
        return (Map<K, V>) local.get(type);
    }

    final <K, V> void addToNamespace(final ParserNamespace<K, V> type, final K key, final V value) {
        accessNamespace(type).valueTo(this, key, value);
    }

    @Override
    public <K, V> V getFromLocalStorage(final ParserNamespace<K, V> type, final K key) {
        final var localNamespace = getLocalNamespace(type);
        return localNamespace == null ? null : localNamespace.get(key);
    }

    @Override
    public <K, V> Map<K, V> getAllFromLocalStorage(final ParserNamespace<K, V> type) {
        return getLocalNamespace(type);
    }

    @Override
    public <K, V> V putToLocalStorage(final ParserNamespace<K, V> type, final K key, final V value) {
        final V ret = ensureLocalNamespace(type).put(key, value);
        onNamespaceElementAdded(type, key, value);
        return ret;
    }

    @Override
    public final <K, V> V putToLocalStorageIfAbsent(final ParserNamespace<K, V> type, final K key, final V value) {
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

    private <K, V> Map<K, V> ensureLocalNamespace(final ParserNamespace<K, V> type) {
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
                    final var newNamespaces = new HashMap<ParserNamespace<?, ?>, Map<?, ?>>(4);
                    final var entry = namespaces.entrySet().iterator().next();
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
