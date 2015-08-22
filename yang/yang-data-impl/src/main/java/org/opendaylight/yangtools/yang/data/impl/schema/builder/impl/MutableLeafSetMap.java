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
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import org.opendaylight.yangtools.util.ModifiableMapPhase;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafSetEntryNodeBuilder.ImmutableLeafSetEntryNode;

/**
 * Mutable version of {@link AbstractLeafSetMap}.
 *
 * @param <T> Type of stored {@link LeafSetEntryNode} values
 */
final class MutableLeafSetMap<T> extends AbstractLeafSetMap<T> implements ModifiableMapPhase<NodeWithValue, LeafSetEntryNode<T>> {
    private static final long serialVersionUID = 1L;
    // Default capacity: three items at load factor 0.75
    private static final int DEFAULT_CAPACITY = 4;

    private Set<NodeWithValue> keys;
    private volatile transient int modCount;

    MutableLeafSetMap() {
        keys = new LinkedHashSet<>(DEFAULT_CAPACITY);
    }

    MutableLeafSetMap(final int initialCapacity) {
        keys = new LinkedHashSet<>(initialCapacity + initialCapacity / 3);
    }

    MutableLeafSetMap(final ImmutableSet<NodeWithValue> keys) {
        this.keys = Preconditions.checkNotNull(keys);
    }

    @Override
    protected Set<NodeWithValue> keys() {
        return keys;
    }

    private void ensureMutable() {
        if (keys instanceof ImmutableSet) {
            keys = new LinkedHashSet<>(keys);
            modCount++;
        }
    }

    @Override
    public LeafSetEntryNode<T> put(final NodeWithValue key, final LeafSetEntryNode<T> value) {
        // Check key/value consistency, as we'll retain just the key
        Preconditions.checkArgument(Objects.deepEquals(key.getValue(), value.getValue()));

        if (keys.contains(key)) {
            // Fake a replacement
            return new ImmutableLeafSetEntryNode<T>(key);
        }

        ensureMutable();
        keys.add(key);
        modCount++;
        return null;
    }

    @Override
    public LeafSetEntryNode<T> remove(final Object key) {
        final NodeWithValue k = (NodeWithValue)key;

        // If the set is immutable and it does not contain an entry, skip copying it
        if (keys instanceof ImmutableSet) {
            if (keys.contains(k)) {
                ensureMutable();
                keys.remove(k);
                return new ImmutableLeafSetEntryNode<T>(k);
            }

            return null;
        }

        // We do not have to manipulate modCount, as the collection will take care of that
        return keys.remove(k) ? new ImmutableLeafSetEntryNode<T>(k) : null;
    }

    @Override
    public void clear() {
        if (!keys.isEmpty()) {
            keys = new LinkedHashSet<>(DEFAULT_CAPACITY);
            modCount++;
        }
    }

    @Override
    protected EntrySetIterator entrySetIterator() {
        return new EntrySetIterator();
    }

    @Override
    public ImmutableLeafSetMap<T> toUnmodifiableMap() {
        /*
         * Make the current keys immutable again, in case the user ends up building multiple nodes
         * with the same set. Otherwise we'd end up creating multiple copies, which can be easily
         * avoided. We do not have to bump modCount, as the iterator can continue on the old set as
         * long as it is unmodified.
         */
        final ImmutableSet<NodeWithValue> newKeys = ImmutableSet.copyOf(keys);
        keys = newKeys;
        return new ImmutableLeafSetMap<>(newKeys);
    }

    private final class EntrySetIterator implements Iterator<Entry<NodeWithValue, LeafSetEntryNode<T>>> {
        private Iterator<NodeWithValue> it = keys.iterator();
        private int expectedModCount = modCount;
        private NodeWithValue current;

        private void checkModCount() {
            if (expectedModCount != modCount) {
                throw new ConcurrentModificationException();
            }
        }

        @Override
        public boolean hasNext() {
            checkModCount();
            return it.hasNext();
        }

        @Override
        public Entry<NodeWithValue, LeafSetEntryNode<T>> next() {
            checkModCount();
            current = it.next();

            return new SimpleEntry<NodeWithValue, LeafSetEntryNode<T>>(current, new ImmutableLeafSetEntryNode<T>(current));
        }

        @Override
        public void remove() {
            Preconditions.checkState(current != null);
            checkModCount();

            final Object prevKeys = keys;
            ensureMutable();

            // If the collection has changed, we need a new iterator and move it to current element
            if (keys != prevKeys) {
                it = keys.iterator();
                while (!current.equals(it.next())) {
                    // Intentionally empty
                }
            }

            it.remove();
            expectedModCount = modCount;
            current = null;
        }
    }
}
