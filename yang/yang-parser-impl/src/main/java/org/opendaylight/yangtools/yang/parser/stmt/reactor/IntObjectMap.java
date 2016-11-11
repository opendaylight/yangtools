/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import com.google.common.base.Preconditions;
import java.util.Arrays;
import javax.annotation.Nonnull;

/**
 * Simple integer-to-object map optimized for size and restricted in scope of operations.
 *
 * @author Robert Varga
 *
 * @param <T> Object type
 */
abstract class IntObjectMap<T> {
    private static final class Empty<T> extends IntObjectMap<T> {
        @Override
        T get(final int index) {
            return null;
        }

        @Override
        IntObjectMap<T> put(final int index, final T object) {
            return new Singleton<>(index, object);
        }
    }

    private static final class Regular<T> extends IntObjectMap<T> {
        private Object[] elements;

        Regular(final int index, final T object) {
            elements = new Object[index + 1];
            elements[index] = Preconditions.checkNotNull(object);
        }

        @SuppressWarnings("unchecked")
        @Override
        T get(final int index) {
            if (index >= elements.length) {
                return null;
            }

            return (T)elements[index];
        }

        @Override
        IntObjectMap<T> put(final int index, final T object) {
            Preconditions.checkNotNull(object);
            if (index < elements.length) {
                Preconditions.checkArgument(elements[index] == null);
            } else {
                elements = Arrays.copyOf(elements, index + 1);
            }

            elements[index] = object;
            return this;
        }
    }

    private static final class Singleton<T> extends IntObjectMap<T> {
        private final int index;
        private final T object;

        Singleton(final int index, final T object) {
            Preconditions.checkArgument(index >= 0);
            this.object = Preconditions.checkNotNull(object);
            this.index = index;
        }

        @Override
        T get(final int index) {
            return this.index == index ? object : null;
        }

        @Override
        IntObjectMap<T> put(final int index, final T object) {
            Preconditions.checkArgument(index != this.index);
            return new Regular<>(this.index, this.object).put(index, object);
        }
    }

    private static final IntObjectMap<?> EMPTY = new Empty<>();

    @SuppressWarnings("unchecked")
    static <T> IntObjectMap<T> empty() {
        return (IntObjectMap<T>) EMPTY;
    }

    abstract T get(int index);
    abstract @Nonnull IntObjectMap<T> put(int index, @Nonnull T object);
}
