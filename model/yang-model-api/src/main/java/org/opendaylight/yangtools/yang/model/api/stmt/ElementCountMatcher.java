/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import static java.util.Objects.requireNonNull;

import java.math.BigInteger;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.stmt.MaxElementsArgument.Bounded;
import org.opendaylight.yangtools.yang.model.api.stmt.MaxElementsArgument.Unbounded;

/**
 * A matcher on element count in a container like {@code list}, as expressed via {@code min-elements} and
 * {@code max-elememnts}, which can be matched against representations integral to Java.
 *
 * @since 15.0.0
 */
@NonNullByDefault
public sealed interface ElementCountMatcher extends Immutable
        permits MinMaxElementCountMatcher, MaxElementsArgument, MinElementsArgument {
    /**
     * {@return {@code true} if {@code elementCount} matches this bound}
     * @param elementCount the element count
     */
    boolean matches(int elementCount);

    /**
     * {@return {@code true} if {@code elementCount} matches this bound}
     * @param elementCount the element count
     */
    boolean matches(long elementCount);

    /**
     * {@return {@code true} if {@code elementCount} matches this bound}
     * @param elementCount the element count
     */
    boolean matches(BigInteger elementCount);

    /**
     * {@return an ElementCountMatcher matching specified {@ode min-elements} and {@code max-elements}}
     * @param minElements the {@link MinElementsArgument}
     * @param maxElements the {@link MaxElementsArgument}
     */
    static ElementCountMatcher of(final MinElementsArgument minElements, final MaxElementsArgument maxElements) {
        return switch (maxElements) {
            case Bounded bounded -> of(minElements, bounded);
            case Unbounded unbouded -> requireNonNull(minElements);
        };
    }

    /**
     * {@return an ElementCountMatcher matching specified {@ode min-elements} and {@code max-elements}}
     * @param minElements the {@link MinElementsArgument}
     * @param maxElements the {@link Bounded}
     */
    static ElementCountMatcher of(final MinElementsArgument minElements, final Bounded maxElements) {
        // 'min.lowerInt == -1' == 'min-elements == 0'
        return minElements.lowerInt() == -1 ? requireNonNull(maxElements)
            : new MinMaxElementCountMatcher(minElements, maxElements);
    }
}
