/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.GlobalStorageAccess;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceKeyCriterion;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceStorage;

/**
 * Reactor interface towards namespaces.
 */
abstract class NamespaceAccess<K, V> implements GlobalStorageAccess {
    private final @NonNull NamespaceStorage globalStorage;

    NamespaceAccess(final NamespaceStorage globalStorage) {
        this.globalStorage = requireNonNull(globalStorage);
    }

    @Override
    public final NamespaceStorage getGlobalStorage() {
        return globalStorage;
    }

    abstract @Nullable V valueFrom(@NonNull NamespaceStorage storage, K key);

    abstract void valueTo(@NonNull NamespaceStorage storage, K key, V value);

    abstract @Nullable Map<K, V> allFrom(@NonNull NamespaceStorage storage);

    abstract @Nullable Entry<K, V> entryFrom(@NonNull NamespaceStorage storage,
        @NonNull NamespaceKeyCriterion<K> criterion);
}
