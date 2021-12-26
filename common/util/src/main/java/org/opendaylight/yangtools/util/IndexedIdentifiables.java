/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import com.google.common.annotations.Beta;
import java.util.Collection;
import java.util.SequencedCollection;
import java.util.Spliterator;
import java.util.function.Predicate;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.Mutable;

/**
 * A immutable array of items being unique according to an indexing function.
 *
 * @param IndexedIdentifiables index type
 * @param E element type
 */
@Beta
public interface IndexedIdentifiables<I, E extends Identifiable<I>> extends Immutable, SequencedCollection<E> {

    @NonNull E elementAt(int offset);

    /**
     * Return the element matching specified index, if present.
     *
     * @param index element index
     * @return Matching element, or {@code null}
     * @throws NullPointerException if {@code id} is {@code null}
     */
    @Nullable E elementOf(@NonNull I index);

    @Override
    IndexedIdentifiables<I, E> reversed();

    /**
     * {@inheritDoc}
     *
     * Implementations are required to report at least the following {@link Spliterator#characteristics()}:
     * <ul>
     *   <li>{@code IMMUTABLE} implied by {@link Immutable}</li>
     *   <li>{@code NONNULL} implied by this interface's contract of storing only non-null elements</li>
     *   <li>{@code ORDERED} implied by {@link SequencedCollection}</li>
     * </ul>
     *
     * <p>Default implementation defers to {@link #iterator()} and additionally reports {@code SIZED} and
     * {@code SUBSIZED}.
     */
    @Override
    Spliterator<E> spliterator();

    @Override
    @Deprecated
    boolean addAll(Collection<? extends E> c);

    @Override
    @Deprecated
    void clear();

    @Override
    @Deprecated
    boolean remove(Object o);

    @Override
    @Deprecated
    boolean removeAll(Collection<?> c);

    @Override
    @Deprecated
    boolean retainAll(final Collection<?> c);

    @Override
    @Deprecated
    boolean removeIf(final Predicate<? super E> filter);

    Builder<I, E> toBuilder();

    interface Builder<I, E extends Identifiable<I>> extends Mutable {

        int offsetOf(I index);

        // equivalent to add(0, element)
        Builder<I, E> addFirst(E element);

        Builder<I, E> addLast(E element);

        Builder<I, E> add(int offset, E element);

        Builder<I, E> removeFirst();

        Builder<I, E> removeLast();

        Builder<I, E> remove(int offset);

        // with [a, b, c, d] the following happens:
        //  0, 0 -> no-op
        //  0, 1 -> [b, a, c, d]
        //  1, 0 -> [b. a, c, d]
        //  1, 3 -> [a, v, d, c]
        //
        Builder<I, E> move(int oldOffset, int newOffset);

        IndexedIdentifiables<I, E> build();
    }
}
