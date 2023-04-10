/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceKeyCriterion;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceStorage;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceStorage.GlobalStorage;
import org.opendaylight.yangtools.yang.parser.spi.meta.ParserNamespace;

/**
 * A {@link NamespaceAccess} backed by a {@link NamespaceBehaviour}. Also holds reference to {@link BuildGlobalContext}.
 */
final class BehaviourNamespaceAccess<K, V> extends NamespaceAccess<K, V> {
    private final @NonNull NamespaceBehaviour<K, V> behaviour;
    private final @NonNull GlobalStorage globalStorage;

    private Multimap<K, KeyedValueAddedListener<K, V>> keyListeners;
    private List<PredicateValueAddedListener<K, V>> predicateListeners;

    BehaviourNamespaceAccess(final GlobalStorage globalStorage, final NamespaceBehaviour<K, V> behaviour) {
        this.globalStorage = requireNonNull(globalStorage);
        this.behaviour = requireNonNull(behaviour);
    }

    @Override
    ParserNamespace<K, V> namespace() {
        return behaviour.namespace();
    }

    @Override
    V valueFrom(final NamespaceStorage storage, final K key) {
        return behaviour.getFrom(globalStorage, storage, key);
    }

    @Override
    void valueTo(final NamespaceStorage storage, final K key, final V value) {
        behaviour.addTo(globalStorage, storage, key, value);

        if (keyListeners != null) {
            final var listeners = keyListeners.get(key);
            if (!listeners.isEmpty()) {
                final var toNotify = new ArrayList<KeyedValueAddedListener<K, V>>();
                final var it = listeners.iterator();
                while (it.hasNext()) {
                    final var listener = it.next();
                    if (listener.isRequestedValue(this, storage, key, value)) {
                        it.remove();
                        toNotify.add(listener);
                    }
                }
                for (var listener : toNotify) {
                    listener.onValueAdded(key, value);
                }

                if (keyListeners.isEmpty()) {
                    keyListeners = null;
                }
            }
        }

        if (predicateListeners != null) {
            final var it = predicateListeners.iterator();
            while (it.hasNext()) {
                if (it.next().onValueAdded(key, value)) {
                    it.remove();
                }
            }
            if (predicateListeners != null && predicateListeners.isEmpty()) {
                predicateListeners = null;
            }
        }
    }

    @Override
    Map<K, V> allFrom(final NamespaceStorage storage) {
        return behaviour.getAllFrom(globalStorage, storage);
    }

    @Override
    Entry<K, V> entryFrom(final NamespaceStorage storage, final NamespaceKeyCriterion<K> criterion) {
        return behaviour.getFrom(globalStorage, storage, criterion);
    }

    @Override
    void addListener(final K key, final KeyedValueAddedListener<K, V> listener) {
        final var k = requireNonNull(key);
        final var l = requireNonNull(listener);
        if (keyListeners == null) {
            keyListeners = Multimaps.newMultimap(new HashMap<>(), ArrayDeque::new);
        }
        keyListeners.put(k, l);
    }

    @Override
    void addListener(final PredicateValueAddedListener<K, V> listener) {
        if (predicateListeners == null) {
            predicateListeners = new ArrayList<>();
        }
        predicateListeners.add(listener);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("behaviour", behaviour).toString();
    }
}
