/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.annotations.Beta;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Support work with namespace content.
 */
@Beta
public interface NamespaceStmtCtx extends CommonStmtCtx {
    /**
     * Return the selected namespace.
     *
     * @param <K> namespace key type
     * @param <V> namespace value type
     * @param <N> namespace type
     * @param nsType namespace type class
     * @return Namespace contents, if available
     */
    <K, V, N extends ParserNamespace<K, V>> @Nullable Map<K, V> namespace(Class<@NonNull N> nsType);

    /**
     * Return a value associated with specified key within a namespace.
     *
     * @param nsType Namespace type
     * @param key Key
     * @param <K> namespace key type
     * @param <V> namespace value type
     * @param <N> namespace type
     * @param <T> key type
     * @return Value, or null if there is no element
     * @throws NamespaceNotAvailableException when the namespace is not available.
     */
    <K, V, T extends K, N extends ParserNamespace<K, V>> @Nullable V namespaceItem(Class<@NonNull N> nsType, T key);

    /**
     * Return the portion of specified namespace stored in this node. Depending on namespace behaviour this may or may
     * not represent the complete contents of the namespace as available via {@link #namespace(Class)}.
     *
     * <p>
     * This partial view is useful when the need is not to perform a proper namespace lookup, but rather act on current
     * statement's contribution to the namespace.
     *
     * @param <K> namespace key type
     * @param <V> namespace value type
     * @param <N> namespace type
     * @param nsType namespace type class
     * @return Namespace portion stored in this node, if available
     */
    <K, V, N extends ParserNamespace<K, V>> @Nullable Map<K, V> localNamespacePortion(Class<@NonNull N> nsType);

    /**
     * Return the selected namespace.
     *
     * @param <K> namespace key type
     * @param <V> namespace value type
     * @param <N> namespace type
     * @param nsType namespace type class
     * @return Namespace contents, if available
     */
    // TODO: migrate users away
    default <K, V, N extends ParserNamespace<K, V>> Map<K, V> getAllFromNamespace(final Class<N> nsType) {
        return namespace(nsType);
    }

    /**
     * Return a value associated with specified key within a namespace.
     *
     * @param type Namespace type
     * @param key Key
     * @param <K> namespace key type
     * @param <V> namespace value type
     * @param <N> namespace type
     * @param <T> key type
     * @return Value, or null if there is no element
     * @throws NamespaceNotAvailableException when the namespace is not available.
     */
    // TODO: migrate users away
    default <K, V, T extends K, N extends ParserNamespace<K, V>>
            @Nullable V getFromNamespace(final Class<@NonNull N> type, final T key) {
        return namespaceItem(type, key);
    }
}
