/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
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
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceKeyCriterion;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceStorage;
import org.opendaylight.yangtools.yang.parser.spi.meta.ParserNamespace;

abstract class NamespaceAccess<K, V> {
    abstract static class KeyedValueAddedListener<K> {
        private final @NonNull NamespaceStorage contextNode;
        private final @NonNull K key;

        KeyedValueAddedListener(final NamespaceStorage contextNode, final K key) {
            this.contextNode = requireNonNull(contextNode);
            this.key = requireNonNull(key);
        }

        final <V> boolean isRequestedValue(final NamespaceAccess<K, ?> access, final NamespaceStorage storage,
                final V value) {
            return value == access.valueFrom(contextNode, key);
        }

        abstract void onValueAdded(Object value);
    }

    @FunctionalInterface
    interface PredicateValueAddedListener<K, V> {

        boolean onValueAdded(@NonNull K key, @NonNull V value);
    }

    abstract @NonNull ParserNamespace<K, V> namespace();

    abstract @Nullable V valueFrom(@NonNull NamespaceStorage storage, K key);

    abstract void valueTo(@NonNull NamespaceStorage storage, K key, V value);

    abstract @Nullable Map<K, V> allFrom(@NonNull NamespaceStorage storage);

    abstract @Nullable Entry<K, V> entryFrom(@NonNull NamespaceStorage storage,
        @NonNull NamespaceKeyCriterion<K> criterion);

    abstract void addListener(KeyedValueAddedListener<K> listener);

    abstract void addListener(PredicateValueAddedListener<K, V> listener);
}
