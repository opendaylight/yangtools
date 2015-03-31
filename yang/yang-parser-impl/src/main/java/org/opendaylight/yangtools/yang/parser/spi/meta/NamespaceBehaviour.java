package org.opendaylight.yangtools.yang.parser.spi.meta;

import javax.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;

public abstract class NamespaceBehaviour<K,V, N extends IdentifierNamespace<K, V>> implements Identifiable<Class<N>>{

    public enum StorageNodeType {
        Global,
        SourceLocalSpecial,
        StatementLocal
    }

    public interface Registry {

        abstract <K, V, N extends IdentifierNamespace<K, V>> NamespaceBehaviour<K, V, N> getNamespaceBehaviour(Class<N> type);

    }

    public interface NamespaceStorageNode {

        StorageNodeType getStorageNodeType();

        @Nullable NamespaceStorageNode getParentNamespaceStorage();

        @Nullable  <K, V, N extends IdentifierNamespace<K, V>> V getFromLocalStorage(Class<N> type, K key);

        @Nullable  <K, V, N extends IdentifierNamespace<K, V>> void addToLocalStorage(Class<N> type, K key, V value);

    }

    private final Class<N> identifier;


    protected NamespaceBehaviour(Class<N> identifier) {
        this.identifier = identifier;
    }


    public static <K,V, N extends IdentifierNamespace<K, V>> NamespaceBehaviour<K,V,N> global(Class<N> identifier) {
        return new StorageSpecific<>(identifier, StorageNodeType.Global);
    }

    public static <K,V, N extends IdentifierNamespace<K, V>> NamespaceBehaviour<K,V,N> sourceLocal(Class<N> identifier) {
        return new StorageSpecific<>(identifier, StorageNodeType.SourceLocalSpecial);
    }

    public static <K,V, N extends IdentifierNamespace<K, V>> NamespaceBehaviour<K,V,N> treeScoped(Class<N> identifier) {
        return new TreeScoped<>(identifier);
    }

    public abstract V getFrom(NamespaceStorageNode storage, K key);
    public abstract void addTo(NamespaceStorageNode storage,K key,V value);


    @Override
    public Class<N> getIdentifier() {
        return identifier;
    }

    protected final V getFromLocalStorage(NamespaceStorageNode storage, K key) {
        return storage.getFromLocalStorage(getIdentifier(), key);
    }

    protected final void addToStorage(NamespaceStorageNode storage,K key,V value) {
        storage.addToLocalStorage(getIdentifier(),key,value);
    }

    static class StorageSpecific<K,V, N extends IdentifierNamespace<K, V>> extends NamespaceBehaviour<K, V, N> {

        StorageNodeType storageType;

        public StorageSpecific(Class<N> identifier, StorageNodeType type) {
            super(identifier);
            storageType = type;
        }

        @Override
        public V getFrom(final NamespaceStorageNode storage, final K key) {
            NamespaceStorageNode current = storage;
            while(current.getStorageNodeType() != storageType) {
                current = current.getParentNamespaceStorage();
            }
            return getFromLocalStorage(current,key);
        }

        @Override
        public void addTo(NamespaceBehaviour.NamespaceStorageNode storage, K key, V value) {
            NamespaceStorageNode current = storage;
            while(current.getStorageNodeType() != storageType) {
                current = current.getParentNamespaceStorage();
            }
            addToStorage(current, key, value);
        }

    }

    static class TreeScoped<K,V, N extends IdentifierNamespace<K, V>> extends NamespaceBehaviour<K, V, N> {

        public TreeScoped(Class<N> identifier) {
            super(identifier);
        }

        @Override
        public V getFrom(final NamespaceStorageNode storage, final K key) {
            NamespaceStorageNode current = storage;
            while(current != null) {
                final V val = getFromLocalStorage(current, key);
                if(val != null) {
                    return val;
                }
                current = current.getParentNamespaceStorage();
            }
            return null;
        }

        @Override
        public void addTo(NamespaceStorageNode storage,K key, V value) {
            addToStorage(storage, key, value);
        }

    }

}
