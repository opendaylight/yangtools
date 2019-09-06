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
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.google.common.primitives.UnsignedLong;
import java.math.BigInteger;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.concepts.Variant;

/**
 * Dedicated type for YANG's 'type uint64' type.
 *
 * @author Robert Varga
 */
/*
 * Note this class is final even though it is a CanonicalValue. As our current progress on using that mechanics
 * is stalled, that should not be a problem. If that effort is restarted, this class may become non-final when needed.
 *
 * Valhalla is a major consideration, as inline types in the current prototype
 * (https://wiki.openjdk.java.net/display/valhalla/LW2) do not allow subclassing. That would mean
 * we'd want to convert to an inline type of value + support, which should still be lower than a full object.
 *
 * Anyway, this needs to be revisited once either one gets closer to being integrated.
 */
@Beta
@NonNullByDefault
public final class Uint64 extends Number implements CanonicalValue<Uint64> {
    @MetaInfServices(value = CanonicalValueSupport.class)
    public static final class Support extends AbstractCanonicalValueSupport<Uint64> {
        public Support() {
            super(Uint64.class);
        }

        @Override
        public Variant<Uint64, CanonicalValueViolation> fromString(final String str) {
            try {
                return Variant.ofFirst(Uint64.valueOf(str));
            } catch (IllegalArgumentException e) {
                return CanonicalValueViolation.variantOf(e);
            }
        }
    }

    private static final CanonicalValueSupport<Uint64> SUPPORT = new Support();
    private static final long serialVersionUID = 1L;
    private static final long MIN_VALUE_LONG = 0;

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

    public static final Uint64 ZERO = valueOf(0).intern();
    public static final Uint64 ONE = valueOf(1).intern();
    public static final Uint64 MAX_VALUE = fromLongBits(-1).intern();

    private final long value;

    private Uint64(final long value) {
        this.value = value;
    }

    @VisibleForTesting
    Uint64(final Uint64 other) {
        this(other.value);
    }

    private static Uint64 instanceFor(final long value) {
        final int slot = (int)value;
        return slot >= 0 && slot < CACHE.length ? CACHE[slot] : new Uint64(value);
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
        checkArgument(byteVal >= MIN_VALUE_LONG, "Negative values are not allowed");
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
        checkArgument(shortVal >= MIN_VALUE_LONG, "Negative values are not allowed");
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
        checkArgument(intVal >= MIN_VALUE_LONG, "Negative values are not allowed");
        return instanceFor(intVal);
    }

    /**
     * Returns an {@code Uint64} corresponding to a given {@code longVal}. The inverse operation is
     * {@link #fromLongBits(long)}.
     *
     * @param longVal long value
     * @return A Uint8 instance
     * @throws IllegalArgumentException if longVal is less than zero
     */
    public static Uint64 valueOf(final long longVal) {
        checkArgument(longVal >= MIN_VALUE_LONG, "Negative values are not allowed");
        return instanceFor(longVal);
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
        checkArgument(bigInt.signum() >= 0, "Negative values not allowed");
        checkArgument(bigInt.bitLength() <= Long.SIZE, "Value %s is outside of allowed range", bigInt);
        return instanceFor(bigInt.longValue());
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
        return instanceFor(Long.parseUnsignedLong(string, radix));
    }

    @Override
    public int intValue() {
        return (int)value;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The inverse operation is {@link #fromLongBits(long)}. In case this value is greater than {@link Long#MAX_VALUE},
     * the returned value will be equal to {@code this - 2^64}.
     */
    @Override
    public long longValue() {
        return value;
    }

    @Override
    public float floatValue() {
        // TODO: ditch Guava
        return UnsignedLong.fromLongBits(value).floatValue();
    }

    @Override
    public double doubleValue() {
        // TODO: ditch Guava
        return UnsignedLong.fromLongBits(value).doubleValue();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public int compareTo(final Uint64 o) {
        return Long.compareUnsigned(value, o.value);
    }

    @Override
    public String toCanonicalString() {
        return Long.toUnsignedString(value);
    }

    @Override
    public CanonicalValueSupport<Uint64> support() {
        return SUPPORT;
    }

    /**
     * Return an interned (shared) instance equivalent to this object. This may return the same object.
     *
     * @return A shared instance.
     */
    public Uint64 intern() {
        return value >= 0 && value < CACHE_SIZE ? this : INTERNER.intern(this);
    }

    /**
     * Convert this value to a {@link BigInteger}.
     *
     * @return A BigInteger instance
     */
    public BigInteger toJava() {
        // FIXME: ditch the Guava transition
        return toGuava().bigIntegerValue();
    }

    /**
     * Convert this value to an {@link UnsignedLong}.
     *
     * @return An UnsignedLong instance
     */
    public UnsignedLong toGuava() {
        return UnsignedLong.fromLongBits(value);
    }

    @Override
    public int hashCode() {
        return Long.hashCode(value);
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof Uint64 && value == ((Uint64)obj).value;
    }

    @Override
    public String toString() {
        return toCanonicalString();
    }

    private Object readResolve() {
        return instanceFor(value);
    }
}
