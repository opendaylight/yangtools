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
import com.google.common.primitives.UnsignedInteger;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Either;
import org.opendaylight.yangtools.data.ScalarValue;

/**
 * Dedicated type for YANG's {@code type uint32} type.
 */
@NonNullByDefault
public non-sealed class Uint32 extends Number implements CanonicalValue<Uint32>, ScalarValue {
    public static final class Support extends AbstractCanonicalValueSupport<Uint32> {
        public Support() {
            super(Uint32.class);
        }

        @Override
        public Either<Uint32, CanonicalValueViolation> fromString(final String str) {
            try {
                return Either.ofFirst(Uint32.valueOf(str));
            } catch (IllegalArgumentException e) {
                return CanonicalValueViolation.variantOf(e);
            }
        }
    }

    private static final CanonicalValueSupport<Uint32> SUPPORT = new Support();
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    private static final long MAX_VALUE_LONG = 4294967295L;
    private static final String MAX_VALUE_STR = "4294967295";

    private static final String CACHE_SIZE_PROPERTY = "org.opendaylight.yangtools.yang.common.Uint32.cache.size";
    private static final int DEFAULT_CACHE_SIZE = 256;

    /**
     * Tunable cache for values. By default it holds {@value #DEFAULT_CACHE_SIZE} entries. This can be
     * changed via {@value #CACHE_SIZE_PROPERTY} system property.
     */
    private static final int CACHE_SIZE;

    static {
        final int p = Integer.getInteger(CACHE_SIZE_PROPERTY, DEFAULT_CACHE_SIZE);
        CACHE_SIZE = p >= 0 ? Math.min(p, Integer.MAX_VALUE) : DEFAULT_CACHE_SIZE;
    }

    private static final @NonNull Uint32[] CACHE;

    static {
        final Uint32[] c = new Uint32[CACHE_SIZE];
        for (int i = 0; i < c.length; ++i) {
            c[i] = new Uint32(i);
        }
        CACHE = c;
    }

    private static final Interner<Uint32> INTERNER = Interners.newWeakInterner();

    /**
     * Value of {@code 0}.
     */
    public static final Uint32 ZERO = valueOf(0).intern();
    /**
     * Value of {@code 1}.
     */
    public static final Uint32 ONE = valueOf(1).intern();
    /**
     * Value of {@code 2}.
     */
    public static final Uint32 TWO = valueOf(2).intern();
    /**
     * Value of {@code 10}.
     */
    public static final Uint32 TEN = valueOf(10).intern();
    /**
     * Value of {@code 4294967295}.
     */
    public static final Uint32 MAX_VALUE = valueOf(MAX_VALUE_LONG).intern();

    private final int value;

    private Uint32(final int value) {
        this.value = value;
    }

    protected Uint32(final Uint32 other) {
        this(other.value);
    }

    private static Uint32 instanceFor(final int value) {
        final long longSlot = Integer.toUnsignedLong(value);
        return longSlot < CACHE.length ? CACHE[(int)longSlot] : new Uint32(value);
    }

    /**
     * Returns an {@code Uint32} corresponding to a given bit representation. The argument is interpreted as an
     * unsigned 32-bit value.
     *
     * @param bits unsigned bit representation
     * @return A Uint32 instance
     */
    public static Uint32 fromIntBits(final int bits) {
        return instanceFor(bits);
    }

    /**
     * Returns an {@code Uint32} corresponding to a given {@code byteVal}. The inverse operation is
     * {@link #byteValue()}.
     *
     * @param byteVal byte value
     * @return A Uint32 instance
     * @throws IllegalArgumentException if byteVal is less than zero
     */
    public static Uint32 valueOf(final byte byteVal) {
        UintConversions.checkNonNegative(byteVal, MAX_VALUE_STR);
        return instanceFor(byteVal);
    }

    /**
     * Returns an {@code Uint32} corresponding to a given {@code shortVal}. The inverse operation is
     * {@link #shortValue()}.
     *
     * @param shortVal short value
     * @return A Uint32 instance
     * @throws IllegalArgumentException if shortVal is less than zero
     */
    public static Uint32 valueOf(final short shortVal) {
        UintConversions.checkNonNegative(shortVal, MAX_VALUE_STR);
        return instanceFor(shortVal);
    }

    /**
     * Returns an {@code Uint32} corresponding to a given {@code intVal}. The inverse operation is {@link #intValue()}.
     *
     * @param intVal int value
     * @return A Uint32 instance
     * @throws IllegalArgumentException if intVal is less than zero
     */
    public static Uint32 valueOf(final int intVal) {
        UintConversions.checkNonNegative(intVal, MAX_VALUE_STR);
        return instanceFor(intVal);
    }

    /**
     * Returns an {@code Uint32} corresponding to a given {@code longVal}. The inverse operation is
     * {@link #longValue()}.
     *
     * @param longVal long value
     * @return A Uint8 instance
     * @throws IllegalArgumentException if longVal is less than zero or greater than 4294967295
     */
    public static Uint32 valueOf(final long longVal) {
        UintConversions.checkRange(longVal, MAX_VALUE_LONG);
        return instanceFor((int)longVal);
    }

    /**
     * Returns an {@code Uint32} corresponding to a given {@code uint}.
     *
     * @param uint Uint8 value
     * @return A Uint32 instance
     * @throws NullPointerException if uint is null
     */
    public static Uint32 valueOf(final Uint8 uint) {
        return instanceFor(uint.shortValue());
    }

    /**
     * Returns an {@code Uint32} corresponding to a given {@code uint}.
     *
     * @param uint Uint16 value
     * @return A Uint32 instance
     * @throws NullPointerException if uint is null
     */
    public static Uint32 valueOf(final Uint16 uint) {
        return instanceFor(uint.intValue());
    }

    /**
     * Returns an {@code Uint32} corresponding to a given {@code uint}.
     *
     * @param uint Uint64 value
     * @return A Uint32 instance
     * @throws NullPointerException if uint is null
     * @throws IllegalArgumentException if uint is greater than 4294967295
     */
    public static Uint32 valueOf(final Uint64 uint) {
        return valueOf(uint.longValue());
    }

    /**
     * Returns an {@code Uint32} corresponding to a given {@code uint}.
     *
     * @param uint UnsignedInteger value
     * @return A Uint32 instance
     * @throws NullPointerException if uint is null
     */
    public static Uint32 valueOf(final UnsignedInteger uint) {
        return instanceFor(uint.intValue());
    }

    /**
     * Returns an {@code Uint32} holding the value of the specified {@code String}, parsed as an unsigned {@code long}
     * value.
     *
     * @param string String to parse
     * @return A Uint32 instance
     * @throws NullPointerException if string is null
     * @throws IllegalArgumentException if the parsed value is less than zero or greater than 4294967295
     * @throws NumberFormatException if the string does not contain a parsable unsigned {@code long} value.
     */
    public static Uint32 valueOf(final String string) {
        return valueOf(string, 10);
    }

    /**
     * Returns an {@code Uint32} holding the value of the specified {@code String}, parsed as an unsigned {@code long}
     * value.
     *
     * @param string String to parse
     * @param radix Radix to use
     * @return A Uint32 instance
     * @throws NullPointerException if string is null
     * @throws IllegalArgumentException if the parsed value is less than zero or greater than 4294967295
     * @throws NumberFormatException if the string does not contain a parsable unsigned {@code long} value, or if the
     *                               {@code radix} is outside of allowed range.
     */
    public static Uint32 valueOf(final String string, final int radix) {
        return instanceFor(Integer.parseUnsignedInt(requireNonNull(string), radix));
    }

    /**
     * Returns an {@code Uint32} corresponding to a given {@code byteVal} if it is representable. If the value is
     * negative {@link #ZERO} will be returned.
     *
     * @param byteVal byte value
     * @return A Uint32 instance
     */
    public static Uint32 saturatedOf(final byte byteVal) {
        return byteVal <= 0 ? Uint32.ZERO : instanceFor(byteVal);
    }

    /**
     * Returns an {@code Uint32} corresponding to a given {@code shortVal} if it is representable. If the value is
     * negative {@link #ZERO} will be returned.
     *
     * @param shortVal short value
     * @return A Uint32 instance
     */
    public static Uint32 saturatedOf(final short shortVal) {
        return shortVal <= 0 ? Uint32.ZERO : instanceFor(shortVal);
    }

    /**
     * Returns an {@code Uint32} corresponding to a given {@code intVal} if it is representable. If the value is
     * negative {@link #ZERO} will be returned.
     *
     * @param intVal int value
     * @return A Uint32 instance
     */
    public static Uint32 saturatedOf(final int intVal) {
        return intVal <= 0 ? Uint32.ZERO : instanceFor(intVal);
    }

    /**
     * Returns an {@code Uint32} corresponding to a given {@code longVal} if it is representable. If the value is
     * negative {@link #ZERO} will be returned. If the value is greater than 4294967295, {@link #MAX_VALUE} will be
     * returned.
     *
     * @param longVal long value
     * @return A Uint32 instance
     */
    public static Uint32 saturatedOf(final long longVal) {
        if (longVal <= 0) {
            return Uint32.ZERO;
        }
        if (longVal >= MAX_VALUE_LONG) {
            return Uint32.MAX_VALUE;
        }
        return instanceFor((int) longVal);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The inverse operation is {@link #fromIntBits(int)}. In case this value is greater than
     * {@link Integer#MAX_VALUE}, the returned value will be equal to {@code this - 2^32}.
     */
    @Override
    public final int intValue() {
        return value;
    }

    @Override
    public final long longValue() {
        return Integer.toUnsignedLong(value);
    }

    @Override
    public final float floatValue() {
        return longValue();
    }

    @Override
    public final double doubleValue() {
        return longValue();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public final int compareTo(final Uint32 o) {
        return Integer.compareUnsigned(value, o.value);
    }

    @Override
    public final String toCanonicalString() {
        return Integer.toUnsignedString(value);
    }

    @Override
    public final CanonicalValueSupport<Uint32> support() {
        return SUPPORT;
    }

    /**
     * Return an interned (shared) instance equivalent to this object. This may return the same object.
     *
     * @return A shared instance.
     */
    public final Uint32 intern() {
        return value >= 0 && value < CACHE_SIZE ? this : INTERNER.intern(this);
    }

    /**
     * Convert this value to a {@code long}.
     *
     * @return A long
     */
    public final long toJava() {
        return longValue();
    }

    /**
     * Convert this value to an {@link UnsignedInteger}.
     *
     * @return An UnsignedInteger instance
     */
    public final UnsignedInteger toGuava() {
        return UnsignedInteger.fromIntBits(value);
    }

    /**
     * Convert this value to a {@code Uint8}.
     *
     * @return A Uint8
     * @throws IllegalArgumentException if this value is greater than 255.
     */
    public final Uint8 toUint8() {
        return Uint8.valueOf(toJava());
    }

    /**
     * Convert this value to a {@code Uint16}.
     *
     * @return A Uint16
     * @throws IllegalArgumentException if this value is greater than 65535.
     */
    public final Uint16 toUint16() {
        return Uint16.valueOf(toJava());
    }

    /**
     * Convert this value to a {@code Uint64}.
     *
     * @return A Uint64
     */
    public final Uint64 toUint64() {
        return Uint64.fromLongBits(longValue());
    }

    public final Uint8 toSaturatedUint8() {
        return Uint8.saturatedOf(toJava());
    }

    public final Uint16 toSaturatedUint16() {
        return Uint16.saturatedOf(toJava());
    }

    // FIXME: more saturated conversions

    @Override
    public final int hashCode() {
        return Integer.hashCode(value);
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof Uint32 other && value == other.value;
    }

    /**
     * A slightly faster version of {@link #equals(Object)}.
     *
     * @param obj Uint32 object
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
     */
    public final boolean equals(final @Nullable Uint32 obj) {
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
        return new U4v1(value);
    }
}
