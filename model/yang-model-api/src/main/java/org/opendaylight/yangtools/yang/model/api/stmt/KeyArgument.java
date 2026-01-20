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
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * An argument to {@link KeyStatement}.
 *
 * @since 15.0.0
 */
@NonNullByDefault
public sealed interface KeyArgument extends SizedIterable<QName> permits KeyArgument.OfOne, KeyArgument.OfMore {
    /**
     * A {@link KeyArgument} containing one {@code node-identifier}.
     */
    sealed interface OfOne extends KeyArgument permits SingleKeyArgument {
        /**
         * {@return the single {@code node-identifier}}
         */
        QName item();

        @Override
        default Iterator<QName> iterator() {
            return Iterators.singletonIterator(item());
        }

        @Override
        default int size() {
            return 1;
        }

        @Override
        default List<QName> asList() {
            return List.of(item());
        }

        @Override
        default Set<QName> asSet() {
            return Set.of(item());
        }
    }

    /**
     * A {@link KeyArgument} containing two or more {@code node-identifier}s.
     */
    sealed interface OfMore extends KeyArgument permits RegularKeyArgument {
        // just marker
    }

    /**
     * {@return a {@link KeyArgument} containing specified {@code node-identifier}}
     * @param nodeIdentifier the {code node-identifier}
     */
    static KeyArgument.OfOne of(final QName nodeIdentifier) {
        return new SingleKeyArgument(nodeIdentifier);
    }

    /**
     * {@return a {@link KeyArgument} containing specified {@code node-identifier}}s
     * @param nodeIdentifiers the {code node-identifier}s
     */
    static KeyArgument of(final List<QName> nodeIdentifiers) {
        return switch (nodeIdentifiers.size()) {
            case 0 -> throw new IllegalArgumentException("empty node-identifiers");
            case 1 -> of(nodeIdentifiers.getFirst());
            default -> {
                final var copy = ImmutableSet.copyOf(nodeIdentifiers);
                yield copy.size() == 1 ? of(nodeIdentifiers.iterator().next()) : new RegularKeyArgument(copy);
            }
        };
    }

    /**
     * {@return return {@code true} if this argument contains specified {@code node-identifier}}
     * @param nodeIdentifier the {@code node-identifier}
     */
    boolean contains(QName nodeIdentifier);

    /**
     * {@return this argument as a {@code List<QName>}}
     */
    List<QName> asList();

    /**
     * {@return this argument as a {@code Set<QName>}}
     */
    Set<QName> asSet();

    /**
     * {@return the equivalent of {@code asList().toString()}}
     */
    @Override
    String toString();
}
