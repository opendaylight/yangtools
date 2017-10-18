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
import com.google.common.base.Strings;
import com.google.common.primitives.Longs;
import java.math.BigDecimal;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * Dedicated type for YANG's 'type decimal64' type. This class is similar to {@link BigDecimal}, but provides more
 * efficient storage, as it has fixed precision.
 *
 * @author Robert Varga
 */
@Beta
public final class Decimal64 extends Number implements Comparable<Decimal64>, Immutable {
    private static final long serialVersionUID = 1L;

    private static final int MAX_FRACTION_DIGITS = 18;

    private static final long[] SCALE = {
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

    private final byte scaleOffset;
    private final long value;

    @VisibleForTesting
    Decimal64(final int fractionDigits, final long intPart, final long fracPart, final boolean negative) {
        checkArgument(fractionDigits >= 1 && fractionDigits <= MAX_FRACTION_DIGITS);
        this.scaleOffset = (byte) (fractionDigits - 1);

        final long bits = intPart * SCALE[this.scaleOffset] + fracPart;
        this.value = negative ? -bits : bits;
    }

    public static Decimal64 valueOf(final byte byteVal) {
        return byteVal < 0 ? new Decimal64(1, -byteVal, 0, true) : new Decimal64(1, byteVal, 0, false);
    }

    public static Decimal64 valueOf(final short shortVal) {
        return shortVal < 0 ? new Decimal64(1, -shortVal, 0, true) : new Decimal64(1, shortVal, 0, false);
    }

    public static Decimal64 valueOf(final int intVal) {
        return intVal < 0 ? new Decimal64(1, -intVal, 0, true) : new Decimal64(1, intVal, 0, false);
    }

    public static Decimal64 valueOf(final long longVal) {
        // XXX: we should be able to do something smarter here
        return valueOf(Long.toString(longVal));
    }

    public static Decimal64 valueOf(final double doubleVal) {
        // XXX: we should be able to do something smarter here
        return valueOf(Double.toString(doubleVal));
    }

    public static Decimal64 valueOf(final BigDecimal decimalVal) {
        // XXX: we should be able to do something smarter here
        return valueOf(decimalVal.toPlainString());
    }

    /**
     * Attempt to parse a String into a Decimal64. This method uses minimum fraction digits required to hold
     * the entire value.
     *
     * @param str String to parser
     * @return A Decimal64 instance
     * @throws NullPointerException if value is null.
     * @throws NumberFormatException if the string does not contain a parsable decimal64.
     */
    public static Decimal64 valueOf(final String str) {
        // https://tools.ietf.org/html/rfc6020#section-9.3.1
        //
        // A decimal64 value is lexically represented as an optional sign ("+"
        // or "-"), followed by a sequence of decimal digits, optionally
        // followed by a period ('.') as a decimal indicator and a sequence of
        // decimal digits.  If no sign is specified, "+" is assumed.
        if (str.isEmpty()) {
            throw new NumberFormatException("Empty string is not a valid decimal64 representation");
        }

        // Deal with optional sign
        final boolean negative;
        int idx;
        switch (str.charAt(0)) {
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
        if (idx == str.length()) {
            throw new NumberFormatException("Missing digits after sign");
        }

        // Character limit, used for caching and cutting trailing zeroes
        int limit = str.length() - 1;

        // Skip any leading zeroes, but leave at least one
        for (; idx < limit && str.charAt(idx) == '0'; idx++) {
            final char ch = str.charAt(idx + 1);
            if (ch < '0' || ch > '9') {
                break;
            }
        }

        // Integer part and its length
        int intLen = 0;
        long intPart = 0;

        for (; idx <= limit; idx++, intLen++) {
            final char ch = str.charAt(idx);
            if (ch == '.') {
                // Fractions are next
                break;
            }
            if (intLen == MAX_FRACTION_DIGITS) {
                throw new NumberFormatException("Integer part is longer than " + MAX_FRACTION_DIGITS + " digits");
            }

            intPart = 10 * intPart + toInt(ch, idx);
        }

        if (idx > limit) {
            // No fraction digits, we are done
            return new Decimal64((byte)1, intPart, 0, negative);
        }

        // Bump index to skip over period and check the remainder
        idx++;
        if (idx > limit) {
            throw new NumberFormatException("Value '" + str + "' is missing fraction digits");
        }

        // Trim trailing zeroes, if any
        while (idx < limit && str.charAt(limit) == '0') {
            limit--;
        }

        final int fracLimit = MAX_FRACTION_DIGITS - intLen;
        byte fracLen = 0;
        long fracPart = 0;
        for (; idx <= limit; idx++, fracLen++) {
            final char ch = str.charAt(idx);
            if (fracLen == fracLimit) {
                throw new NumberFormatException("Fraction part longer than " + fracLimit + " digits");
            }

            fracPart = 10 * fracPart + toInt(ch, idx);
        }

        return new Decimal64(fracLen, intPart, fracPart, negative);
    }

    public BigDecimal decimalValue() {
        return BigDecimal.valueOf(value, scaleOffset + 1);
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
        return (float) doubleValue();
    }

    @Override
    public double doubleValue() {
        return 1.0 * value / SCALE[scaleOffset];
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public int compareTo(final Decimal64 o) {
        if (this == o) {
            return 0;
        }
        if (scaleOffset == o.scaleOffset) {
            return Long.compare(value, o.value);
        }

        // XXX: we could do something smarter here
        return Double.compare(doubleValue(), o.doubleValue());
    }

    @Override
    public int hashCode() {
        // We need to normalize the results in order to be consistent with equals()
        return Longs.hashCode(intPart()) * 31 + Long.hashCode(fracPart());
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
        if (scaleOffset == other.scaleOffset) {
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
        final StringBuilder sb = new StringBuilder(21).append(intPart()).append('.');
        final long fracPart = fracPart();
        if (fracPart != 0) {
            // We may need to zero-pad the fraction part
            sb.append(Strings.padStart(Long.toString(fracPart), scaleOffset + 1, '0'));
        } else {
            sb.append('0');
        }

        return sb.toString();
    }

    private long intPart() {
        return value / SCALE[scaleOffset];
    }

    private long fracPart() {
        return Math.abs(value % SCALE[scaleOffset]);
    }

    private static int toInt(final char ch, final int index) {
        if (ch < '0' || ch > '9') {
            throw new NumberFormatException("Illegal character at offset " + index);
        }
        return ch - '0';
    }
}
