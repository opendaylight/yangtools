/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;

/**
 * An argument to {@link UniqueStatement}. It contains one or more {@link Descendant} schema node identifiers.
 *
 * @since 15.0.0
 */
@NonNullByDefault
public sealed interface UniqueArgument extends SizedIterable<Descendant> {
    /**
     * A {@link UniqueArgument} containing one {@link Descendant}.
     */
    sealed interface OfOne extends UniqueArgument permits UniqueArgument1 {
        /**
         * {@return the single {@link Descendant}}
         */
        Descendant item();

        @Override
        default Iterator<Descendant> iterator() {
            return Iterators.singletonIterator(item());
        }

        @Override
        default Stream<Descendant> stream() {
            return Stream.of(item());
        }

        @Override
        default int size() {
            return 1;
        }

        @Override
        default List<Descendant> asList() {
            return List.of(item());
        }

        @Override
        default Set<Descendant> asSet() {
            return Set.of(item());
        }
    }

    /**
     * A {@link UniqueArgument} containing two or more {@link Descendant}s.
     */
    sealed interface OfMore extends UniqueArgument permits UniqueArgumentN {
        // just a marker
    }

    /**
     * {@return a {@link UniqueArgument} containing specified {@link Descendant}}
     * @param descendant the {@link Descendant}
     */
    static UniqueArgument.OfOne of(final Descendant descendant) {
        return new UniqueArgument1(descendant);
    }

    /**
     * {@return a {@link UniqueArgument} containing specified {@link Descendant}}s
     * @param descendants the {@link Descendant}s
     */
    static UniqueArgument of(final List<Descendant> descendants) {
        return switch (descendants.size()) {
            case 0 -> throw new IllegalArgumentException("empty descendants");
            case 1 -> of(descendants.getFirst());
            default -> of(ImmutableSet.copyOf(descendants));
        };
    }

    private static UniqueArgument of(final ImmutableSet<Descendant> descendants) {
        return switch (descendants.size()) {
            case 1 -> of(descendants.iterator().next());
            default -> new UniqueArgumentN(descendants);
        };
    }

    /**
     * {@return return {@code true} if this argument contains specified {@link Descendant}}
     * @param descendant the {{@link Descendant}
     */
    boolean contains(Descendant descendant);

    /**
     * {@return this argument as a {@code List<Descendant>}}
     */
    List<Descendant> asList();

    /**
     * {@return this argument as a {@code Set<Descendant>}}
     */
    Set<Descendant> asSet();

    /**
     * {@return the equivalent of {@code asList().toString()}}
     */
    @Override
    String toString();
}
