/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.Beta;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.google.common.primitives.UnsignedInteger;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.concepts.Variant;

/**
 * Dedicated type for YANG's 'type uint32' type.
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public class Uint32 extends Number implements CanonicalValue<Uint32> {
    @MetaInfServices(value = CanonicalValueSupport.class)
    public static final class Support extends AbstractCanonicalValueSupport<Uint32> {
        public Support() {
            super(Uint32.class);
        }

        @Override
        public Variant<Uint32, CanonicalValueViolation> fromString(final String str) {
            try {
                return Variant.ofFirst(Uint32.valueOf(str));
            } catch (IllegalArgumentException e) {
                return CanonicalValueViolation.variantOf(e);
            }
        }
    }

    private static final CanonicalValueSupport<Uint32> SUPPORT = new Support();
    private static final long serialVersionUID = 1L;
    private static final long MIN_VALUE_LONG = 0;
    private static final long MAX_VALUE_LONG = 0xffffffffL;

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
        for (int i = 0; i <= c.length; ++i) {
            c[i] = new Uint32(i);
        }
        CACHE = c;
    }

    private static final Interner<Uint32> INTERNER = Interners.newWeakInterner();

    public static final Uint32 ZERO = valueOf(0).intern();
    public static final Uint32 ONE = valueOf(1).intern();
    public static final Uint32 MAX_VALUE = valueOf(MAX_VALUE_LONG).intern();

    private final int value;

    Uint32(final int value) {
        this.value = value;
    }

    protected Uint32(final Uint32 other) {
        this.value = other.value;
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
     * Returns an {@code Uint32} corresponding to a given {@code byteVal}. The inverse operation is {@link #byteValue()}.
     *
     * @param byteVal byte value
     * @return A Uint32 instance
     * @throws IllegalArgumentException if byteVal is less than zero
     */
    public static Uint32 valueOf(final byte byteVal) {
        checkArgument(byteVal >= MIN_VALUE_LONG, "Negative values are not allowed");
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
        checkArgument(shortVal >= MIN_VALUE_LONG, "Negative values are not allowed");
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
        checkArgument(intVal >= MIN_VALUE_LONG, "Negative values are not allowed");
        return instanceFor(intVal);
    }

    /**
     * Returns an {@code Uint32} corresponding to a given {@code longVal}. The inverse operation is
     * {@link #longValue()}.
     *
     * @param longVal long value
     * @return A Uint8 instance
     * @throws IllegalArgumentException if intVal is less than zero or greater than 4294967295
     */
    public static Uint32 valueOf(final long longVal) {
        checkArgument(longVal >= MIN_VALUE_LONG && longVal <= MAX_VALUE_LONG, "Value %s is outside of allowed range",
                longVal);
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
        return instanceFor(Integer.parseUnsignedInt(string, radix));
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The inverse operation is {@link #fromIntBits(int)}. In case this value is greater than {@link Integer#MAX_VALUE},
     * the returned value will be equal to {@code this - 2^32}.
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

    /**
     * Convert this value to an {@link UnsignedInteger}.
     *
     * @return An UnsignedInteger instance
     */
    public final UnsignedInteger toUnsignedInteger() {
        return UnsignedInteger.fromIntBits(value);
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

    @Override
    public final int hashCode() {
        return Integer.hashCode(value);
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof Uint32 && value == ((Uint32)obj).value;
    }

    @Override
    public final String toString() {
        return toCanonicalString();
    }

    private Object readResolve() {
        return instanceFor(value);
    }
}
