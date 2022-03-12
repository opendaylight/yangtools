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
import java.math.BigDecimal;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Either;

/**
 * Dedicated type for YANG's 'type decimal64' type. This class is similar to {@link BigDecimal}, but provides more
 * efficient storage, as it has fixed precision.
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public class Decimal64 extends Number implements CanonicalValue<Decimal64> {
    public static final class Support extends AbstractCanonicalValueSupport<Decimal64> {
        public Support() {
            super(Decimal64.class);
        }

        @Override
        public Either<Decimal64, CanonicalValueViolation> fromString(final String str) {
            // https://tools.ietf.org/html/rfc6020#section-9.3.1
            //
            // A decimal64 value is lexically represented as an optional sign ("+"
            // or "-"), followed by a sequence of decimal digits, optionally
            // followed by a period ('.') as a decimal indicator and a sequence of
            // decimal digits.  If no sign is specified, "+" is assumed.
            if (str.isEmpty()) {
                return CanonicalValueViolation.variantOf("Empty string is not a valid decimal64 representation");
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
                return CanonicalValueViolation.variantOf("Missing digits after sign");
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
                if (intLen == MAX_SCALE) {
                    return CanonicalValueViolation.variantOf(
                        "Integer part is longer than " + MAX_SCALE + " digits");
                }

                intPart = 10 * intPart + toInt(ch, idx);
            }

            if (idx > limit) {
                // No fraction digits, we are done
                return Either.ofFirst(new Decimal64((byte)1, intPart, 0, negative));
            }

            // Bump index to skip over period and check the remainder
            idx++;
            if (idx > limit) {
                return CanonicalValueViolation.variantOf("Value '" + str + "' is missing fraction digits");
            }

            // Trim trailing zeroes, if any
            while (idx < limit && str.charAt(limit) == '0') {
                limit--;
            }

            final int fracLimit = MAX_SCALE - intLen + 1;
            byte fracLen = 0;
            long fracPart = 0;
            for (; idx <= limit; idx++, fracLen++) {
                final char ch = str.charAt(idx);
                if (fracLen == fracLimit) {
                    return CanonicalValueViolation.variantOf("Fraction part longer than " + fracLimit + " digits");
                }

                fracPart = 10 * fracPart + toInt(ch, idx);
            }

            return Either.ofFirst(new Decimal64(fracLen, intPart, fracPart, negative));
        }

        private static int toInt(final char ch, final int index) {
            if (ch < '0' || ch > '9') {
                throw new NumberFormatException("Illegal character at offset " + index);
            }
            return ch - '0';
        }
    }

    private static final CanonicalValueSupport<Decimal64> SUPPORT = new Support();
    private static final long serialVersionUID = 1L;

    private static final int MAX_SCALE = 18;

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
        verify(SCALE.length == MAX_SCALE);
    }

    private final byte scaleOffset;
    private final long value;

    @VisibleForTesting
    Decimal64(final int scale, final long intPart, final long fracPart, final boolean negative) {
        scaleOffset = offsetOf(scale);

        final long bits = intPart * SCALE[scaleOffset] + fracPart;
        value = negative ? -bits : bits;
    }

    private Decimal64(final byte scaleOffset, final long value) {
        this.scaleOffset = scaleOffset;
        this.value = value;
    }

    protected Decimal64(final Decimal64 other) {
        this(other.scaleOffset, other.value);
    }

    /**
     * Return a {@link Decimal64} with specified scale and unscaled value
     *
     * @param scale scale to use
     * @param unscaledValue unscaled value to use
     * @return A Decimal64 instance
     * @throws IllegalArgumentException if {@code scale} is not in range {@code [1..18]}
     */
    public static Decimal64 of(final int scale, final long unscaledValue) {
        return new Decimal64(offsetOf(scale), unscaledValue);
    }

    // >>> FIXME: these need to take a scale value and perform a range check. we also need truncating counterparts
    public static Decimal64 valueOf(final byte byteVal) {
        return byteVal < 0 ? new Decimal64(1, -byteVal, 0, true) : new Decimal64(1, byteVal, 0, false);
    }

    public static Decimal64 valueOf(final short shortVal) {
        return shortVal < 0 ? new Decimal64(1, -shortVal, 0, true) : new Decimal64(1, shortVal, 0, false);
    }

    public static Decimal64 valueOf(final int intVal) {
        return intVal < 0 ? new Decimal64(1, - (long)intVal, 0, true) : new Decimal64(1, intVal, 0, false);
    }

    public static Decimal64 valueOf(final long longVal) {
        // XXX: we should be able to do something smarter here
        return valueOf(Long.toString(longVal));
    }
    // <<< FIXME

    // FIXME: this should take a RoundingMode and perform rounding
    // FIXME: this should have a float counterpart
    // FIXME: this should have a truncating
    public static Decimal64 valueOf(final double doubleVal) {
        // XXX: we should be able to do something smarter here
        return valueOf(Double.toString(doubleVal));
    }

    public static Decimal64 valueOf(final BigDecimal decimalVal) {
        // FIXME: we should be able to do something smarter here using BigDecimal.unscaledValue() and BigDecimal.scale()
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
        final Either<Decimal64, CanonicalValueViolation> variant = SUPPORT.fromString(str);
        final Optional<Decimal64> value = variant.tryFirst();
        if (value.isPresent()) {
            return value.get();
        }
        final Optional<String> message = variant.getSecond().getMessage();
        throw message.isPresent() ? new NumberFormatException(message.get()) : new NumberFormatException();
    }

    /**
     * Return the scale of this decimal. This is the number of fraction digits, in range {@code [1..18]}.
     *
     * @return This decimal's scale
     */
    public final int scale() {
        return scaleOffset + 1;
    }

    /**
     * Return the unscaled value of this decimal.
     *
     * @return This decimal's unscaled value
     */
    public final long unscaledValue() {
        return value;
    }

    public final BigDecimal decimalValue() {
        return BigDecimal.valueOf(value, scale());
    }

    @Override
    public final int intValue() {
        return (int) intPart();
    }

    @Override
    public final long longValue() {
        return intPart();
    }

    @Override
    public final float floatValue() {
        return (float) doubleValue();
    }

    @Override
    public final double doubleValue() {
        return 1.0 * value / SCALE[scaleOffset];
    }

    /**
     * Converts this {@code BigDecimal} to a {@code byte}, checking for lost information. If this {@code Decimal64} has
     * a nonzero fractional part or is out of the possible range for a {@code byte} result then
     * an {@code ArithmeticException} is thrown.
     *
     * @return this {@code Decimal64} converted to a {@code byte}.
     * @throws ArithmeticException if {@code this} has a nonzero fractional part, or will not fit in a {@code byte}.
     */
    public final byte byteValueExact() {
        final long val = longValueExact();
        final byte ret = (byte) val;
        if (val != ret) {
            throw new ArithmeticException("Value " + val + " is outside of byte range");
        }
        return ret;
    }

    /**
     * Converts this {@code BigDecimal} to a {@code short}, checking for lost information. If this {@code Decimal64} has
     * a nonzero fractional part or is out of the possible range for a {@code short} result then
     * an {@code ArithmeticException} is thrown.
     *
     * @return this {@code Decimal64} converted to a {@code short}.
     * @throws ArithmeticException if {@code this} has a nonzero fractional part, or will not fit in a {@code short}.
     */
    public final short shortValueExact() {
        final long val = longValueExact();
        final short ret = (short) val;
        if (val != ret) {
            throw new ArithmeticException("Value " + val + " is outside of short range");
        }
        return ret;
    }

    /**
     * Converts this {@code BigDecimal} to an {@code int}, checking for lost information. If this {@code Decimal64} has
     * a nonzero fractional part or is out of the possible range for an {@code int} result then
     * an {@code ArithmeticException} is thrown.
     *
     * @return this {@code Decimal64} converted to an {@code int}.
     * @throws ArithmeticException if {@code this} has a nonzero fractional part, or will not fit in an {@code int}.
     */
    public final int intValueExact() {
        final long val = longValueExact();
        final int ret = (int) val;
        if (val != ret) {
            throw new ArithmeticException("Value " + val + " is outside of integer range");
        }
        return ret;
    }

    /**
     * Converts this {@code BigDecimal} to a {@code long}, checking for lost information.  If this {@code Decimal64} has
     * a nonzero fractional part then an {@code ArithmeticException} is thrown.
     *
     * @return this {@code Decimal64} converted to a {@code long}.
     * @throws ArithmeticException if {@code this} has a nonzero fractional part.
     */
    public final long longValueExact() {
        if (fracPart() != 0) {
            throw new ArithmeticException("Conversion of " + this + " would lose fraction");
        }
        return intPart();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public final int compareTo(final Decimal64 o) {
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
    public final String toCanonicalString() {
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
            sb.append(Strings.padStart(Long.toString(fracPart), scale(), '0'));
        } else {
            sb.append('0');
        }

        return sb.toString();
    }

    @Override
    public final CanonicalValueSupport<Decimal64> support() {
        return SUPPORT;
    }

    @Override
    public final int hashCode() {
        // We need to normalize the results in order to be consistent with equals()
        return Long.hashCode(intPart()) * 31 + Long.hashCode(fracPart());
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof Decimal64 && equalsImpl((Decimal64) obj);
    }

    /**
     * A slightly faster version of {@link #equals(Object)}.
     *
     * @param obj Decimal64 object
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
     */
    public final boolean equals(final @Nullable Decimal64 obj) {
        return this == obj || obj != null && equalsImpl(obj);
    }

    @Override
    public final String toString() {
        return toCanonicalString();
    }

    private boolean equalsImpl(final Decimal64 other) {
        return scaleOffset == other.scaleOffset ? value == other.value
                // We need to normalize both
                : intPart() == other.intPart() && fracPart() == other.fracPart();
    }

    private long intPart() {
        return value / SCALE[scaleOffset];
    }

    private long fracPart() {
        return Math.abs(value % SCALE[scaleOffset]);
    }

    private static byte offsetOf(final int scale) {
        checkArgument(scale >= 1 && scale <= MAX_SCALE);
        return (byte) (scale - 1);
    }
}
