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
         * Global storage, visible from all sources. There is exactly one such storage in any {@link NamespaceStorage}
         * hierarchy and it logically sits on top of it.
         */
        GLOBAL,
        /**
         * Virtual storage providing access for source-visible namespace access, as mediated by {@code belongs-to},
         * {@code import} and {@code include} statements. This storage is by definition read-only.
         */
        ACCESSIBLE_SOURCES,
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
     * {@link NamespaceStorage} for {@link StorageType#GLOBAL}. This is sufficiently special to warrant a dedicated
     * interface, as there is only one instance of this storage in every parser build.
     */
    interface GlobalStorage extends NamespaceStorage {
        @Override
        default StorageType getStorageType() {
            return StorageType.GLOBAL;
        }

        @Override
        default NamespaceStorage getParentStorage() {
            return null;
        }
    }

    /**
     * {@return the type of this storage}
     */
    @NonNull StorageType getStorageType();

    /**
     * Return the parent {@link NamespaceStorage}. If this storage is {@link StorageType#GLOBAL}, this method will
     * return {@code null}.
     *
     * @return Parent storage, if this is not the global storage
     */
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
     * @throws UnsupportedOperationException if {@code type} is a read-only namespace
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
     * @return pre-existing value or {@code null} if there was no previous mapping
     * @throws UnsupportedOperationException if {@code type} is a read-only namespace
     */
    <K, V> @Nullable V putToLocalStorageIfAbsent(ParserNamespace<K, V> type, K key, V value);
}
