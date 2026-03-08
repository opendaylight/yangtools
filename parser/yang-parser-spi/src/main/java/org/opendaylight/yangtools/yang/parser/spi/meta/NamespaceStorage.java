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
 * A hierarchical entity storing a portion (or entirety) of a {@link ParserNamespace}. Storages are organized in a
 * hierarchy indicated by {@link #level()}:
 * <ol>
 *   <li>the top-most node is the {@link Level#GLOBAL} storage, which contains</li>
 *   <li>one {@link Level#ACCESSIBLE_SOURCES} storage for each YANG/YIN source, each of which contains</li>
 *   <li>exactly one {@link Level#SOURCE} storage, each of which contains<li>
 *   <li>any number of {@link Level#STATEMENT} storages, each of which can contain nested {@link Level#STATEMENT}
 *       storages</li>
 * </ol>
 */
public interface NamespaceStorage {
    /**
     * The {@link NamespaceStorage} levels, in the order of descending scope.
     */
    enum Level {
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
         * Storage of a particular source.
         */
        SOURCE,
        /**
         * Storage of a single statement.
         */
        STATEMENT,
    }

    /**
     * {@link NamespaceStorage} for {@link Level#GLOBAL}.
     */
    interface Global extends NamespaceStorage {
        @Override
        default Level level() {
            return Level.GLOBAL;
        }

        @Override
        default NamespaceStorage getParentStorage() {
            return null;
        }
    }

    /**
     * {@link NamespaceStorage} for {@link Level#ACCESSIBLE_SOURCES}.
     */
    interface AccessibleSources extends NamespaceStorage {
        @Override
        default Level level() {
            return Level.ACCESSIBLE_SOURCES;
        }

        @Override
        default <K, V> V putToLocalStorage(final ParserNamespace<K, V> type, final K key, final V value) {
            throw new UnsupportedOperationException();
        }

        @Override
        default <K, V> V putToLocalStorageIfAbsent(final ParserNamespace<K, V> type, final K key, final V value) {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * {@link NamespaceStorage} for {@link Level#SOURCE}.
     */
    interface Source extends NamespaceStorage {
        @Override
        default Level level() {
            return Level.SOURCE;
        }
    }

    /**
     * {@link NamespaceStorage} for {@link Level#STATEMENT}.
     */
    interface Statement extends NamespaceStorage {
        @Override
        default Level level() {
            return Level.STATEMENT;
        }
    }

    /**
     * {@return the {@link Level}  of this storage}
     */
    @NonNull Level level();

    /**
     * Return the parent {@link NamespaceStorage}. If this storage is {@link Level#GLOBAL}, this method will
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
