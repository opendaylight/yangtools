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
        /**
         * {@return the {@code error-app-tag}}
         */
        String errorAppTag();
    }

    /**
     * Proposed element count violates constraint imposed by {@code min-elements}, as illustrated in
     * <a href="https://www.rfc-editor.org/rfc/rfc7950#section-15.3">RFC7950 section 15.3</a>.
     *
     * @param atLeast minimum allowed elements
     */
    record TooFewElements(Number atLeast) implements Violation {
        public TooFewElements {
            requireNonNull(atLeast);
        }

        @Override
        public String errorAppTag() {
            return "too-few-elements";
        }
    }

    /**
     * Proposed element count violates constraint imposed by {@code max-elements}, as illustrated in
     * <a href="https://www.rfc-editor.org/rfc/rfc7950#section-15.2">RFC7950 section 15.2</a>.
     *
     * @param atMost maximum allowed elements
     */
    record TooManyElements(Number atMost) implements Violation {
        public TooManyElements {
            requireNonNull(atMost);
        }

        @Override
        public String errorAppTag() {
            return "too-many-elements";
        }
    }

    /**
     * {@return an {@link ElementCountMatcher} matching specified {@code min-elements}, or {@code null} if no matching
     * is needed}
     * @param atLeast the {@link AtLeast} matcher
     */
    static @Nullable ElementCountMatcher atLeast(final AtLeast atLeast) {
        return atLeast.matchesAll() ? null : atLeast;
    }

    /**
     * {@return an ElementCountMatcher matching specified {@code min-elements}, or {@code null} if no matching is
     * needed}
     * @param atMost the {@link AtMost} matcher
     */
    static @Nullable ElementCountMatcher atMost(final AtMost atMost) {
        return atMost.matchesAll() ? null : atMost;
    }

    /**
     * {@return an {@link ElementCountMatcher} matching optional {@code min-elements} and optional {@code max-elements},
     * or {@code null} if no matching is needed}
     * @param atLeast the {@link AtLeast} matcher or {@code null}
     * @param atMost the {@link AtMost} matcher or {@code null}
     */
    static @Nullable ElementCountMatcher ofNullable(final @Nullable AtLeast atLeast, final @Nullable AtMost atMost) {
        if (atLeast == null) {
            return atMost == null ? null : atMost(atMost);
        }
        return atMost == null ? atLeast(atLeast) : ofRange(atLeast, atMost);
    }

    /**
     * {@return an {@link ElementCountMatcher} matching specified {@code min-elements} and {@code max-elements}, or
     * {@code null} if no matching is needed}
     * @param atLeast the {@link AtLeast} matcher
     * @param atMost the {@link AtMost} matcher
     */
    static @Nullable ElementCountMatcher ofRange(final AtLeast atLeast, final AtMost atMost) {
        if (atLeast.matchesAll()) {
            return atMost(atMost);
        }
        return atMost.matchesAll() ? atLeast(atLeast) : new ElementCountRange(atLeast, atMost);
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
