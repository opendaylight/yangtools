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
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Either;
import org.opendaylight.yangtools.data.ScalarValue;

/**
 * Dedicated type for YANG's {@code type uint16} type.
 */
@NonNullByDefault
public non-sealed class Uint16 extends Number implements CanonicalValue<Uint16>, ScalarValue {
    public static final class Support extends AbstractCanonicalValueSupport<Uint16> {
        public Support() {
            super(Uint16.class);
        }

        @Override
        public Either<Uint16, CanonicalValueViolation> fromString(final String str) {
            try {
                return Either.ofFirst(Uint16.valueOf(str));
            } catch (IllegalArgumentException e) {
                return CanonicalValueViolation.variantOf(e);
            }
        }
    }

    private static final CanonicalValueSupport<Uint16> SUPPORT = new Support();
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    private static final int MAX_VALUE_INT = 65535;
    private static final String MAX_VALUE_STR = "65535";

    private static final String CACHE_SIZE_PROPERTY = "org.opendaylight.yangtools.yang.common.Uint16.cache.size";
    private static final int DEFAULT_CACHE_SIZE = 256;

    /**
     * Tunable cache for values. By default it holds {@value #DEFAULT_CACHE_SIZE} entries. This can be
     * changed via {@value #CACHE_SIZE_PROPERTY} system property.
     */
    private static final int CACHE_SIZE;

    static {
        final int p = Integer.getInteger(CACHE_SIZE_PROPERTY, DEFAULT_CACHE_SIZE);
        CACHE_SIZE = p >= 0 ? Math.min(p, MAX_VALUE_INT + 1) : DEFAULT_CACHE_SIZE;
    }

    private static final @NonNull Uint16[] CACHE;

    static {
        final Uint16[] c = new Uint16[CACHE_SIZE];
        for (int i = 0; i < c.length; ++i) {
            c[i] = new Uint16((short) i);
        }
        CACHE = c;
    }

    private static final Interner<Uint16> INTERNER = Interners.newWeakInterner();

    /**
     * Value of {@code 0}.
     */
    public static final Uint16 ZERO = valueOf(0).intern();
    /**
     * Value of {@code 1}.
     */
    public static final Uint16 ONE = valueOf(1).intern();
    /**
     * Value of {@code 2}.
     */
    public static final Uint16 TWO = valueOf(2).intern();
    /**
     * Value of {@code 10}.
     */
    public static final Uint16 TEN = valueOf(10).intern();
    /**
     * Value of {@code 65535}.
     */
    public static final Uint16 MAX_VALUE = valueOf(MAX_VALUE_INT).intern();

    private final short value;

    private Uint16(final short value) {
        this.value = value;
    }

    protected Uint16(final Uint16 other) {
        this(other.value);
    }

    private static Uint16 instanceFor(final short value) {
        final int slot = Short.toUnsignedInt(value);
        return slot < CACHE.length ? CACHE[slot] : new Uint16(value);
    }

    /**
     * Returns an {@code Uint16} corresponding to a given bit representation. The argument is interpreted as an
     * unsigned 16-bit value.
     *
     * @param bits unsigned bit representation
     * @return A Uint16 instance
     */
    public static Uint16 fromShortBits(final short bits) {
        return instanceFor(bits);
    }

    /**
     * Returns an {@code Uint16} corresponding to a given {@code byteVal}. The inverse operation is
     * {@link #byteValue()}.
     *
     * @param byteVal byte value
     * @return A Uint16 instance
     * @throws IllegalArgumentException if byteVal is less than zero
     */
    public static Uint16 valueOf(final byte byteVal) {
        UintConversions.checkNonNegative(byteVal, MAX_VALUE_STR);
        return instanceFor(byteVal);
    }

    /**
     * Returns an {@code Uint16} corresponding to a given {@code shortVal}. The inverse operation is
     * {@link #shortValue()}.
     *
     * @param shortVal short value
     * @return A Uint16 instance
     * @throws IllegalArgumentException if shortVal is less than zero.
     */
    public static Uint16 valueOf(final short shortVal) {
        UintConversions.checkNonNegative(shortVal, MAX_VALUE_STR);
        return instanceFor(shortVal);
    }

    /**
     * Returns an {@code Uint16} corresponding to a given {@code intVal}. The inverse operation is {@link #intValue()}.
     *
     * @param intVal int value
     * @return A Uint16 instance
     * @throws IllegalArgumentException if intVal is less than zero or greater than 65535.
     */
    public static Uint16 valueOf(final int intVal) {
        UintConversions.checkRange(intVal, MAX_VALUE_INT);
        return instanceFor((short)intVal);
    }

    /**
     * Returns an {@code Uint16} corresponding to a given {@code longVal}. The inverse operation is
     * {@link #longValue()}.
     *
     * @param longVal long value
     * @return A Uint16 instance
     * @throws IllegalArgumentException if intVal is less than zero or greater than 65535.
     */
    public static Uint16 valueOf(final long longVal) {
        UintConversions.checkRange(longVal, MAX_VALUE_INT);
        return instanceFor((short)longVal);
    }

    /**
     * Returns an {@code Uint16} corresponding to a given {@code uint}.
     *
     * @param uint Uint8 value
     * @return A Uint16 instance
     * @throws NullPointerException if uint is null
     */
    public static Uint16 valueOf(final Uint8 uint) {
        return instanceFor(uint.shortValue());
    }

    /**
     * Returns an {@code Uint8} corresponding to a given {@code uint}.
     *
     * @param uint Uint32 value
     * @return A Uint16 instance
     * @throws NullPointerException if uint is null
     * @throws IllegalArgumentException if uint is greater than 65535.
     */
    public static Uint16 valueOf(final Uint32 uint) {
        return valueOf(uint.longValue());
    }

    /**
     * Returns an {@code Uint8} corresponding to a given {@code uint}.
     *
     * @param uint Uint64 value
     * @return A Uint16 instance
     * @throws NullPointerException if uint is null
     * @throws IllegalArgumentException if uint is greater than 65535.
     */
    public static Uint16 valueOf(final Uint64 uint) {
        return valueOf(uint.longValue());
    }

    /**
     * Returns an {@code Uint16} holding the value of the specified {@code String}, parsed as an unsigned {@code int}
     * value.
     *
     * @param string String to parse
     * @return A Uint16 instance
     * @throws NullPointerException if string is null
     * @throws IllegalArgumentException if the parsed value is less than zero or greater than 65535
     * @throws NumberFormatException if the string does not contain a parsable unsigned {@code int} value.
     */
    public static Uint16 valueOf(final String string) {
        return valueOf(string, 10);
    }

    /**
     * Returns an {@code Uint16} holding the value of the specified {@code String}, parsed as an unsigned {@code int}
     * value.
     *
     * @param string String to parse
     * @param radix Radix to use
     * @return A Uint16 instance
     * @throws NullPointerException if string is null
     * @throws IllegalArgumentException if the parsed value is less than zero or greater than 65535
     * @throws NumberFormatException if the string does not contain a parsable unsigned {@code int} value, or if the
     *                               {@code radix} is outside of allowed range.
     */
    public static Uint16 valueOf(final String string, final int radix) {
        return valueOf(Integer.parseInt(requireNonNull(string), radix));
    }

    /**
     * Returns an {@code Uint16} corresponding to a given {@code byteVal} if it is representable. If the value is
     * negative {@link #ZERO} will be returned.
     *
     * @param byteVal byte value
     * @return A Uint16 instance
     */
    public static Uint16 saturatedOf(final byte byteVal) {
        return byteVal <= 0 ? Uint16.ZERO : instanceFor(byteVal);
    }

    /**
     * Returns an {@code Uint16} corresponding to a given {@code shortVal} if it is representable. If the value is
     * negative {@link #ZERO} will be returned.
     *
     * @param shortVal short value
     * @return A Uint16 instance
     */
    public static Uint16 saturatedOf(final short shortVal) {
        return shortVal <= 0 ? Uint16.ZERO : instanceFor(shortVal);
    }

    /**
     * Returns an {@code Uint16} corresponding to a given {@code intVal} if it is representable. If the value is
     * negative {@link #ZERO} will be returned. If the value is greater than 65535, {@link #MAX_VALUE} will be returned.
     *
     * @param intVal int value
     * @return A Uint16 instance
     */
    public static Uint16 saturatedOf(final int intVal) {
        if (intVal <= 0) {
            return Uint16.ZERO;
        }
        if (intVal >= MAX_VALUE_INT) {
            return Uint16.MAX_VALUE;
        }
        return instanceFor((short) intVal);
    }

    /**
     * Returns an {@code Uint16} corresponding to a given {@code longVal} if it is representable. If the value is
     * negative {@link #ZERO} will be returned. If the value is greater than 65535, {@link #MAX_VALUE} will be returned.
     *
     * @param longVal long value
     * @return A Uint16 instance
     */
    public static Uint16 saturatedOf(final long longVal) {
        if (longVal <= 0) {
            return Uint16.ZERO;
        }
        if (longVal >= MAX_VALUE_INT) {
            return Uint16.MAX_VALUE;
        }
        return instanceFor((short) longVal);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The inverse operation is {@link #fromShortBits(short)}. In case this value is greater than
     * {@link Short#MAX_VALUE}, the returned value will be equal to {@code this - 2^16}.
     */
    @Override
    public final short shortValue() {
        return value;
    }

    @Override
    public final int intValue() {
        return Short.toUnsignedInt(value);
    }

    @Override
    public final long longValue() {
        return Short.toUnsignedLong(value);
    }

    @Override
    public final float floatValue() {
        return intValue();
    }

    @Override
    public final double doubleValue() {
        return intValue();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public final int compareTo(final Uint16 o) {
        return Short.compareUnsigned(value, o.value);
    }

    @Override
    public final String toCanonicalString() {
        return Integer.toString(intValue());
    }

    @Override
    public final CanonicalValueSupport<Uint16> support() {
        return SUPPORT;
    }

    /**
     * Return an interned (shared) instance equivalent to this object. This may return the same object.
     *
     * @return A shared instance.
     */
    public final Uint16 intern() {
        return intValue() < CACHE_SIZE ? this : INTERNER.intern(this);
    }

    /**
     * Convert this value to an {@code int}.
     *
     * @return An int
     */
    public final int toJava() {
        return intValue();
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
     * Convert this value to a {@code Uint32}.
     *
     * @return A Uint32
     */
    public final Uint32 toUint32() {
        return Uint32.fromIntBits(intValue());
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

    @Override
    public final int hashCode() {
        return Short.hashCode(value);
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof Uint16 other && value == other.value;
    }

    /**
     * A slightly faster version of {@link #equals(Object)}.
     *
     * @param obj Uint16 object
     * @return  {@code true} if this object is the same as the obj argument; {@code false} otherwise.
     */
    public final boolean equals(final @Nullable Uint16 obj) {
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
        return new U2v1(value);
    }
}
