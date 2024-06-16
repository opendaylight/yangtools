/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.google.common.primitives.UnsignedLong;
import java.math.BigInteger;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Either;

/**
 * Dedicated type for YANG's {@code type uint64} type.
 */
@NonNullByDefault
public class Uint64 extends Number implements CanonicalValue<Uint64> {
    public static final class Support extends AbstractCanonicalValueSupport<Uint64> {
        public Support() {
            super(Uint64.class);
        }

        @Override
        public Either<Uint64, CanonicalValueViolation> fromString(final String str) {
            try {
                return Either.ofFirst(Uint64.valueOf(str));
            } catch (IllegalArgumentException e) {
                return CanonicalValueViolation.variantOf(e);
            }
        }
    }

    private static final CanonicalValueSupport<Uint64> SUPPORT = new Support();
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    private static final String MAX_VALUE_STR = Long.toUnsignedString(-1);

    private static final String CACHE_SIZE_PROPERTY = "org.opendaylight.yangtools.yang.common.Uint64.cache.size";
    private static final int DEFAULT_CACHE_SIZE = 256;

    /**
     * Tunable cache for values. By default it holds {@value #DEFAULT_CACHE_SIZE} entries. This can be
     * changed via {@value #CACHE_SIZE_PROPERTY} system property.
     */
    private static final long CACHE_SIZE;

    static {
        final int p = Integer.getInteger(CACHE_SIZE_PROPERTY, DEFAULT_CACHE_SIZE);
        CACHE_SIZE = p >= 0 ? Math.min(p, Integer.MAX_VALUE) : DEFAULT_CACHE_SIZE;
    }

    private static final @NonNull Uint64[] CACHE;

    static {
        final Uint64[] c = new Uint64[(int) CACHE_SIZE];
        for (int i = 0; i < c.length; ++i) {
            c[i] = new Uint64(i);
        }
        CACHE = c;
    }

    private static final Interner<Uint64> INTERNER = Interners.newWeakInterner();

    /**
     * Value of {@code 0}.
     */
    public static final Uint64 ZERO = valueOf(0).intern();
    /**
     * Value of {@code 1}.
     */
    public static final Uint64 ONE = valueOf(1).intern();
    /**
     * Value of {@code 2}.
     */
    public static final Uint64 TWO = valueOf(2).intern();
    /**
     * Value of {@code 10}.
     */
    public static final Uint64 TEN = valueOf(10).intern();
    /**
     * Value of {@code 18446744073709551615}.
     */
    public static final Uint64 MAX_VALUE = fromLongBits(-1).intern();

    private final long value;

    private Uint64(final long value) {
        this.value = value;
    }

    protected Uint64(final Uint64 other) {
        this(other.value);
    }

    private static Uint64 instanceFor(final long value) {
        return value >= 0 && value < CACHE.length ? CACHE[(int) value] : new Uint64(value);
    }

    /**
     * Returns an {@code Uint64} corresponding to a given bit representation. The argument is interpreted as an
     * unsigned 64-bit value.
     *
     * @param bits unsigned bit representation
     * @return A Uint64 instance
     */
    public static Uint64 fromLongBits(final long bits) {
        return instanceFor(bits);
    }

    /**
     * Returns an {@code Uint64} corresponding to a given {@code byteVal}. The inverse operation is
     * {@link #byteValue()}.
     *
     * @param byteVal byte value
     * @return A Uint64 instance
     * @throws IllegalArgumentException if byteVal is less than zero
     */
    public static Uint64 valueOf(final byte byteVal) {
        UintConversions.checkNonNegative(byteVal, MAX_VALUE_STR);
        return instanceFor(byteVal);
    }

    /**
     * Returns an {@code Uint64} corresponding to a given {@code shortVal}. The inverse operation is
     * {@link #shortValue()}.
     *
     * @param shortVal short value
     * @return A Uint64 instance
     * @throws IllegalArgumentException if shortVal is less than zero
     */
    public static Uint64 valueOf(final short shortVal) {
        UintConversions.checkNonNegative(shortVal, MAX_VALUE_STR);
        return instanceFor(shortVal);
    }

    /**
     * Returns an {@code Uint64} corresponding to a given {@code intVal}. The inverse operation is {@link #intValue()}.
     *
     * @param intVal int value
     * @return A Uint64 instance
     * @throws IllegalArgumentException if intVal is less than zero
     */
    public static Uint64 valueOf(final int intVal) {
        UintConversions.checkNonNegative(intVal, MAX_VALUE_STR);
        return instanceFor(intVal);
    }

    /**
     * Returns an {@code Uint64} corresponding to a given {@code longVal}, which is checked for range.
     * See also {@link #longValue()} and {@link #fromLongBits(long)}.
     *
     * @param longVal long value
     * @return A Uint8 instance
     * @throws IllegalArgumentException if longVal is less than zero
     */
    public static Uint64 valueOf(final long longVal) {
        if (longVal >= 0) {
            return instanceFor(longVal);
        }
        throw new IllegalArgumentException("Invalid range: " + longVal + ", expected: [[0..18446744073709551615]].");
    }

    /**
     * Returns an {@code Uint64} corresponding to a given {@code uint}.
     *
     * @param uint Uint8 value
     * @return A Uint64 instance
     * @throws NullPointerException if uint is null
     */
    public static Uint64 valueOf(final Uint8 uint) {
        return instanceFor(uint.shortValue());
    }

    /**
     * Returns an {@code Uint64} corresponding to a given {@code uint}.
     *
     * @param uint Uint16 value
     * @return A Uint64 instance
     * @throws NullPointerException if uint is null
     */
    public static Uint64 valueOf(final Uint16 uint) {
        return instanceFor(uint.intValue());
    }

    /**
     * Returns an {@code Uint64} corresponding to a given {@code uint}.
     *
     * @param uint Uint32 value
     * @return A Uint64 instance
     * @throws NullPointerException if uint is null
     */
    public static Uint64 valueOf(final Uint32 uint) {
        return instanceFor(uint.longValue());
    }

    /**
     * Returns an {@code Uint64} corresponding to a given {@code ulong}.
     *
     * @param ulong UnsignedLong value
     * @return A Uint64 instance
     * @throws NullPointerException if ulong is null
     */
    public static Uint64 valueOf(final UnsignedLong ulong) {
        return instanceFor(ulong.longValue());
    }

    /**
     * Returns an {@code Uint64} corresponding to a given {@code bigInt}.
     *
     * @param bigInt BigInteger value
     * @return A Uint64 instance
     * @throws NullPointerException if bigInt is null
     * @throws IllegalArgumentException if bigInt is less than zero or greater than 18446744073709551615
     */
    public static Uint64 valueOf(final BigInteger bigInt) {
        if (bigInt.signum() >= 0 && bigInt.bitLength() <= Long.SIZE) {
            return instanceFor(bigInt.longValue());
        }
        throw new IllegalArgumentException("Invalid range: " + bigInt + ", expected: [[0..18446744073709551615]].");
    }

    /**
     * Returns an {@code Uint32} holding the value of the specified {@code String}, parsed as an unsigned {@code long}
     * value.
     *
     * @param string String to parse
     * @return A Uint64 instance
     * @throws NullPointerException if string is null
     * @throws IllegalArgumentException if the parsed value is less than zero or greater than 18446744073709551615
     * @throws NumberFormatException if the string does not contain a parsable unsigned {@code long} value.
     */
    public static Uint64 valueOf(final String string) {
        return valueOf(string, 10);
    }

    /**
     * Returns an {@code Uint64} holding the value of the specified {@code String}, parsed as an unsigned {@code long}
     * value.
     *
     * @param string String to parse
     * @param radix Radix to use
     * @return A Uint64 instance
     * @throws NullPointerException if string is null
     * @throws IllegalArgumentException if the parsed value is less than zero or greater than 18446744073709551615
     * @throws NumberFormatException if the string does not contain a parsable unsigned {@code long} value, or if the
     *                               {@code radix} is outside of allowed range.
     */
    public static Uint64 valueOf(final String string, final int radix) {
        return instanceFor(Long.parseUnsignedLong(requireNonNull(string), radix));
    }

    /**
     * Returns an {@code Uint64} corresponding to a given {@code byteVal} if it is representable. If the value is
     * negative {@link #ZERO} will be returned.
     *
     * @param byteVal byte value
     * @return A Uint64 instance
     */
    public static Uint64 saturatedOf(final byte byteVal) {
        return byteVal <= 0 ? Uint64.ZERO : instanceFor(byteVal);
    }

    /**
     * Returns an {@code Uint32} corresponding to a given {@code shortVal} if it is representable. If the value is
     * negative {@link #ZERO} will be returned.
     *
     * @param shortVal short value
     * @return A Uint32 instance
     */
    public static Uint64 saturatedOf(final short shortVal) {
        return shortVal <= 0 ? Uint64.ZERO : instanceFor(shortVal);
    }

    /**
     * Returns an {@code Uint64} corresponding to a given {@code intVal} if it is representable. If the value is
     * negative {@link #ZERO} will be returned.
     *
     * @param intVal int value
     * @return A Uint64 instance
     */
    public static Uint64 saturatedOf(final int intVal) {
        return intVal <= 0 ? Uint64.ZERO : instanceFor(intVal);
    }

    /**
     * Returns an {@code Uint64} corresponding to a given {@code longVal} if it is representable. If the value is
     * negative {@link #ZERO} will be returned.
     *
     * @param longVal long value
     * @return A Uint64 instance
     */
    public static Uint64 saturatedOf(final long longVal) {
        return longVal <= 0 ? Uint64.ZERO : instanceFor(longVal);
    }

    /**
     * Returns an {@code Uint64} corresponding to a given {@code longVal} if it is representable. If the value is
     * negative {@link #ZERO} will be returned. If the value is greater than 18446744073709551615, {@link #MAX_VALUE}
     * will be returned.
     *
     * @param bigInt BigInteger value
     * @return A Uint64 instance
     * @throws NullPointerException if bigInt is null
     */
    public static Uint64 saturatedOf(final BigInteger bigInt) {
        if (bigInt.signum() <= 0) {
            return Uint64.ZERO;
        }
        return bigInt.bitLength() > Long.SIZE ? Uint64.MAX_VALUE : instanceFor(bigInt.longValue());
    }

    @Override
    public final int intValue() {
        return (int)value;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The inverse operation is {@link #fromLongBits(long)}. In case this value is greater than
     * {@link Long#MAX_VALUE}, the returned value will be equal to {@code this - 2^64}.
     */
    @Override
    public final long longValue() {
        return value;
    }

    @Override
    public final float floatValue() {
        // TODO: ditch Guava
        return UnsignedLong.fromLongBits(value).floatValue();
    }

    @Override
    public final double doubleValue() {
        // TODO: ditch Guava
        return UnsignedLong.fromLongBits(value).doubleValue();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public final int compareTo(final Uint64 o) {
        return Long.compareUnsigned(value, o.value);
    }

    @Override
    public final String toCanonicalString() {
        return Long.toUnsignedString(value);
    }

    @Override
    public final CanonicalValueSupport<Uint64> support() {
        return SUPPORT;
    }

    /**
     * Return an interned (shared) instance equivalent to this object. This may return the same object.
     *
     * @return A shared instance.
     */
    public final Uint64 intern() {
        return value >= 0 && value < CACHE_SIZE ? this : INTERNER.intern(this);
    }

    /**
     * Convert this value to a {@link BigInteger}.
     *
     * @return A BigInteger instance
     */
    public final BigInteger toJava() {
        // FIXME: ditch the Guava transition
        return toGuava().bigIntegerValue();
    }

    /**
     * Convert this value to an {@link UnsignedLong}.
     *
     * @return An UnsignedLong instance
     */
    public final UnsignedLong toGuava() {
        return UnsignedLong.fromLongBits(value);
    }

    /**
     * Convert this value to a {@code Uint8}.
     *
     * @return A Uint8
     * @throws IllegalArgumentException if this value is greater than 255.
     */
    public final Uint8 toUint8() {
        if ((value & 0xFFFFFFFFFFFFFF00L) != 0) {
            throw iae(toString(), 255);
        }
        return Uint8.fromByteBits((byte) value);
    }

    /**
     * Convert this value to a {@code Uint16}.
     *
     * @return A Uint16
     * @throws IllegalArgumentException if this value is greater than 65535.
     */
    public final Uint16 toUint16() {
        if ((value & 0xFFFFFFFFFFFF0000L) != 0) {
            throw iae(toString(), 65535);
        }
        return Uint16.fromShortBits((short) value);
    }

    /**
     * Convert this value to a {@code Uint64}.
     *
     * @return A Uint32
     * @throws IllegalArgumentException if this value is greater than 4294967295.
     */
    public final Uint32 toUint32() {
        if ((value & 0xFFFFFFFF00000000L) != 0) {
            throw iae(toString(), 4294967295L);
        }
        return Uint32.fromIntBits((int) value);
    }

    static IllegalArgumentException iae(final String value, final long max) {
        // "Invalid range: 65536, expected: [[0..65535]]."
        return new IllegalArgumentException("Invalid range: " + value + ", expected: [[0.." + max + "]].");
    }

    @Override
    public final int hashCode() {
        return Long.hashCode(value);
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof Uint64 other && value == other.value;
    }

    /**
     * A slightly faster version of {@link #equals(Object)}.
     *
     * @param obj Uint64 object
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
     */
    public final boolean equals(final @Nullable Uint64 obj) {
        return this == obj || obj != null && value == obj.value;
    }

    @Override
    public final String toString() {
        return toCanonicalString();
    }

    @java.io.Serial
    private Object readResolve() {
        return instanceFor(value);
    }

    @java.io.Serial
    private Object writeReplace() {
        return new U8v1(value);
    }
}
