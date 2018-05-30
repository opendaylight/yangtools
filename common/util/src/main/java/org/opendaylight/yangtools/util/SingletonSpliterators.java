/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.Spliterator;
import java.util.function.Consumer;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Mutable;

/**
 * Utility methods for instantiating {@link Spliterator}s containing a single element.
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public final class SingletonSpliterators {
    private SingletonSpliterators() {

    }

    /**
     * Create a new {@link Spliterator} reporting specified element. It has the following characteristics:
     * <ul>
     *   <li>{@link Spliterator#DISTINCT}</li>
     *   <li>{@link Spliterator#IMMUTABLE}</li>
     *   <li>{@link Spliterator#NONNULL}</li>
     *   <li>{@link Spliterator#ORDERED}</li>
     *   <li>{@link Spliterator#SIZED}</li>
     *   <li>{@link Spliterator#SUBSIZED}</li>
     * </ul>
     *
     * @param element Single element to report
     * @param <T> the type of elements returned by this Spliterator
     * @return A new spliterator
     * @throws NullPointerException if element is null
     */
    public static <T> Spliterator<T> immutableOf(final T element) {
        return new ImmutableNonNull<>(element);
    }

    /**
     * Create a new {@link Spliterator} reporting a {@code null} element. It has the following characteristics:
     * <ul>
     *   <li>{@link Spliterator#DISTINCT}</li>
     *   <li>{@link Spliterator#IMMUTABLE}</li>
     *   <li>{@link Spliterator#ORDERED}</li>
     *   <li>{@link Spliterator#SIZED}</li>
     *   <li>{@link Spliterator#SUBSIZED}</li>
     * </ul>
     *
     * @return A new spliterator
     */
    public static <@Nullable T> Spliterator<T> immutableOfNull() {
        return new ImmutableNull<>();
    }

    private static final class ImmutableNonNull<T> implements Mutable, Spliterator<T> {
        private @Nullable T element;

        private ImmutableNonNull(final T element) {
            this.element = requireNonNull(element);
        }

        @Override
        public boolean tryAdvance(final @Nullable Consumer<? super T> action) {
            requireNonNull(action);
            if (element == null) {
                return false;
            }

            action.accept(element);
            element = null;
            return true;
        }

        @Override
        public @Nullable Spliterator<T> trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            return element == null ? 0 : 1;
        }

        @Override
        public int characteristics() {
            return Spliterator.NONNULL | Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.IMMUTABLE
                    | Spliterator.DISTINCT | Spliterator.ORDERED;
        }
    }

    private static final class ImmutableNull<@Nullable E> implements Mutable, Spliterator<E> {
        private boolean consumed;

        @Override
        public boolean tryAdvance(final @Nullable Consumer<? super E> action) {
            requireNonNull(action);
            if (consumed) {
                return false;
            }

            action.accept(null);
            consumed = true;
            return true;
        }

        @Override
        public @Nullable Spliterator<E> trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            return consumed ? 0 : 1;
        }

        @Override
        public int characteristics() {
            return Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.IMMUTABLE | Spliterator.DISTINCT
                    | Spliterator.ORDERED;
        }
    }
}
