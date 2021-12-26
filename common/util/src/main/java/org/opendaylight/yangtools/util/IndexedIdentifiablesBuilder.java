/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import org.opendaylight.yangtools.concepts.Identifiable;

public final class IndexedIdentifiablesBuilder<I, E extends Identifiable<I>>
        implements IndexedIdentifiables.Builder<I, E> {
    IndexedIdentifiablesBuilder() {

    }

    @Override
    public int offsetOf(final I index) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public IndexedIdentifiablesBuilder<I, E> addFirst(final E element) {
        return add(0, element);
    }

    @Override
    public IndexedIdentifiablesBuilder<I, E> addLast(final E element) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IndexedIdentifiablesBuilder<I, E> add(final int offset, final E element) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IndexedIdentifiablesBuilder<I, E> removeFirst() {
        return remove(0);
    }

    @Override
    public IndexedIdentifiablesBuilder<I, E> removeLast() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IndexedIdentifiablesBuilder<I, E> remove(final int offset) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IndexedIdentifiablesBuilder<I, E> move(final int oldOffset, final int newOffset) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IndexedIdentifiables<I, E> build() {
        // TODO Auto-generated method stub
        return null;
    }
}