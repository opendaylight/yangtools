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
import java.util.Iterator;
import java.util.Spliterator;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Identifiable;

final class NaiveIndexedIdentifiables<I, E extends Identifiable<I>> extends AbstractIndexedIdentifiables<I, E> {
    static final @NonNull NaiveIndexedIdentifiables<?, ?> EMPTY = new NaiveIndexedIdentifiables<>(ImmutableMap.of());

    private final ImmutableMap<I, E> elements;

    NaiveIndexedIdentifiables(final ImmutableMap<I, E> elements) {
        this.elements = requireNonNull(elements);
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
    public ReversedIndexedIdentifiables<I, E> reversed() {
        return new ReversedIndexedIdentifiables<>(this);
    }

    @Override
    public Iterator<E> iterator() {
        return elements.values().iterator();
    }

    @Override
    public Spliterator<E> spliterator() {
        return elements.values().spliterator();
    }

    @Override
    public IndexedIdentifiablesBuilder<I, E> toBuilder() {
        // TODO Auto-generated method stub
        return null;
    }

    ImmutableList<E> reverseValues() {
        return values().reverse();
    }

    private ImmutableList<E> values() {
        return elements.values().asList();
    }
 }