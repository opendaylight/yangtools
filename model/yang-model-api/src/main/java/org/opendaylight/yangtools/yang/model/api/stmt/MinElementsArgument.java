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
@NonNullByDefault
public sealed interface MinElementsArgument extends Comparable<MinElementsArgument>, ElementCountMatcher
        permits MinElementsArgument32, MinElementsArgument64, MinElementsArgumentBig {
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

    // Note: implementations of this method rely on:
    //         - the inability to directly instantiate individual implementations outside of static methods in this
    //           interface, and
    //         - those static methods choosing the minimal representation for each possible value
    //       this means that:
    //         - any *31 object is less than any *63 object
    //         - any *63 object is less than any *Str object
    //       and all that remains to be implemented are intra-class comparisons, which are nigh trivial
    @Override
    int compareTo(MinElementsArgument other);

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
     * Parse a string as a {@code min-value-arg} ABNF production, as defined in
     * <a href="https://www.rfc-editor.org/rfc/rfc7950#page191">RFC7950</a>.
     *
     * @param str the string representation
     * @return a {@link MinElementsArgument}
     * @throws ParseException if the representation is not valid
     */
    static MinElementsArgument parse(final String str) throws ParseException {
        final var len = str.length();
        return switch (len) {
            case 0 -> throw new ParseException("Empty value", 0);
            case 1 -> of(parseDigit(str, 0));
            default -> parse(str, len);
        };
    }

    private static MinElementsArgument parse(final String str, final int len) throws ParseException {
        // Integer.MAX_VALUE is 10 characters, so anything shorter can be fast with an int variable
        if (len < 10) {
            return parse31(str, len);
        }
        // Long.MAX_VALUE is 19 characters, so anything shorter can be fast with a long variable, otherwise we need to
        // be more careful.
        return len < 19 ? parse63(str, len) : parseStr(str, len);
    }

    private static int parseDigit(final String str, final int index) throws ParseException {
        final var ch = str.charAt(index);
        final int value = ch - '0';
        if (value >= 0 && value <= 9) {
            return value;
        }
        throw new ParseException("Invalid character '" + ch + "'", index);
    }

    private static int parseNotZeroDigit(final String str) throws ParseException {
        final var value = parseDigit(str, 0);
        if (value != 0) {
            return value;
        }
        throw new ParseException("Invalid character '0'", 0);
    }

    // 2 <= len <= 9: any valid value will fit into 31 bits
    private static MinElementsArgument parse31(final String str, final int len) throws ParseException {
        int value = parseNotZeroDigit(str);
        for (int i = 1; i < len; ++i) {
            value = value * 10 + parseDigit(str, i);
        }
        return of(value);
    }

    // 10 <= len <= 18: any valid value will fit into 63 bits
    private static MinElementsArgument parse63(final String str, final int len) throws ParseException {
        long value = parseNotZeroDigit(str);
        for (int i = 1; i < len; ++i) {
            value = value * 10 + parseDigit(str, i);
        }
        return of(value);
    }

    // len >= 19: we need to be careful
    private static MinElementsArgument parseStr(final String str, final int len) throws ParseException {
        // validate all characters first, ensuring the value is positive
        final var first = str.charAt(0);
        if (first < '1' || first > '9') {
            throw new ParseException("Invalid character '" + first + "'", 0);
        }
        for (int i = 1; i < len; ++i) {
            final var ch = str.charAt(i);
            if (ch < '0' || ch > '9') {
                throw new ParseException("Invalid character '" + ch + "'", i);
            }
        }

        // longer than Long.MAX_VALUE: use BigInteger storage
        if (len >= 20) {
            return new MinElementsArgumentBig(str);
        }

        // try to parse as a long if possible
        final long value;
        try {
            value = Long.parseLong(str);
        } catch (NumberFormatException e) {
            // long overflow: use String storage
            return new MinElementsArgumentBig(str, e);
        }
        return of(value);
    }
}