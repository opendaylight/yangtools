/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static java.util.Objects.requireNonNull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Collector;
import org.eclipse.jdt.annotation.NonNull;

/**
 * A vector of values associated with a unique constraint. This is almost an {@link ArrayList}, except it is
 * unmodifiable.
 */
final class UniqueValues extends AbstractList<Object> implements RandomAccess {
    static final Collector<Object, ?, UniqueValues> COLLECTOR = Collector.of(ArrayList::new, ArrayList::add,
        (left, right) -> {
            left.addAll(right);
            return left;
        },
        list -> new UniqueValues(list.toArray()));

    private final Object[] objects;
    private final int hashCode;

    private UniqueValues(final Object[] objects) {
        this.objects = requireNonNull(objects);
        this.hashCode = super.hashCode();
    }

    @Override
    public Object get(final int index) {
        return objects[index];
    }

    @Override
    public int size() {
        return objects.length;
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean contains(final Object o) {
        return o == null ? containsNull(objects) : containsNonNull(objects, o);
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public int indexOf(final Object o) {
        return o == null ? indexOfNull(objects) : indexOfNonNull(objects, o);
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public int lastIndexOf(final Object o) {
        return o == null ? lastIndexOfNull(objects) : lastIndexOfNonNull(objects, o);
    }

    @Override
    public Object[] toArray() {
        return objects.clone();
    }

    @Override
    public Iterator<Object> iterator() {
        return new Itr(objects);
    }

    @Override
    public Spliterator<Object> spliterator() {
        return Spliterators.spliterator(objects, Spliterator.IMMUTABLE | Spliterator.ORDERED);
    }

    @Override
    public void forEach(final Consumer<Object> action) {
        requireNonNull(action);
        for (Object obj : objects) {
            action.accept(obj);
        }
    }

    @Override
    @SuppressWarnings("checkstyle:equalsHashCode")
    @SuppressFBWarnings(value = "HE_HASHCODE_NO_EQUALS", justification = "Cached hashCode(), equals remains the same")
    public int hashCode() {
        return hashCode;
    }

    private static boolean containsNonNull(final Object[] objs, final @NonNull Object obj) {
        for (Object o : objs) {
            if (obj.equals(o)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsNull(final Object[] objs) {
        for (Object o : objs) {
            if (o == null) {
                return true;
            }
        }
        return false;
    }

    private static int indexOfNonNull(final Object[] objs, final @NonNull Object obj) {
        for (int i = 0; i < objs.length; ++i) {
            if (obj.equals(objs[i])) {
                return i;
            }
        }
        return -1;
    }

    private static int indexOfNull(final Object[] objs) {
        for (int i = 0; i < objs.length; ++i) {
            if (objs[i] == null) {
                return i;
            }
        }
        return -1;
    }

    private static int lastIndexOfNonNull(final Object[] objs, final @NonNull Object obj) {
        for (int i = objs.length - 1; i >= 0; --i) {
            if (obj.equals(objs[i])) {
                return i;
            }
        }
        return -1;
    }

    private static int lastIndexOfNull(final Object[] objs) {
        for (int i = objs.length - 1; i >= 0; --i) {
            if (objs[i] == null) {
                return i;
            }
        }
        return -1;
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
