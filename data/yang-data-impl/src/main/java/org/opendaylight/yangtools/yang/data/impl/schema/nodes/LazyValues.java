/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.nodes;

import static java.util.Objects.requireNonNull;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;

// This is *almost* the same as Guava's TransformedCollection. The main difference is delegation of hashCode()/equals()
// towards the backing map. This is needed because we do not retain a reference to this object and thus
// NormalizedNode.getValue() does not compare as equal. When invoked twice and lazy leaves are in effect. Note that
// Collection.equals() is undefined, but the expectation from users is that we will return the same view object, which
// equals on identity.
public final class LazyValues extends AbstractCollection<DataContainerChild> {
    private final Map<NodeIdentifier, Object> map;

    public LazyValues(final Map<NodeIdentifier, Object> map) {
        this.map = requireNonNull(map);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public Iterator<DataContainerChild> iterator() {
        return new Iter(map.entrySet().iterator());
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof LazyValues other && map.equals(other.map);
    }

    private static final class Iter implements Iterator<DataContainerChild> {
        private final Iterator<Entry<NodeIdentifier, Object>> iterator;

        Iter(final Iterator<Entry<NodeIdentifier, Object>> iterator) {
            this.iterator = requireNonNull(iterator);
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public DataContainerChild next() {
            final var entry = iterator.next();
            final var value = entry.getValue();
            return value instanceof DataContainerChild child ? child
                : LazyLeafOperations.coerceLeaf(entry.getKey(), value);
        }
    }
}
