/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import java.math.BigInteger;
import java.text.ParseException;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * An argument to {@code min-elements} statement.
 *
 * @since 15.0.0
 */
// Note: implementations of compareTo() rely on:
//         - the inability to directly instantiate individual implementations outside of static methods in this
//           interface, and
//         - those static methods choosing the minimal representation for each possible value
//       this means that:
//         - any *32 object is less than any *64 object
//         - any *64 object is less than any *Big object
//       and all that remains to be implemented are intra-class comparisons, which are nigh trivial
@NonNullByDefault
public sealed interface MinElementsArgument extends Comparable<MinElementsArgument>, ElementCountMatcher
        permits MinElementsArgument32, MinElementsArgument64, MinElementsArgumentBig {
    /**
     * {@return a {@link MinElementsArgument} with specified value}
     * @param value the value
     * @throws IllegalArgumentException if value is negative
     */
    static MinElementsArgument of(final int value) {
        if (value < 0) {
            throw new IllegalArgumentException("negative value " + value);
        }
        return new MinElementsArgument32(value - 1);
    }

    /**
     * {@return a {@link MinElementsArgument} with specified value}
     * @param value the value
     * @throws IllegalArgumentException if value is negative
     */
    static MinElementsArgument of(final long value) {
        if (value < 0) {
            throw new IllegalArgumentException("negative value " + value);
        }
        final long lower = value - 1;
        return value <= Integer.MAX_VALUE ? new MinElementsArgument32((int) lower) : new MinElementsArgument64(lower);
    }

    /**
     * {@return a {@link MinElementsArgument} with specified value}
     * @param value the value
     * @throws IllegalArgumentException if value is negative
     */
    static MinElementsArgument of(final BigInteger value) {
        if (value.compareTo(BigInteger.ZERO) < 0) {
            throw new IllegalArgumentException("negative value " + value);
        }
        return value.compareTo(MaxElementsArgumentBig.LONG_MAX_VALUE) <= 0
            ? of(value.longValueExact())
            : new MinElementsArgumentBig(value.subtract(BigInteger.ONE));
    }

    /**
     * Parse a string as a {@code min-value-arg} ABNF production, as defined in
     * <a href="https://www.rfc-editor.org/rfc/rfc7950#page191">RFC7950</a>.
     *
     * @param argument the string representation
     * @return a {@link MinElementsArgument}
     * @throws ParseException if the representation is not valid
     */
    static MinElementsArgument parse(final String argument) throws ParseException {
        // parse a non-negative-integer-value
        final var length = argument.length();
        return switch (length) {
            case 0 -> throw new ParseException("empty min-value-arg", 0);
            case 1 -> parse(argument.charAt(0));
            default -> parse(argument, length);
        };
    }

    private static MinElementsArgument parse(final char ch) throws ParseException {
        if (ch < '0' || ch > '9') {
            throw new ParseException("'" + ch + "' is not a valid DIGIT", 0);
        }
        return of(ch - '0');
    }

    private static MinElementsArgument parse(final String argument, final int length) throws ParseException {
        var ch = argument.charAt(0);
        if (ch < '1' || ch > '9') {
            throw new ParseException("'" + ch + "' is not a valid non-zero-digit", 0);
        }
        for (int i = 1; i < length; i++) {
            ch = argument.charAt(i);
            if (ch < '0' || ch > '9') {
                throw new ParseException("'" + ch + "' is not a valid DIGIT", i);
            }
        }

        // at this point we have established that argument conforms to the ABNF. Any IAEs stemming from code below are
        // a manifestation of a bug in code above.

        // log10(value) == length here, allowing us to use primitive type parse results
        if (length <= 9) {
            // 10^9 is less than 2^31-1, defer to Integer.parseInt()
            return of(Integer.parseInt(argument));
        }
        if (length <= 18) {
            // 10^18 is less than 2^63-1, defer to Long.parseLong()
            return of(Long.parseLong(argument));
        }
        return of(new BigInteger(argument));
    }

    @Override
    default boolean matches(final int elementCount) {
        return elementCount > lowerInt();
    }

    @Override
    default boolean matches(final long elementCount) {
        return elementCount > lowerLong();
    }

    @Override
    default boolean matches(final BigInteger elementCount) {
        return elementCount.compareTo(lowerBig()) > 0;
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
     * {@return interned equivalent of this argument}
     */
    default MinElementsArgument intern() {
        return MinElementsArgument32.INTERNER.intern(this);
    }

    /**
     * {@return the string representation of the minimum number of elements}
     */
    @Override
    String toString();
}