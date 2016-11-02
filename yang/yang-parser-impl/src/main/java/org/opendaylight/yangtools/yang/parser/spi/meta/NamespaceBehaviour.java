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
 * Definition / implementation of specific Identifier Namespace behaviour.
 *
 * Namespace behaviour is build on top of tree of {@link NamespaceStorageNode} which represents local context of one of
 * types defined in {@link StorageNodeType}.
 *
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

        StorageNodeType getStorageNodeType();

        @Nullable
        NamespaceStorageNode getParentNamespaceStorage();

        @Nullable
        <K, V, N extends IdentifierNamespace<K, V>> V getFromLocalStorage(Class<N> type, K key);

        @Nullable
        <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAllFromLocalStorage(Class<N> type);

        <K, V, N extends IdentifierNamespace<K, V>> void addToLocalStorage(Class<N> type, K key, V value);

    }

    private final Class<N> identifier;

    protected NamespaceBehaviour(Class<N> identifier) {
        this.identifier = Preconditions.checkNotNull(identifier);
    }

    /**
     *
     * Creates global namespace behaviour for supplied namespace type.
     *
     * Global behaviour stores and loads all values from root {@link NamespaceStorageNode} with type of
     * {@link StorageNodeType#GLOBAL}.
     *
     * @param identifier
     *            Namespace identifier.
     * @param <K> type parameter
     * @param <V> type parameter
     * @param <N> type parameter
     *
     * @return global namespace behaviour for supplied namespace type.
     */
    public static @Nonnull <K, V, N extends IdentifierNamespace<K, V>> NamespaceBehaviour<K, V, N> global(
            Class<N> identifier) {
        return new StorageSpecific<>(identifier, StorageNodeType.GLOBAL);
    }

    /**
     *
     * Creates source-local namespace behaviour for supplied namespace type.
     *
     * Source-local namespace behaviour stores and loads all values from closest {@link NamespaceStorageNode} ancestor
     * with type of {@link StorageNodeType#SOURCE_LOCAL_SPECIAL}.
     *
     * @param identifier
     *            Namespace identifier.
     * @param <K> type parameter
     * @param <V> type parameter
     * @param <N> type parameter
     *
     * @return source-local namespace behaviour for supplied namespace type.
     */
    public static <K, V, N extends IdentifierNamespace<K, V>> NamespaceBehaviour<K, V, N> sourceLocal(
            Class<N> identifier) {
        return new StorageSpecific<>(identifier, StorageNodeType.SOURCE_LOCAL_SPECIAL);
    }

    public static <K, V, N extends IdentifierNamespace<K, V>> NamespaceBehaviour<K, V, N> statementLocal(
           Class<N> identifier) {
       return new StorageSpecific<>(identifier, StorageNodeType.STATEMENT_LOCAL);
   }

    /**
     *
     * Creates tree-scoped namespace behaviour for supplied namespace type.
     *
     * Tree-scoped namespace behaviour search for value in all storage nodes up to the root and stores values in
     * supplied node.
     *
     * @param identifier
     *            Namespace identifier.     *
     * @param <K> type parameter
     * @param <V> type parameter
     * @param <N> type parameter
     *
     * @return tree-scoped namespace behaviour for supplied namespace type.
     */
    public static <K, V, N extends IdentifierNamespace<K, V>> NamespaceBehaviour<K, V, N> treeScoped(Class<N> identifier) {
        return new TreeScoped<>(identifier);
    }

    /**
     * returns value from model namespace storage according to key param class
     *
     * @param storage namespace storage
     * @param key type parameter
     *
     * @return value from model namespace storage according to key param class
     */
    public abstract V getFrom(NamespaceStorageNode storage, K key);

    /**
     * returns all values of a keys of param class from model namespace storage
     *
     * @param storage namespace storage
     *
     * @return all values of keys of param class from model namespace storage
     */
    public abstract Map<K, V> getAllFrom(NamespaceStorageNode storage);

    /**
     * adds key and value to corresponding namespace storage according to param class
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

    protected final V getFromLocalStorage(NamespaceStorageNode storage, K key) {
        return storage.getFromLocalStorage(getIdentifier(), key);
    }

    protected final Map<K, V> getAllFromLocalStorage(NamespaceStorageNode storage) {
        return storage.getAllFromLocalStorage(getIdentifier());
    }

    protected final void addToStorage(NamespaceStorageNode storage, K key, V value) {
        storage.addToLocalStorage(getIdentifier(), key, value);
    }

    static class StorageSpecific<K, V, N extends IdentifierNamespace<K, V>> extends NamespaceBehaviour<K, V, N> {

        StorageNodeType storageType;

        public StorageSpecific(Class<N> identifier, StorageNodeType type) {
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
        public void addTo(NamespaceBehaviour.NamespaceStorageNode storage, K key, V value) {
            NamespaceStorageNode current = findClosestTowardsRoot(storage, storageType);
            addToStorage(current, key, value);
        }

    }

    static class TreeScoped<K, V, N extends IdentifierNamespace<K, V>> extends NamespaceBehaviour<K, V, N> {

        public TreeScoped(Class<N> identifier) {
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
        public void addTo(NamespaceStorageNode storage, K key, V value) {
            addToStorage(storage, key, value);
        }

    }

    protected static NamespaceStorageNode findClosestTowardsRoot(NamespaceStorageNode storage, StorageNodeType type) {
        NamespaceStorageNode current = storage;
        while (current != null && current.getStorageNodeType() != type) {
            current = current.getParentNamespaceStorage();
        }
        return current;
    }
}
