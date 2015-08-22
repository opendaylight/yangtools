/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafSetEntryNodeBuilder.ImmutableLeafSetEntryNode;

/**
 * A specialized implementation for tracking LeafSetEntryNodes. This class relies on the fact that {@link NodeWithValue}
 * already contains the value of the stored leaf node. That means that we only need to store the key set and derive
 * child {@link LeafSetEntryNode} objects from the keys.
 *
 * Alternatively we could just store the QName and the values and calculate both the keys and the child nodes, but
 * {@link NodeWithValue} performs eager hashCode caching, which means instantiation is not as cheap as we'd like.
 *
 * This abstract class is subclassed to provide both mutable and immutable view of the nodes.
 *
 * @param <T> Type of stored {@link LeafSetEntryNode} values
 */
abstract class AbstractLeafSetMap<T> extends AbstractMap<NodeWithValue, LeafSetEntryNode<T>> implements Map<NodeWithValue, LeafSetEntryNode<T>>, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public final int size() {
        return keys().size();
    }

    @Override
    public final boolean isEmpty() {
        return keys().isEmpty();
    }

    @Override
    public final boolean containsKey(final Object key) {
        return keys().contains(key);
    }

    @Override
    public final LeafSetEntryNode<T> get(final Object key) {
        if (!(key instanceof NodeWithValue)) {
            return null;
        }

        final NodeWithValue k = (NodeWithValue) key;
        if (!keys().contains(k)) {
            return null;
        }

        return new ImmutableLeafSetEntryNode<T>(k);
    }

    @Override
    public Set<Entry<NodeWithValue, LeafSetEntryNode<T>>> entrySet() {
        return new EntrySet();
    }

    /**
     * Return the current key set.
     *
     * @return Current key set.
     */
    protected abstract Set<NodeWithValue> keys();

    /**
     * Return the entry set iterator.
     *
     * @return Iterator over the entry set.
     */
    protected abstract Iterator<Entry<NodeWithValue, LeafSetEntryNode<T>>> entrySetIterator();

    private final class EntrySet extends AbstractSet<Entry<NodeWithValue, LeafSetEntryNode<T>>> {
        @Override
        public Iterator<Entry<NodeWithValue, LeafSetEntryNode<T>>> iterator() {
            return entrySetIterator();
        }

        @Override
        public int size() {
            return keys().size();
        }

        @Override
        public boolean contains(final Object o) {
            if (!(o instanceof Entry)) {
                return false;
            }

            @SuppressWarnings("unchecked")
            final Entry<NodeWithValue, LeafSetEntryNode<T>> e = (Entry<NodeWithValue, LeafSetEntryNode<T>>) o;
            if (e.getValue() == null) {
                return false;
            }

            return e.getValue().equals(AbstractLeafSetMap.this.get(e.getKey()));
        }
    }
}
