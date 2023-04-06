/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A hierarchical entity storing a portion (or entirety) of a {@link ParserNamespace}.
 */
// TODO: describe how the hierarchy is organized
public interface NamespaceStorage {
    /**
     * Enumeration of all possible types of storage.
     */
    enum StorageType {
        /**
         * Global storage, visible from all sources.
         */
        GLOBAL,
        /**
         * Storage of the root statement of a particular source and any sources it is importing.
         */
        // FIXME: 7.0.0: this is a misnomer and should be renamed
        SOURCE_LOCAL_SPECIAL,
        /**
         * Storage of a single statement.
         */
        STATEMENT_LOCAL,
        /**
         * Storage of the root statement of a particular source.
         */
        ROOT_STATEMENT_LOCAL
    }

    /**
     * Return the type of this storage.
     *
     * @return The type of this storage
     */
    @NonNull StorageType getStorageType();

    @Nullable NamespaceStorage getParentStorage();

    <K, V> @Nullable V getFromLocalStorage(ParserNamespace<K, V> type, K key);

    <K, V> @Nullable Map<K, V> getAllFromLocalStorage(ParserNamespace<K, V> type);

    /**
     * Populate specified namespace with a key/value pair, overwriting previous contents. Similar to
     * {@link Map#put(Object, Object)}.
     *
     * @param <K> Namespace key type
     * @param <V> Namespace value type
     * @param type Namespace identifier
     * @param key Key
     * @param value Value
     * @return Previously-stored value, or null if the key was not present
     */
    <K, V> @Nullable V putToLocalStorage(ParserNamespace<K, V> type, K key, V value);

    /**
     * Populate specified namespace with a key/value pair unless the key is already associated with a value. Similar
     * to {@link Map#putIfAbsent(Object, Object)}.
     *
     * @param <K> Namespace key type
     * @param <V> Namespace value type
     * @param type Namespace identifier
     * @param key Key
     * @param value Value
     * @return Preexisting value or null if there was no previous mapping
     */
    <K, V> @Nullable V putToLocalStorageIfAbsent(ParserNamespace<K, V> type, K key, V value);
}
