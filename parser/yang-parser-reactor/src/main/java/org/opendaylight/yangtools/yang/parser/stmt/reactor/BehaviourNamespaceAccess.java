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
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.GlobalStorageAccess;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceKeyCriterion;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceStorage;

/**
 * A {@link NamespaceAccess} backed by a {@link NamespaceBehaviour}. Also holds reference to {@link BuildGlobalContext}.
 */
abstract class BehaviourNamespaceAccess<K, V> extends NamespaceAccess<K, V> implements GlobalStorageAccess {
    private final @NonNull AbstractNamespaceStorage globalContext;
    private final @NonNull NamespaceBehaviour<K, V> behaviour;

    BehaviourNamespaceAccess(final AbstractNamespaceStorage globalContext, final NamespaceBehaviour<K, V> behaviour) {
        this.globalContext = requireNonNull(globalContext);
        this.behaviour = requireNonNull(behaviour);
    }

    @Override
    public final AbstractNamespaceStorage getGlobalStorage() {
        return globalContext;
    }

    @Override
    final V valueFrom(final NamespaceStorage storage, final K key) {
        return behaviour.getFrom(this, storage, key);
    }

    @Override
    final void valueTo(final NamespaceStorage storage, final K key, final V value) {
        behaviour.addTo(this, storage, key, value);
        onValueTo(storage, key, value);
    }

    abstract void onValueTo(NamespaceStorage storage, K key, V value);

    @Override
    final Map<K, V> allFrom(final NamespaceStorage storage) {
        return behaviour.getAllFrom(this, storage);
    }

    @Override
    final Entry<K, V> entryFrom(final NamespaceStorage storage, final NamespaceKeyCriterion<K> criterion) {
        return behaviour.getFrom(this, storage, criterion);
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this).add("behaviour", behaviour).toString();
    }
}
