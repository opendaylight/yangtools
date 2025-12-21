/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import java.math.BigInteger;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.stmt.MaxElementsArgument.Bounded;
import org.opendaylight.yangtools.yang.model.api.stmt.MaxElementsArgument.Unbounded;

/**
 * An argument to {@link MaxElementsStatement}. Can be either {@link Bounded} or {@link Unbounded}.
 *
 * @since 15.0.0
 */
// FIXME: use JEP-401 when available
@NonNullByDefault
public sealed interface MaxElementsArgument extends Comparable<MaxElementsArgument>, ElementsConstraint
        permits Bounded, Unbounded {
    /**
     * Singleton value representing an {@code unbounded} value.
     */
    final class Unbounded implements MaxElementsArgument {
        /**
         * The singleton instance.
         */
        public static final Unbounded INSTANCE = new Unbounded();

        private Unbounded() {
            // Hidden on purpose
        }

        @Override
        public int asSaturatedInt() {
            return Integer.MAX_VALUE;
        }

        @Override
        public long asSaturatedLong() {
            return Long.MAX_VALUE;
        }

        @Override
        public boolean matches(final int elementCount) {
            return true;
        }

        @Override
        public boolean matches(final long elementCount) {
            return true;
        }

        @Override
        public boolean matches(final BigInteger elementCount) {
            return true;
        }

        @Override
        public String toString() {
            return "unbounded";
        }
    }

    /**
     * A {@link MaxElementsArgument} with an upper bound on elements.
     *
     * @implSpec this interface establishes exclusion alongside
     */
    // All implementations are kept internal to establish the following invariant:
    sealed interface Bounded extends MaxElementsArgument
            permits MaxElementsArgument32, MaxElementsArgument64, MaxElementsArgumentBig {
        /**
         * {@return this value as a {@link BigInteger}, guaranteed to be greater than {@link BigInteger#ZERO}}
         */
        BigInteger asBigInteger();

        /**
         * Compare this instance to another object.
         *
         * @param obj the other instance
         * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater
         *         than the specified object
         */
        int compareTo(Bounded obj);

        @Override
        default boolean matches(final BigInteger elementCount) {
            return elementCount.compareTo(asBigInteger()) <= 0;
        }
    }

    /**
     * Return a {@link MaxElementsArgument} value for specified argument string.
     *
     * @param argument the argument string
     * @return the {@link MaxElementsArgument} value
     * @throws IllegalArgumentException if {@code argument} does not represent a valid value
     */
    static MaxElementsArgument ofArgument(final String argument) {
        if (argument.equals("unbounded")) {
            return Unbounded.INSTANCE;
        }
        final var value = new BigInteger(argument);
        if (value.compareTo(BigInteger.ONE) < 0) {
            throw new IllegalArgumentException("non-positive max-elements");
        }
        return MaxElementsArgument32.ofArgument(value);
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    default int compareTo(final MaxElementsArgument o) {
        return switch (o) {
            case Unbounded other -> this instanceof Unbounded ? 0 : -1;
            case Bounded other -> this instanceof Bounded bounded ? bounded.compareTo(other) : 1;
        };
    }

    /**
     * {@return this argument saturated to {@code int}}
     */
    int asSaturatedInt();

    /**
     * {@return this argument saturated to {@code long}}
     */
    long asSaturatedLong();

    /**
     * {@return the canonical representation of this value}
     */
    @Override String toString();
}
