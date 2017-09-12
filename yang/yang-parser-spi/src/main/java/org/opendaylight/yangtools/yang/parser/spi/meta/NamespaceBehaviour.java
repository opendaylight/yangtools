/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.base.Preconditions;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;

/**
 * Definition / implementation of specific Identifier Namespace behaviour. A namespace behaviour is built on top
 * of a tree of {@link NamespaceStorageNode} which represents local context of one of types defined
 * n {@link StorageNodeType}.
 *
 * <p>
 * For common behaviour models please use static factories {@link #global(Class)}, {@link #sourceLocal(Class)} and
 * {@link #treeScoped(Class)}.
 *
 * @param <K>
 *            Key type
 * @param <V>
 *            Value type
 * @param <N>
 *            Namespace Type
 */
public abstract class NamespaceBehaviour<K, V, N extends IdentifierNamespace<K, V>> implements Identifiable<Class<N>> {

    public enum StorageNodeType {
        GLOBAL, SOURCE_LOCAL_SPECIAL, STATEMENT_LOCAL, ROOT_STATEMENT_LOCAL
    }

    public interface Registry {
        <K, V, N extends IdentifierNamespace<K, V>> NamespaceBehaviour<K, V, N> getNamespaceBehaviour(Class<N> type);
    }

    public interface NamespaceStorageNode {
        /**
         * Return local namespace behaviour type.
         *
         * @return local namespace behaviour type {@link NamespaceBehaviour}
         */
        StorageNodeType getStorageNodeType();

        @Nullable
        NamespaceStorageNode getParentNamespaceStorage();

        @Nullable
        <K, V, N extends IdentifierNamespace<K, V>> V getFromLocalStorage(Class<N> type, K key);

        @Nullable
        <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAllFromLocalStorage(Class<N> type);

        /**
         * Populate specified namespace with a key/value pair, overwriting previous contents. Similar to
         * {@link Map#put(Object, Object)}.
         *
         * @param type Namespace identifier
         * @param key Key
         * @param value Value
         * @return Previously-stored value, or null if the key was not present
         */
        @Nullable
        <K, V, N extends IdentifierNamespace<K, V>> V putToLocalStorage(Class<N> type, K key, V value);

        /**
         * Populate specified namespace with a key/value pair unless the key is already associated with a value. Similar
         * to {@link Map#putIfAbsent(Object, Object)}.
         *
         * @param type Namespace identifier
         * @param key Key
         * @param value Value
         * @return Preexisting value or null if there was no previous mapping
         */
        @Nullable
        <K, V, N extends IdentifierNamespace<K, V>> V putToLocalStorageIfAbsent(Class<N> type, K key, V value);
    }

    private final Class<N> identifier;

    protected NamespaceBehaviour(final Class<N> identifier) {
        this.identifier = Preconditions.checkNotNull(identifier);
    }

    /**
     * Creates a global namespace behaviour for supplied namespace type. Global behaviour stores and loads all values
     * from root {@link NamespaceStorageNode} with type of {@link StorageNodeType#GLOBAL}.
     *
     * @param identifier
     *            Namespace identifier.
     * @param <K> type parameter
     * @param <V> type parameter
     * @param <N> type parameter
     * @return global namespace behaviour for supplied namespace type.
     */
    public static @Nonnull <K, V, N extends IdentifierNamespace<K, V>> NamespaceBehaviour<K, V, N> global(
            final Class<N> identifier) {
        return new StorageSpecific<>(identifier, StorageNodeType.GLOBAL);
    }

    /**
     * Creates source-local namespace behaviour for supplied namespace type. Source-local namespace behaviour stores
     * and loads all values from closest {@link NamespaceStorageNode} ancestor with type
     * of {@link StorageNodeType#SOURCE_LOCAL_SPECIAL}.
     *
     * @param identifier
     *            Namespace identifier.
     * @param <K> type parameter
     * @param <V> type parameter
     * @param <N> type parameter
     * @return source-local namespace behaviour for supplied namespace type.
     */
    public static <K, V, N extends IdentifierNamespace<K, V>> NamespaceBehaviour<K, V, N> sourceLocal(
            final Class<N> identifier) {
        return new StorageSpecific<>(identifier, StorageNodeType.SOURCE_LOCAL_SPECIAL);
    }

    public static <K, V, N extends IdentifierNamespace<K, V>> NamespaceBehaviour<K, V, N> statementLocal(
           final Class<N> identifier) {
        return new StorageSpecific<>(identifier, StorageNodeType.STATEMENT_LOCAL);
    }

    /**
     * Creates tree-scoped namespace behaviour for supplied namespace type. Tree-scoped namespace behaviour searches
     * for value in all storage nodes up to the root and stores values in supplied node.
     *
     * @param identifier
     *            Namespace identifier.
     * @param <K> type parameter
     * @param <V> type parameter
     * @param <N> type parameter
     * @return tree-scoped namespace behaviour for supplied namespace type.
     */
    public static <K, V, N extends IdentifierNamespace<K, V>> NamespaceBehaviour<K, V, N> treeScoped(
            final Class<N> identifier) {
        return new TreeScoped<>(identifier);
    }

    /**
     * Returns a value from model namespace storage according to key param class.
     *
     * @param storage namespace storage
     * @param key type parameter
     * @return value from model namespace storage according to key param class
     */
    public abstract V getFrom(NamespaceStorageNode storage, K key);

    /**
     * Returns all values of a keys of param class from model namespace storage
     *
     * @param storage namespace storage
     * @return all values of keys of param class from model namespace storage
     */
    public abstract Map<K, V> getAllFrom(NamespaceStorageNode storage);

    /**
     * Adds a key/value to corresponding namespace storage according to param class.
     *
     * @param storage namespace storage
     * @param key type parameter
     * @param value type parameter
     */
    public abstract void addTo(NamespaceStorageNode storage, K key, V value);

    @Override
    public Class<N> getIdentifier() {
        return identifier;
    }

    protected final V getFromLocalStorage(final NamespaceStorageNode storage, final K key) {
        return storage.getFromLocalStorage(getIdentifier(), key);
    }

    protected final Map<K, V> getAllFromLocalStorage(final NamespaceStorageNode storage) {
        return storage.getAllFromLocalStorage(getIdentifier());
    }

    protected final void addToStorage(final NamespaceStorageNode storage, final K key, final V value) {
        storage.putToLocalStorage(getIdentifier(), key, value);
    }

    static class StorageSpecific<K, V, N extends IdentifierNamespace<K, V>> extends NamespaceBehaviour<K, V, N> {

        StorageNodeType storageType;

        public StorageSpecific(final Class<N> identifier, final StorageNodeType type) {
            super(identifier);
            storageType = Preconditions.checkNotNull(type);
        }

        @Override
        public V getFrom(final NamespaceStorageNode storage, final K key) {
            NamespaceStorageNode current = findClosestTowardsRoot(storage, storageType);
            return getFromLocalStorage(current, key);
        }

        @Override
        public Map<K, V> getAllFrom(final NamespaceStorageNode storage) {
            NamespaceStorageNode current = storage;
            while (current.getStorageNodeType() != storageType) {
                current = current.getParentNamespaceStorage();
            }

            return getAllFromLocalStorage(current);
        }

        @Override
        public void addTo(final NamespaceBehaviour.NamespaceStorageNode storage, final K key, final V value) {
            NamespaceStorageNode current = findClosestTowardsRoot(storage, storageType);
            addToStorage(current, key, value);
        }

    }

    static class TreeScoped<K, V, N extends IdentifierNamespace<K, V>> extends NamespaceBehaviour<K, V, N> {

        public TreeScoped(final Class<N> identifier) {
            super(identifier);
        }

        @Override
        public V getFrom(final NamespaceStorageNode storage, final K key) {
            NamespaceStorageNode current = storage;
            while (current != null) {
                final V val = getFromLocalStorage(current, key);
                if (val != null) {
                    return val;
                }
                current = current.getParentNamespaceStorage();
            }
            return null;
        }

        @Override
        public Map<K, V> getAllFrom(final NamespaceStorageNode storage) {
            NamespaceStorageNode current = storage;
            while (current != null) {
                final Map<K, V> val = getAllFromLocalStorage(current);
                if (val != null) {
                    return val;
                }
                current = current.getParentNamespaceStorage();
            }
            return null;
        }

        @Override
        public void addTo(final NamespaceStorageNode storage, final K key, final V value) {
            addToStorage(storage, key, value);
        }

    }

    protected static NamespaceStorageNode findClosestTowardsRoot(final NamespaceStorageNode storage, final StorageNodeType type) {
        NamespaceStorageNode current = storage;
        while (current != null && current.getStorageNodeType() != type) {
            current = current.getParentNamespaceStorage();
        }
        return current;
    }
}
