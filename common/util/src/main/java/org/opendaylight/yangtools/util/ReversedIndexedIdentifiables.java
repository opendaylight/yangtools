/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static java.util.Objects.requireNonNull;

import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Identifiable;

final class ReversedIndexedIdentifiables<I, E extends Identifiable<I>> extends AbstractIndexedIdentifiables<I, E> {
    private final @NonNull NaiveIndexedIdentifiables<I, E> regular;

    ReversedIndexedIdentifiables(final NaiveIndexedIdentifiables<I, E> regular) {
        this.regular = requireNonNull(regular);
    }

    @Override
    public int size() {
        return regular.size();
    }

    @Override
    public NaiveIndexedIdentifiables<I, E> reversed() {
        return regular;
    }

    @Override
    public E elementAt(final int offset) {
        final var length = regular.size();
        Objects.checkIndex(offset, length);
        return regular.elementAt(length - offset - 1);
    }

    @Override
    public E elementOf(final I index) {
        return regular.elementOf(index);
    }

    @Override
    public Iterator<E> iterator() {
        return regular.reverseValues().iterator();
    }

    @Override
    public Spliterator<E> spliterator() {
        return regular.reverseValues().spliterator();
    }

    @Override
    public Builder<I, E> toBuilder() {
        // TODO Auto-generated method stub
        return null;
    }
}