/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Identifiable;

public final class NaiveIndexedIdentifiables<I, E extends Identifiable<I>> extends AbstractIndexedIdentifiables<I, E> {
    private static final @NonNull NaiveIndexedIdentifiables<?, ?> EMPTY =
        new NaiveIndexedIdentifiables<>(ImmutableMap.of());

    private final ImmutableMap<I, E> elements;

    private NaiveIndexedIdentifiables(final ImmutableMap<I, E> elements) {
        this.elements = requireNonNull(elements);
    }

    @SuppressWarnings("unchecked")
    public static <I, E extends Identifiable<I>> @NonNull IndexedIdentifiables<I, E> of() {
        return (NaiveIndexedIdentifiables<I, E>) EMPTY;
    }

    public static <I, E extends Identifiable<I>> @NonNull IndexedIdentifiables<I, E> of(final E element) {
        return new SingletonIndexedIdentifiables<>(element);
    }

    @SafeVarargs
    public static <I, E extends Identifiable<I>> @NonNull IndexedIdentifiables<I, E> of(final E... elements) {
        return of(Arrays.asList(elements));
    }

    public static <I, E extends Identifiable<I>> @NonNull IndexedIdentifiables<I, E> of(final List<E> elements) {
        return of(Maps.uniqueIndex(elements, Identifiable::getIdentifier));
    }

    static <I, E extends Identifiable<I>> @NonNull IndexedIdentifiables<I, E> of(final ImmutableMap<I, E> elements) {
        return switch (elements.size()) {
            case 0 -> of();
            case 1 -> of(elements.values().asList().getFirst());
            default -> new NaiveIndexedIdentifiables<>(elements);
        };
    }

    public static <I, E extends Identifiable<I>> @NonNull Builder<I, E> builder() {
        return new NaiveIndexedIdentifiablesBuilder<>(of());
    }

    @Override
    public E elementAt(final int offset) {
        return values().get(offset);
    }

    @Override
    public E elementOf(final I index) {
        return elements.get(index);
    }

    @Override
    public int size() {
        return elements.size();
    }

    @Override
    public IndexedIdentifiables<I, E> reversed() {
        return new ReversedIndexedIdentifiables<>(this);
    }

    @Override
    public Iterator<E> iterator() {
        return values().iterator();
    }

    @Override
    public Spliterator<E> spliterator() {
        return values().spliterator();
    }

    @Override
    public Builder<I, E> toBuilder() {
        return new NaiveIndexedIdentifiablesBuilder<>(this);
    }

    ImmutableList<E> reverseValues() {
        return values().reverse();
    }

    private ImmutableList<E> values() {
        return elements.values().asList();
    }
}