/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.UnmodifiableIterator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.util.UnmodifiableMapPhase;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafSetEntryNodeBuilder.ImmutableLeafSetEntryNode;

/**
 * Immutable view of an {@link AbstractLeafSetMap}.
 *
 * @param <T> Type of stored {@link LeafSetEntryNode} values
 */
final class ImmutableLeafSetMap<T> extends AbstractLeafSetMap<T> implements UnmodifiableMapPhase<NodeWithValue, LeafSetEntryNode<T>> {
    private static final long serialVersionUID = 1L;
    private final ImmutableSet<NodeWithValue> keys;

    ImmutableLeafSetMap(final ImmutableSet<NodeWithValue> keys) {
        this.keys = Preconditions.checkNotNull(keys);
    }

    @Override
    protected Set<NodeWithValue> keys() {
        return keys;
    }

    @Override
    public LeafSetEntryNode<T> remove(final Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(final Map<? extends NodeWithValue, ? extends LeafSetEntryNode<T>> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Iterator<Entry<NodeWithValue, LeafSetEntryNode<T>>> entrySetIterator() {
        final Iterator<NodeWithValue> it = keys.iterator();

        return new UnmodifiableIterator<Entry<NodeWithValue, LeafSetEntryNode<T>>>() {
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public Entry<NodeWithValue, LeafSetEntryNode<T>> next() {
                final LeafSetEntryNode<T> node = new ImmutableLeafSetEntryNode<T>(it.next());
                return new SimpleEntry<>(node.getIdentifier(), node);
            }
        };
    }

    @Override
    public MutableLeafSetMap<T> toModifiableMap() {
        return new MutableLeafSetMap<>(keys);
    }
}
