/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.function.Predicate;
import org.opendaylight.yangtools.concepts.Identifiable;

/**
 * Abstract substrate for {@link OrderedDict} implementations.
 */
public abstract class AbstractIndexedIdentifiables<I, E extends Identifiable<I>> extends AbstractCollection<E>
        implements IndexedIdentifiables<I, E> {
    @Override
    @Deprecated
    public final boolean addAll(final Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public final void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public final boolean remove(final Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public final boolean removeAll(final Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public final boolean retainAll(final Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public final boolean removeIf(final Predicate<? super E> filter) {
        throw new UnsupportedOperationException();
    }

//    @Override
//    public abstract Iterator<E> iterator();
//
//    @Override
//    public abstract Spliterator<E> spliterator();
}
