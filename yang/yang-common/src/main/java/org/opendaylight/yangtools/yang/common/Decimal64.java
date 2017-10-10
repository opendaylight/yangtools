/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.primitives.Longs;
import java.math.BigDecimal;

/**
 * Dedicated type for YANG's 'type decimal64' type.
 *
 * @author Robert Varga
 */
@Beta
public final class Decimal64 extends Number implements Comparable<Decimal64> {
    private static final long serialVersionUID = 1L;

    private static final int MAX_FRACTION_DIGITS = 18;

    private static final long SCALE[] = {
            10,
            100,
            1000,
            10000,
            100000,
            1000000,
            10000000,
            100000000,
            1000000000,
            10000000000L,
            100000000000L,
            1000000000000L,
            10000000000000L,
            100000000000000L,
            1000000000000000L,
            10000000000000000L,
            100000000000000000L,
            1000000000000000000L
    };

    static {
        verify(SCALE.length == MAX_FRACTION_DIGITS);
    }

    private final byte fractionDigits;
    private final long value;

    @VisibleForTesting
    Decimal64(final int fractionDigits, final long intPart, final long fracPart, final boolean negative) {
        checkArgument(fractionDigits >= 1 && fractionDigits <= MAX_FRACTION_DIGITS);
        this.fractionDigits = (byte) (fractionDigits - 1);

        final long bits = intPart * SCALE[this.fractionDigits] + fracPart;
        this.value = negative ? -bits : bits;
    }

    /**
     * Attempt to parse a String into a Decimal64. This method uses minimum fraction digits required to hold
     * the entire value.
     *
     * @param value
     * @return
     * @throws NullPointerException if value is null.
     * @throws NumberFormatException if the string does not contain a parsable decimal64.
     */
    public static Decimal64 parse(final String value) {
        // https://tools.ietf.org/html/rfc6020#section-9.3.1
        //
        // A decimal64 value is lexically represented as an optional sign ("+"
        // or "-"), followed by a sequence of decimal digits, optionally
        // followed by a period ('.') as a decimal indicator and a sequence of
        // decimal digits.  If no sign is specified, "+" is assumed.
        if (value.isEmpty()) {
            throw new NumberFormatException("Empty string is not a valid decimal64 representation");
        }

        // Deal with optional sign
        final boolean negative;
        int idx;
        switch (value.charAt(0)) {
            case '-':
                negative = true;
                idx = 1;
                break;
            case '+':
                negative = false;
                idx = 1;
                break;
            default:
                negative = false;
                idx = 0;
        }

        // Sanity check length
        if (idx == value.length()) {
            throw new NumberFormatException("Missing digits after sign");
        }

        // Character limit, used for caching and cutting trailing zeroes
        int limit = value.length() - 1;

        // Skip any leading zeroes, but leave at least one
        for (; idx < limit && value.charAt(idx) == '0'; idx++) {
            final char ch = value.charAt(idx + 1);
            if (ch < '0' || ch > '9') {
                break;
            }
        }

        // Integer part and its length
        int intLen = 0;
        long intPart = 0;

        for (; idx <= limit; idx++, intLen++) {
            final char ch = value.charAt(idx);
            if (ch == '.') {
                // Fractions are next
                break;
            }
            if (ch < '0' || ch > '9') {
                throw new NumberFormatException("Illegal character at offset " + idx);
            }
            if (intLen == MAX_FRACTION_DIGITS) {
                throw new NumberFormatException("Integer part is longer than " + MAX_FRACTION_DIGITS + " digits");
            }

            intPart = 10 * intPart + ch - '0';
        }

        if (idx > limit) {
            // No fraction digits, we are done
            return new Decimal64((byte)1, intPart, 0, negative);
        }

        // Bump index to skip over period and check the remainder
        idx++;
        if (idx > limit) {
            throw new NumberFormatException("Value '" + value + "' is missing fraction digits");
        }

        // Trim trailing zeroes, if any
        while (idx < limit && value.charAt(limit) == '0') {
            limit--;
        }

        final int fracLimit = MAX_FRACTION_DIGITS - intLen;
        byte fracLen = 0;
        long fracPart = 0;
        for (; idx <= limit; idx++, fracLen++) {
            final char ch = value.charAt(idx);
            if (ch < '0' || ch > '9') {
                throw new NumberFormatException("Illegal character at offset " + idx);
            }
            if (fracLen == fracLimit) {
                throw new NumberFormatException("Fraction part longer than " + fracLimit + " digits");
            }

            fracPart = 10 * fracPart + ch - '0';
        }

        return new Decimal64(fracLen, intPart, fracPart, negative);
    }

    @Override
    public int compareTo(final Decimal64 o) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int intValue() {
        return (int) intPart();
    }

    @Override
    public long longValue() {
        return intPart();
    }

    @Override
    public float floatValue() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double doubleValue() {
        // TODO Auto-generated method stub
        return 0;
    }

    public BigDecimal decimalValue() {
        return BigDecimal.valueOf(value, fractionDigits + 1);
    }

    @Override
    public int hashCode() {
        return Longs.hashCode(value) * 31 + fractionDigits;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Decimal64)) {
            return false;
        }
        final Decimal64 other = (Decimal64) obj;
        if (fractionDigits == other.fractionDigits) {
            return value == other.value;
        }

        // We need to normalize both
        return intPart() == other.intPart() && fracPart() == fracPart();
    }

    @Override
    public String toString() {
        // https://tools.ietf.org/html/rfc6020#section-9.3.2
        //
        // The canonical form of a positive decimal64 does not include the sign
        // "+".  The decimal point is required.  Leading and trailing zeros are
        // prohibited, subject to the rule that there MUST be at least one digit
        // before and after the decimal point.  The value zero is represented as
        // "0.0".
        return new StringBuilder(21).append(intPart()).append('.').append(fracPart()).toString();
    }

    private long intPart() {
        return value / SCALE[fractionDigits];
    }

    private long fracPart() {
        return Math.abs(value % SCALE[fractionDigits]);
    }
}
