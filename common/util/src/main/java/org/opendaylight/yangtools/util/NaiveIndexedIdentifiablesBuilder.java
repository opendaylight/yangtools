/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Identifiable;

final class NaiveIndexedIdentifiablesBuilder<I, E extends Identifiable<I>>
        implements IndexedIdentifiables.Builder<I, E> {
    private sealed interface State<I, E extends Identifiable<I>> {

        int length();

        @NonNull E elementAt(int offset);

        @NonNull Modified<I, E> toModified();

        @NonNull IndexedIdentifiables<I, E> build();
    }

    private record Unmodified<I, E extends Identifiable<I>>(@NonNull IndexedIdentifiables<I, E> base)
            implements State<I, E> {
        Unmodified {
            requireNonNull(base);
        }

        @Override
        public int length() {
            return base.size();
        }

        @Override
        public E elementAt(final int offset) {
            return base.elementAt(offset);
        }


        @Override
        public IndexedIdentifiables<I, E> build() {
            return base;
        }

        @Override
        public Modified<I, E> toModified() {
            // FIXME: implement this
            throw new UnsupportedOperationException("Missing implementation");
        }
    }

    private record Modified<I, E extends Identifiable<I>>() implements State<I, E> {

        @Override
        public int length() {
            // FIXME: implement this
            throw new UnsupportedOperationException("Missing implementation");
        }

        @Override
        public E elementAt(final int offset) {
            // FIXME: implement this
            throw new UnsupportedOperationException("Missing implementation");
        }

        @Override
        public IndexedIdentifiables<I, E> build() {
            // FIXME: implement this
            throw new UnsupportedOperationException("Missing implementation");
        }

        @Override
        public Modified<I, E> toModified() {
            return this;
        }

        void add(final int offset, final E element) {
            // FIXME: implement this
            throw new UnsupportedOperationException("Missing implementation");
        }

        void remove(final int offset) {
            // FIXME: implement this
            throw new UnsupportedOperationException("Missing implementation");
        }

        void move(final int oldOffset, final int newOffset) {
            // FIXME: implement this
            throw new UnsupportedOperationException("Missing implementation");
        }
    }

    private @NonNull State<I, E> state;

    NaiveIndexedIdentifiablesBuilder(final IndexedIdentifiables<I, E> base) {
        state = new Unmodified<>(base);
    }

    @Override
    public int offsetOf(final I index) {
        // FIXME: implement this
        throw new UnsupportedOperationException("Missing implementation");
    }

    private int offsetOf(final E element) {
        return offsetOf(element.getIdentifier());
    }

    @Override
    public int addLast(final E element) {
        if (offsetOf(element) != -1) {
            return -1;
        }

        final var modified = ensureModified();
        final var offset = modified.length();
        modified.add(offset, element);
        return offset;
    }

    @Override
    public boolean add(final int offset, final E element) {
        if (offsetOf(element) == -1) {
            return false;
        }

        ensureModified().add(offset, element);
        return true;
    }

    @Override
    public int removeLast() {
        final var offset = state.length() - 1;
        if (offset < 0) {
            return -1;
        }

        ensureModified().remove(offset);
        return offset;
    }

    @Override
    public boolean remove(final int offset) {
        if (offset < -1) {
            return false;
        }

        Objects.checkIndex(offset, state.length());
        ensureModified().remove(offset);
        return true;
    }

    @Override
    public E move(final int oldOffset, final int newOffset) {
        final var length = state.length();
        Objects.checkIndex(oldOffset, length);
        Objects.checkIndex(newOffset, length);
        if (oldOffset != newOffset) {
            ensureModified().move(oldOffset, newOffset);
        }
        return state.elementAt(newOffset);
    }

    @Override
    public IndexedIdentifiables<I, E> build() {
        return state.build();
    }

    private Modified<I, E> ensureModified() {
        final var ret = state.toModified();
        state = ret;
        return ret;
    }
}