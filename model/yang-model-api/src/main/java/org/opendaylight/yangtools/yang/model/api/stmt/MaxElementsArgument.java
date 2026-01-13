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
        @Deprecated(forRemoval = true)
        public Unbounded intern() {
            return this;
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
     */
    // All implementations are kept internal and carve out the BigInteger value space into memory-efficient
    // representations for 'int' and 'long'. This is done via MaxElementsArgument32.of() and MaxElementsArgument64.of()
    // cascade.
    sealed interface Bounded extends MaxElementsArgument
            // Note: we could also use 'Int' prefix/suffix instead of '32' suffix, but this way the name's alpha sort
            //       matches the storage efficiency preference
            permits MaxElementsArgument32, MaxElementsArgument64, MaxElementsArgumentBig {
        @Override
        default Bounded intern() {
            return MaxElementsArgument32.INTERNER.intern(this);
        }

        @Override
        default boolean matches(final BigInteger elementCount) {
            return elementCount.compareTo(asBigInteger()) <= 0;
        }

        /**
         * {@return this value as a {@link BigInteger}, guaranteed to be greater than {@link BigInteger#ZERO}}
         */
        BigInteger asBigInteger();

        /**
         * Compare this instance to another {@link Bounded}.
         *
         * @param obj the other instance
         * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater
         *         than the specified object
         */
        // Note: 'obj' to keep similarity with compareTo(Bounded) we would like, but SpotBugs rejects due to naming
        //       similarity. That also allows 'other' to be used as a common identifier in a switch pattern.
        int compareToOther(Bounded obj);
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

        // parse a positive-integer-value ABNF production
        final var length = argument.length();
        if (length == 0) {
            throw new IllegalArgumentException("empty max-value-arg");
        }
        final var first = argument.charAt(0);
        if (first < '1' || first > '9') {
            throw new IllegalArgumentException("'" + argument + "' is not a valid max-value-arg on position 1: '"
                + first + "' is not a valid non-zero-digit");
        }
        for (int i = 1; i < length; i++) {
            final var ch = argument.charAt(i);
            if (ch < '0' || ch > '9') {
                throw new IllegalArgumentException("'" + argument + "' is not a valid max-value-arg on position "
                    + (i + 1) + ": '" + ch + "' is not a valid DIGIT");
            }
        }

        // log10(value) == length here, allowing us to use primitive type parse results
        if (length <= 9) {
            // 10^9 is less than 2^31-1, defer to Integer.parseInt()
            return ofArgument(Integer.parseInt(argument));
        }
        if (length <= 18) {
            // 10^18 is less than 2^63-1, defer to Long.parseLong()
            return ofArgument(Long.parseLong(argument));
        }
        return ofArgument(new BigInteger(argument));
    }

    private static MaxElementsArgument32 ofArgument(final int value) {
        return new MaxElementsArgument32(value);
    }

    private static MaxElementsArgument.Bounded ofArgument(final long value) {
        return value <= Integer.MAX_VALUE ? ofArgument((int) value) : new MaxElementsArgument64(value);
    }

    private static MaxElementsArgument.Bounded ofArgument(final BigInteger value) {
        return value.compareTo(MaxElementsArgumentBig.LONG_MAX_VALUE) <= 0 ? ofArgument(value.longValueExact())
            : new MaxElementsArgumentBig(value);
    }

    /**
     * {@return this argument saturated to {@link Integer#MAX_VALUE}}
     */
    int asSaturatedInt();

    /**
     * {@return this argument saturated to {@link Long#MAX_VALUE}}
     */
    long asSaturatedLong();

    /**
     * {@return interned equivalent of this argument}
     */
    MaxElementsArgument intern();

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    default int compareTo(final MaxElementsArgument o) {
        return switch (o) {
            case Unbounded other -> this instanceof Unbounded ? 0 : -1;
            case Bounded other -> this instanceof Bounded bounded ? bounded.compareToOther(other) : 1;
        };
    }
}
