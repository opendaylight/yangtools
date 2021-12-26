/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Iterators;
import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Identifiable;

final class SingletonIndexedIdentifiables<I, E extends Identifiable<I>> extends AbstractIndexedIdentifiables<I, E> {
    private final @NonNull E element;

    SingletonIndexedIdentifiables(final E element) {
        this.element = requireNonNull(element);
    }

    @Override
    public E elementAt(final int offset) {
        Objects.checkIndex(offset, 1);
        return element;
    }

    @Override
    public E elementOf(final I index) {
        return index.equals(element.getIdentifier()) ? element : null;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public E getFirst() {
        return element;
    }

    @Override
    public E getLast() {
        return element;
    }

    @Override
    public Iterator<E> iterator() {
        return Iterators.singletonIterator(element);
    }

    @Override
    public Spliterator<E> spliterator() {
        return SingletonSpliterators.immutableOf(element);
    }

    @Override
    public SingletonIndexedIdentifiables<I, E> reversed() {
        return this;
    }

    @Override
    public Builder<I, E> toBuilder() {
        // TODO Auto-generated method stub
        return null;
    }
}