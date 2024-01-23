/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Collector;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * A vector of values associated with a unique constraint. This is almost an {@link ArrayList}, except it is
 * unmodifiable. The only way to construct this instance is through {@link #COLLECTOR}. This should be used only in case
 * of more than one value.
 */
// Design note: this could have been a List, because the order matters, but it does not matter much, we are expected
// to be compared against ourselves -- and we treat byte[] specially, breaking reflexivity of equality. We could also go
// for Collection, but at this point nobody cares about size().
final class UniqueValues implements Immutable, Iterable<Object> {
    static final Collector<Object, ?, UniqueValues> COLLECTOR = Collector.of(ArrayList::new, ArrayList::add,
        (left, right) -> {
            left.addAll(right);
            return left;
        },
        list -> new UniqueValues(list.toArray()));

    private final Object[] objects;
    private final int hashCode;

    private UniqueValues(final Object[] objects) {
        verify(objects.length != 0);
        this.objects = objects;
        hashCode = Arrays.deepHashCode(objects);
    }

    @Override
    public Iterator<Object> iterator() {
        return new Itr(objects);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof UniqueValues other && Arrays.deepEquals(objects, other.objects);
    }

    @Override
    public String toString() {
        return toString(objects);
    }

    private static String toString(final Object[] objects) {
        final var sb = new StringBuilder();
        sb.append('[').append(BinaryValue.wrapToString(objects[0]));
        for (int i = 1; i < objects.length; ++i) {
            sb.append(", ").append(BinaryValue.wrapToString(objects[i]));
        }
        return sb.append(']').toString();
    }

    private static final class Itr implements Iterator<Object> {
        private final Object[] objects;

        private int offset = 0;

        Itr(final Object[] objects) {
            this.objects = requireNonNull(objects);
        }

        @Override
        public boolean hasNext() {
            return offset < objects.length;
        }

        @Override
        public Object next() {
            int local = offset;
            if (local >= objects.length) {
                throw new NoSuchElementException();
            }
            offset = local + 1;
            return objects[local];
        }
    }
}
