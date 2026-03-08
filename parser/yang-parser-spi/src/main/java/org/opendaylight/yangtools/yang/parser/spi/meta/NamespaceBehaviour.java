/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceStorage.Level;

/**
 * Definition / implementation of specific Identifier Namespace behaviour. A namespace behaviour is built on top
 * of a tree of {@link NamespaceStorage} which represents local context of one of types defined in {@link Level}.
 *
 * <p>For common behaviour models please use static factories {@link #global(ParserNamespace)},
 * {@link #sourceLocal(ParserNamespace)} and {@link #treeScoped(ParserNamespace)}.
 *
 * @param <K> Key type
 * @param <V> Value type
 */
public abstract class NamespaceBehaviour<K, V> {
    private final @NonNull ParserNamespace<K, V> namespace;

    protected NamespaceBehaviour(final ParserNamespace<K, V> namespace) {
        this.namespace = requireNonNull(namespace);
    }

    /**
     * {@return the {@link ParserNamespace} serviced by this behaviour}
     */
    public final @NonNull ParserNamespace<K, V> namespace() {
        return namespace;
    }

    /**
     * Creates a global namespace behaviour for supplied namespace. Resulting behaviour stores and loads all values from
     * the {@link NamespaceStorage.Global} storage.
     *
     * @param <K> Namespace key type
     * @param <V> Namespace value type
     * @param namespace Namespace identifier
     * @return namespace behaviour for supplied namespace type.
     */
    public static <K, V> @NonNull NamespaceBehaviour<K, V> global(final ParserNamespace<K, V> namespace) {
        return new Global<>(namespace);
    }

    /**
     * Creates a source namespace behaviour for supplied namespace type. Resulting behaviour stores and loads all values
     * from closest {@link NamespaceStorage.Source} ancestor.
     *
     * @param <K> Namespace key type
     * @param <V> Namespace value type
     * @param namespace Namespace identifier
     * @return namespace behaviour for supplied namespace type.
     */
    public static <K, V> @NonNull NamespaceBehaviour<K, V> sourceLocal(final ParserNamespace<K, V> namespace) {
        return new SourceLocal<>(namespace);
    }

    /**
     * Creates a statement namespace behaviour for supplied namespace type. Resulting behaviour stores and loads all
     * values from the closest {@link NamespaceStorage.Statement} ancestor.
     *
     * @param <K> Namespace key type
     * @param <V> Namespace value type
     * @param namespace Namespace identifier
     * @return namespace behaviour for supplied namespace type.
     */
    public static <K, V> @NonNull NamespaceBehaviour<K, V> statementLocal(final ParserNamespace<K, V> namespace) {
        return new StatementLocal<>(namespace);
    }

    /**
     * Creates tree-scoped namespace behaviour for supplied namespace type. Tree-scoped namespace behaviour searches
     * for value in all storage nodes up to the root and stores values in supplied node.
     *
     * @param <K> Namespace key type
     * @param <V> Namespace value type
     * @param namespace Namespace identifier
     * @return tree-scoped namespace behaviour for supplied namespace type.
     */
    public static <K, V> @NonNull NamespaceBehaviour<K, V> treeScoped(final ParserNamespace<K, V> namespace) {
        return new TreeScoped<>(namespace);
    }

    /**
     * Returns a value from model namespace storage according to key param class.
     *
     * @param global global namespace storage
     * @param storage namespace storage
     * @param key type parameter
     * @return value from model namespace storage according to key param class
     */
    public abstract @Nullable V getFrom(NamespaceStorage.Global global, NamespaceStorage storage, K key);

    /**
     * Returns the key/value mapping best matching specified criterion.
     *
     * @param global global namespace storage
     * @param storage namespace storage
     * @param criterion selection criterion
     * @return Selected mapping, if available.
     */
    public final @Nullable Entry<K, V> getFrom(final NamespaceStorage.Global global, final NamespaceStorage storage,
            final NamespaceKeyCriterion<K> criterion) {
        final var mappings = getAllFrom(global, storage);
        if (mappings == null) {
            return null;
        }

        Entry<K, V> match = null;
        for (var entry : mappings.entrySet()) {
            final K key = entry.getKey();
            if (criterion.match(key)) {
                if (match != null) {
                    final K selected = criterion.select(match.getKey(), key);
                    if (selected.equals(match.getKey())) {
                        continue;
                    }

                    verify(selected == key, "Criterion %s selected invalid key %s from candidates [%s %s]", criterion,
                        selected, match.getKey(), key);
                }

                match = entry;
            }
        }

        return match;
    }

    /**
     * Returns all values of a keys of param class from model namespace storage.
     *
     * @param global global namespace storage
     * @param storage namespace storage
     * @return all values of keys of param class from model namespace storage
     */
    public abstract Map<K, V> getAllFrom(NamespaceStorage.Global global, NamespaceStorage storage);

    /**
     * Adds a key/value to corresponding namespace storage according to param class.
     *
     * @param global global namespace storage
     * @param storage namespace storage
     * @param key type parameter
     * @param value type parameter
     */
    public abstract void addTo(NamespaceStorage.Global global, NamespaceStorage storage, K key, V value);

    protected final V getFromLocalStorage(final NamespaceStorage storage, final K key) {
        return storage.getFromLocalStorage(namespace, key);
    }

    protected final Map<K, V> getAllFromLocalStorage(final NamespaceStorage storage) {
        return storage.getAllFromLocalStorage(namespace);
    }

    protected final void addToStorage(final NamespaceStorage storage, final K key, final V value) {
        storage.putToLocalStorage(namespace, key, value);
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return toStringHelper.add("namespace", namespace);
    }

    private abstract static sealed class AbstractSpecific<K, V> extends NamespaceBehaviour<K, V> {
        AbstractSpecific(final ParserNamespace<K, V> namespace) {
            super(namespace);
        }

        @Override
        public final V getFrom(final NamespaceStorage.Global global, final NamespaceStorage storage, final K key) {
            return getFromLocalStorage(findStorage(global, storage), key);
        }

        @Override
        public final Map<K, V> getAllFrom(final NamespaceStorage.Global global, final NamespaceStorage storage) {
            return getAllFromLocalStorage(findStorage(global, storage));
        }

        @Override
        public final void addTo(final NamespaceStorage.Global global, final NamespaceStorage storage, final K key,
                final V value) {
            addToStorage(findStorage(global, storage), key, value);
        }

        abstract NamespaceStorage findStorage(NamespaceStorage.Global global, NamespaceStorage storage);
    }

    private static final class Global<K, V> extends AbstractSpecific<K, V> {
        Global(final ParserNamespace<K, V> namespace) {
            super(namespace);
        }

        @Override
        NamespaceStorage.Global findStorage(final NamespaceStorage.Global global, final NamespaceStorage storage) {
            return global;
        }
    }

    private static final class SourceLocal<K, V> extends AbstractSpecific<K, V> {
        SourceLocal(final ParserNamespace<K, V> namespace) {
            super(namespace);
        }

        @Override
        NamespaceStorage findStorage(final NamespaceStorage.Global global, final NamespaceStorage storage) {
            var current = storage;
            while (current != null && current.level() != Level.SOURCE) {
                current = current.getParentStorage();
            }
            return current;
        }

        @Override
        protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return super.addToStringAttributes(helper.add("type", Level.SOURCE));
        }
    }

    private static final class StatementLocal<K, V> extends AbstractSpecific<K, V> {
        StatementLocal(final ParserNamespace<K, V> identifier) {
            super(identifier);
        }

        @Override
        NamespaceStorage findStorage(final NamespaceStorage.Global global, final NamespaceStorage storage) {
            return storage;
        }
    }

    private static final class TreeScoped<K, V> extends NamespaceBehaviour<K, V> {
        TreeScoped(final ParserNamespace<K, V> namespace) {
            super(namespace);
        }

        @Override
        public V getFrom(final NamespaceStorage.Global global, final NamespaceStorage storage, final K key) {
            var current = storage;
            while (current != null) {
                final V val = getFromLocalStorage(current, key);
                if (val != null) {
                    return val;
                }
                current = current.getParentStorage();
            }
            return null;
        }

        @Override
        public Map<K, V> getAllFrom(final NamespaceStorage.Global global, final NamespaceStorage storage) {
            var current = storage;
            while (current != null) {
                final Map<K, V> val = getAllFromLocalStorage(current);
                if (val != null) {
                    return val;
                }
                current = current.getParentStorage();
            }
            return null;
        }

        @Override
        public void addTo(final NamespaceStorage.Global global, final NamespaceStorage storage, final K key,
                final V value) {
            addToStorage(storage, key, value);
        }
    }
}
