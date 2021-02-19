/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.ri.node.impl;

import static java.util.Objects.requireNonNull;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.ri.node.LazyLeafOperations;

// This is *almost* the same as Guava's TransformedCollection. The main difference is delegation of hashCode()/equals()
// towards the backing map. This class is needed to fulfull users' expectation that DataContainerNode.body() invocations
// return equal instances. Since we are *not* memoizing that return, i.e. each invocation returns a new object, we need
// to fudge the usual Collection's equal-on-identity contract.
final class LazyValues extends AbstractCollection<DataContainerChild> {
    private final Map<PathArgument, Object> map;

    LazyValues(final Map<PathArgument, Object> map) {
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
        return this == obj || obj instanceof LazyValues && map.equals(((LazyValues)obj).map);
    }

    private static final class Iter implements Iterator<DataContainerChild> {
        private final Iterator<Entry<PathArgument, Object>> iterator;

        Iter(final Iterator<Entry<PathArgument, Object>> iterator) {
            this.iterator = requireNonNull(iterator);
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public DataContainerChild next() {
            final Entry<PathArgument, Object> entry = iterator.next();
            final Object value = entry.getValue();
            return value instanceof DataContainerChild ? (DataContainerChild) value
                : LazyLeafOperations.coerceLeaf(entry.getKey(), value);
        }
    }
}
