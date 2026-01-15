/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import static java.util.Objects.requireNonNull;

import java.math.BigInteger;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * A matcher on element count in a container like {@code list}, as expressed via {@code min-elements} and
 * {@code max-elememnts}, which can be matched against representations integral to Java.
 *
 * @since 15.0.0
 */
@NonNullByDefault
public sealed interface ElementCountMatcher extends Immutable {
    /**
     * An {@link ElementCountMatcher} enforcing a minimum number of elements.
     */
    non-sealed interface AtLeast extends ElementCountMatcher {
        @Override
        default @Nullable TooFewElements matches(final int elementCount) {
            return elementCount > lowerInt() ? null : tooFewElements(lowerBig());
        }

        @Override
        default @Nullable TooFewElements matches(final long elementCount) {
            return elementCount > lowerLong() ? null : tooFewElements(lowerBig());
        }

        @Override
        default @Nullable TooFewElements matches(final BigInteger elementCount) {
            final var lowerBig = lowerBig();
            return elementCount.compareTo(lowerBig) > 0 ? null : tooFewElements(lowerBig);
        }

        @Override
        default boolean matchesAll() {
            // 'min.lowerInt == -1' == 'min-elements == 0'
            return lowerInt() == -1;
        }

        private static TooFewElements tooFewElements(final BigInteger lowerBig) {
            return new TooFewElements(lowerBig.add(BigInteger.ONE));
        }

        /**
         * {@return the next lower-than-this {@code int} bound of this argument}
         */
        int lowerInt();

        /**
         * {@return the next lower-than-this {@code long} bound of this argument}
         */
        long lowerLong();

        /**
         * {@return the next lower-than-this {@code BigInteger} bound of this argument}
         */
        BigInteger lowerBig();

        /**
         * {@return the string representation of the minimum number of elements}
         */
        @Override
        String toString();
    }

    /**
     * An {@link ElementCountMatcher} enforcing a maximum number of elements.
     */
    non-sealed interface AtMost extends ElementCountMatcher {
        @Override
        @Nullable TooManyElements matches(int elementCount);

        @Override
        @Nullable TooManyElements matches(long elementCount);

        @Override
        @Nullable TooManyElements matches(BigInteger elementCount);

        /**
         * {@return this argument saturated to {@link Integer#MAX_VALUE}}
         */
        int asSaturatedInt();

        /**
         * {@return this argument saturated to {@link Long#MAX_VALUE}}
         */
        long asSaturatedLong();

        /**
         * {@return the string representation of the maximum number of elements}
         */
        @Override
        String toString();
    }

    /**
     * An {@link ElementCountMatcher} enforcing both minimum and maximum number of elements.
     */
    sealed interface InRange extends ElementCountMatcher permits ElementCountRange {
        @Override
        default @Nullable Violation matches(final int elementCount) {
            final var min = atLeast().matches(elementCount);
            return min != null ? min : atMost().matches(elementCount);
        }

        @Override
        default @Nullable Violation matches(final long elementCount) {
            final var min = atLeast().matches(elementCount);
            return min != null ? min : atMost().matches(elementCount);
        }

        @Override
        default @Nullable Violation matches(final BigInteger elementCount) {
            final var min = atLeast().matches(elementCount);
            return min != null ? min : atMost().matches(elementCount);
        }

        @Override
        default boolean matchesAll() {
            return atLeast().matchesAll() && atMost().matchesAll();
        }

        /**
         * {@return the lower bound, inclusive}
         */
        AtLeast atLeast();

        /**
         * {@return the lower bound, inclusive}
         */
        AtMost atMost();

        /**
         * {@return the string representation of this range}
         */
        @Override
        String toString();
    }

    /**
     * A matching violation.
     */
    sealed interface Violation {
        // Just a marker
    }

    /**
     * Proposed element count violates constraint imposed by {@code min-elements}, as illustrated in
     * <a href="https://www.rfc-editor.org/rfc/rfc7950#section-15.3">RFC7950 section 15.3</a>.
     *
     * @param minElements minimum allowed elements
     */
    record TooFewElements(Number minElements) implements Violation {
        public TooFewElements {
            requireNonNull(minElements);
        }
    }

    /**
     * Proposed element count violates constraint imposed by {@code max-elements}, as illustrated in
     * <a href="https://www.rfc-editor.org/rfc/rfc7950#section-15.2">RFC7950 section 15.2</a>.
     *
     * @param maxElements maximum allowed elements
     */
    record TooManyElements(Number maxElements) implements Violation {
        public TooManyElements {
            requireNonNull(maxElements);
        }
    }

    /**
     * {@return an ElementCountMatcher matching specified {@code min-elements}, or {@code null} if no matching is
     * needed}
     * @param minElements the {@link AtLeast}
     */
    static @Nullable ElementCountMatcher atLeast(final AtLeast minElements) {
        return minElements.matchesAll() ? null : minElements;
    }

    /**
     * {@return an ElementCountMatcher matching specified {@code min-elements}, or {@code null} if no matching is
     * needed}
     * @param maxElements the {@link AtMost}
     */
    static @Nullable ElementCountMatcher atMost(final AtMost maxElements) {
        return maxElements.matchesAll() ? null : maxElements;
    }

    /**
     * {@return an ElementCountMatcher matching optional {@code min-elements} and optional {@code max-elements},
     * or {@code null} if no matching is needed}
     * @param minElements the {@link AtLeast} or {@code null}
     * @param maxElements the {@link AtMost} or {@code null}
     */
    static @Nullable ElementCountMatcher ofNullable(final @Nullable AtLeast minElements,
            final @Nullable AtMost maxElements) {
        if (minElements == null) {
            return maxElements == null ? null : atMost(maxElements);
        }
        return maxElements == null ? atLeast(minElements) : ofRange(minElements, maxElements);
    }

    /**
     * {@return an ElementCountMatcher matching specified {@code min-elements} and {@code max-elements}, or {@code null}
     * if no matching is needed}
     * @param minElements the {@link AtLeast}
     * @param maxElements the {@link AtMost}
     */
    static @Nullable ElementCountMatcher ofRange(final AtLeast minElements, final AtMost maxElements) {
        if (minElements.matchesAll()) {
            return atMost(maxElements);
        }
        return maxElements.matchesAll() ? atLeast(minElements) : new ElementCountRange(minElements, maxElements);
    }

    /**
     * {@return {@code null} if {@code elementCount} matches this matcher or a {@link Violation}}
     * @param elementCount the element count
     */
    @Nullable Violation matches(int elementCount);

    /**
     * {@return {@code null} if {@code elementCount} matches this matcher or a {@link Violation}}
     * @param elementCount the element count
     */
    @Nullable Violation matches(long elementCount);

    /**
     * {@return {@code null} if {@code elementCount} matches this matcher or a {@link Violation}}
     * @param elementCount the element count
     */
    @Nullable Violation matches(BigInteger elementCount);

    /**
     * {@return {@code true} if this matcher is guaranteed to match any number of elements}
     */
    boolean matchesAll();
}
