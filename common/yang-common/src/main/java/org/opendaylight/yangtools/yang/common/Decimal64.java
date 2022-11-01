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
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import java.io.Serial;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
            int idx = switch (str.charAt(0)) {
                case '-' -> {
                    negative = true;
                    yield 1;
                }
                case '+' -> {
                    negative = false;
                    yield 1;
                }
                default -> {
                    negative = false;
                    yield 0;
                }
            };
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

    /**
     * Tri-state indicator of how a non-zero remainder is significant to rounding.
     */
    private enum RemainderSignificance {
        /**
         * The remainder is less than the half of the interval.
         */
        LT_HALF,
        /**
         * The remainder is exactly half of the interval.
         */
        HALF,
        /**
         * The remainder is greater than the half of the interval.
         */
        GT_HALF;

        static RemainderSignificance of(final long remainder, final long interval) {
            final long absRemainder = Math.abs(remainder);
            final long half = interval / 2;

            if (absRemainder > half) {
                return GT_HALF;
            } else if (absRemainder < half) {
                return LT_HALF;
            } else {
                return HALF;
            }
        }
    }

    private static final CanonicalValueSupport<Decimal64> SUPPORT = new Support();
    @Serial
    private static final long serialVersionUID = 1L;

    private static final int MAX_SCALE = 18;

    private static final long[] FACTOR = {
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

    private static final Decimal64Conversion[] CONVERSION = Decimal64Conversion.values();
    private static final Decimal64[] MIN_VALUE;
    private static final Decimal64[] MAX_VALUE;

    static {
        verify(CONVERSION.length == MAX_SCALE);
        verify(FACTOR.length == MAX_SCALE);

        MIN_VALUE = new Decimal64[MAX_SCALE];
        MAX_VALUE = new Decimal64[MAX_SCALE];
        for (byte i = 0; i < MAX_SCALE; ++i) {
            MIN_VALUE[i] = new Decimal64(i, Long.MIN_VALUE);
            MAX_VALUE[i] = new Decimal64(i, Long.MAX_VALUE);
        }
    }

    private final byte offset;
    private final long value;

    @VisibleForTesting
    Decimal64(final int scale, final long intPart, final long fracPart, final boolean negative) {
        offset = offsetOf(scale);

        final long bits = intPart * FACTOR[offset] + fracPart;
        value = negative ? -bits : bits;
    }

    private Decimal64(final byte offset, final long intPart, final boolean negative) {
        this.offset = offset;
        final long bits = intPart * FACTOR[offset];
        value = negative ? -bits : bits;
    }

    private Decimal64(final byte offset, final long value) {
        this.offset = offset;
        this.value = value;
    }

    protected Decimal64(final Decimal64 other) {
        this(other.offset, other.value);
    }

    /**
     * Return a {@link Decimal64} with specified scale and unscaled value.
     *
     * @param scale scale to use
     * @param unscaledValue unscaled value to use
     * @return A Decimal64 instance
     * @throws IllegalArgumentException if {@code scale} is not in range {@code [1..18]}
     */
    public static Decimal64 of(final int scale, final long unscaledValue) {
        return new Decimal64(offsetOf(scale), unscaledValue);
    }

    /**
     * Return the minimum value supported in specified scale.
     *
     * @param scale scale to use
     * @return Minimum value in that scale
     * @throws IllegalArgumentException if {@code scale} is not in range {@code [1..18]}
     */
    public static Decimal64 minValueIn(final int scale) {
        return MIN_VALUE[offsetOf(scale)];
    }

    /**
     * Return the maximum value supported in specified scale.
     *
     * @param scale scale to use
     * @return Maximum value in that scale
     * @throws IllegalArgumentException if {@code scale} is not in range {@code [1..18]}
     */
    public static Decimal64 maxValueIn(final int scale) {
        return MAX_VALUE[offsetOf(scale)];
    }

    // >>> FIXME: these need truncating counterparts
    public static Decimal64 valueOf(final int scale, final byte byteVal) {
        final byte offset = offsetOf(scale);
        final var conv = CONVERSION[offset];
        if (byteVal < conv.minByte || byteVal > conv.maxByte) {
            throw new IllegalArgumentException("Value " + byteVal + " is not in range ["
                + conv.minByte + ".." + conv.maxByte + "] to fit scale " + scale);
        }
        return byteVal < 0 ? new Decimal64(offset, -byteVal, true) : new Decimal64(offset, byteVal, false);
    }

    public static Decimal64 valueOf(final int scale, final short shortVal) {
        final byte offset = offsetOf(scale);
        final var conv = CONVERSION[offset];
        if (shortVal < conv.minShort || shortVal > conv.maxShort) {
            throw new IllegalArgumentException("Value " + shortVal + " is not in range ["
                + conv.minShort + ".." + conv.maxShort + "] to fit scale " + scale);
        }
        return shortVal < 0 ? new Decimal64(offset, -shortVal, true) : new Decimal64(offset, shortVal, false);
    }

    public static Decimal64 valueOf(final int scale, final int intVal) {
        final byte offset = offsetOf(scale);
        final var conv = CONVERSION[offset];
        if (intVal < conv.minInt || intVal > conv.maxInt) {
            throw new IllegalArgumentException("Value " + intVal + " is not in range ["
                + conv.minInt + ".." + conv.maxInt + "] to fit scale " + scale);
        }
        return intVal < 0 ? new Decimal64(offset, - (long)intVal, true) : new Decimal64(offset, intVal, false);
    }

    public static Decimal64 valueOf(final int scale, final long longVal) {
        final byte offset = offsetOf(scale);
        final var conv = CONVERSION[offset];
        if (longVal < conv.minLong || longVal > conv.maxLong) {
            throw new IllegalArgumentException("Value " + longVal + " is not in range ["
                + conv.minLong + ".." + conv.maxLong + "] to fit scale " + scale);
        }
        return longVal < 0 ? new Decimal64(offset, -longVal, true) : new Decimal64(offset, longVal, false);
    }
    // <<< FIXME

    // FIXME: this should take a RoundingMode and perform rounding
    // FIXME: this should have a truncating counterpart
    public static Decimal64 valueOf(final float floatVal, final RoundingMode rounding) {
        // XXX: we should be able to do something smarter here
        return valueOf(Float.toString(floatVal));
    }

    // FIXME: this should take a RoundingMode and perform rounding
    // FIXME: this should have a truncating counterpart
    public static Decimal64 valueOf(final double doubleVal, final RoundingMode rounding) {
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
        return offset + 1;
    }

    /**
     * Return the unscaled value of this decimal.
     *
     * @return This decimal's unscaled value
     */
    public final long unscaledValue() {
        return value;
    }

    /**
     * Return this decimal in the specified scale.
     *
     * @param scale target scale
     * @return Scaled number
     * @throws ArithmeticException if the conversion would overflow or require rounding
     */
    public Decimal64 scaleTo(final int scale) {
        return scaleTo(scale, RoundingMode.UNNECESSARY);
    }

    /**
     * Return this decimal in the specified scale.
     *
     * @param scale scale
     * @param roundingMode rounding mode
     * @return Scaled number
     * @throws ArithmeticException if the conversion would overflow or require rounding and {@code roundingMode} is
     *                             {@link RoundingMode#UNNECESSARY}.
     * @throws IllegalArgumentException if {@code scale} is not valid
     * @throws NullPointerException if {@code roundingMode} is {@code null}
     */
    public Decimal64 scaleTo(final int scale, final RoundingMode roundingMode) {
        final var mode = requireNonNull(roundingMode);
        final byte scaleOffset = offsetOf(scale);
        final int diff = scaleOffset - offset;
        if (diff == 0) {
            // Same scale, no-op
            return this;
        } else if (value == 0) {
            // Zero is special, as it has the same unscaled value in all scales
            return new Decimal64(scaleOffset, 0);
        }

        if (diff > 0) {
            // Increasing scale is simple, as we have pre-calculated min/max boundaries and then it's just
            // factor multiplication
            final int diffOffset = diff - 1;
            final var conv = CONVERSION[diffOffset];
            if (value < conv.minLong || value > conv.maxLong) {
                throw new ArithmeticException("Increasing scale of " + this + " to " + scale + " would overflow");
            }
            return new Decimal64(scaleOffset, value * FACTOR[diffOffset]);
        }

        // Decreasing scale is hard, as we need to deal with rounding
        final int diffOffset = -diff - 1;
        final long factor = FACTOR[diffOffset];
        final long trunc = value / factor;
        final long remainder = value - trunc * factor;

        // No remainder, we do not need to involve rounding
        if (remainder == 0) {
            return new Decimal64(scaleOffset, trunc);
        }

        final long increment = switch (mode) {
            case UP -> Long.signum(trunc);
            case DOWN -> 0;
            case CEILING -> Long.signum(trunc) > 0 ? 1 : 0;
            case FLOOR -> Long.signum(trunc) < 0 ? -1 : 0;
            case HALF_UP -> switch (RemainderSignificance.of(remainder, factor)) {
                case LT_HALF -> 0;
                case HALF, GT_HALF -> Long.signum(trunc);
            };
            case HALF_DOWN -> switch (RemainderSignificance.of(remainder, factor)) {
                case LT_HALF, HALF -> 0;
                case GT_HALF -> Long.signum(trunc);
            };
            case HALF_EVEN -> switch (RemainderSignificance.of(remainder, factor)) {
                case LT_HALF -> 0;
                case HALF -> (trunc & 0x1) != 0 ? Long.signum(trunc) : 0;
                case GT_HALF -> Long.signum(trunc);
            };
            case UNNECESSARY ->
                throw new ArithmeticException("Decreasing scale of " + this + " to " + scale + " requires rounding");
        };

        return new Decimal64(scaleOffset, trunc + increment);
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
        return 1.0 * value / FACTOR[offset];
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
        if (offset == o.offset) {
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

        // Pad unscaled value to scale + 1 size string starting after optional '-' sign
        final var builder = new StringBuilder(21).append(value);
        final int start = value < 0 ? 1 : 0;
        final int scale = scale();
        final int padding = scale + 1 + start - builder.length();
        if (padding > 0) {
            builder.insert(start, "0".repeat(padding));
        }

        // The first digit of the fraction part is now 'scale' from the end. We will insert the decimal point there,
        // but also we it is the digit we never trim.
        final int length = builder.length();
        final int firstDecimal = length - scale;

        // Remove trailing '0's from decimal part. We walk backwards from the last character stop at firstDecimal
        int significantLength = length;
        for (int i = length - 1; i > firstDecimal && builder.charAt(i) == '0'; --i) {
            significantLength = i;
        }
        if (significantLength != length) {
            builder.setLength(significantLength);
        }

        // Insert '.' before the first decimal and we're done
        return builder.insert(firstDecimal, '.').toString();
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
        return this == obj || obj instanceof Decimal64 other && equalsImpl(other);
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
        return offset == other.offset ? value == other.value
                // We need to normalize both
                : intPart() == other.intPart() && fracPart() == other.fracPart();
    }

    private long intPart() {
        return value / FACTOR[offset];
    }

    private long fracPart() {
        return value % FACTOR[offset];
    }

    private static byte offsetOf(final int scale) {
        checkArgument(scale >= 1 && scale <= MAX_SCALE, "Scale %s is not in range [1..%s]", scale, MAX_SCALE);
        return (byte) (scale - 1);
    }
}
