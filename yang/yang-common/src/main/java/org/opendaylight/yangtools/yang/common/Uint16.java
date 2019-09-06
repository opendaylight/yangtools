/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.concepts.Variant;

/**
 * Dedicated type for YANG's 'type uint16' type.
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public class Uint16 extends Number implements CanonicalValue<Uint16> {
    @MetaInfServices(value = CanonicalValueSupport.class)
    public static final class Support extends AbstractCanonicalValueSupport<Uint16> {
        public Support() {
            super(Uint16.class);
        }

        @Override
        public Variant<Uint16, CanonicalValueViolation> fromString(final String str) {
            try {
                return Variant.ofFirst(Uint16.valueOf(str));
            } catch (IllegalArgumentException e) {
                return CanonicalValueViolation.variantOf(e);
            }
        }
    }

    private static final CanonicalValueSupport<Uint16> SUPPORT = new Support();
    private static final long serialVersionUID = 1L;
    private static final int MIN_VALUE_INT = 0;
    private static final int MAX_VALUE_INT = 65535;

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
        for (int i = MIN_VALUE_INT; i < c.length; ++i) {
            c[i] = new Uint16((short) i);
        }
        CACHE = c;
    }

    private static final Interner<Uint16> INTERNER = Interners.newWeakInterner();

    public static final Uint16 ZERO = valueOf(MIN_VALUE_INT).intern();
    public static final Uint16 ONE = valueOf(1).intern();
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
        checkArgument(byteVal >= MIN_VALUE_INT, "Negative values are not allowed");
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
        checkArgument(shortVal >= MIN_VALUE_INT, "Negative values are not allowed");
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
        checkArgument(intVal >= MIN_VALUE_INT && intVal <= MAX_VALUE_INT, "Value %s is outside of allowed range",
                intVal);
        return instanceFor((short)(intVal & 0xffff));
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
        checkArgument(longVal >= MIN_VALUE_INT && longVal <= MAX_VALUE_INT, "Value %s is outside of allowed range",
                longVal);
        return instanceFor((short)(longVal & 0xffff));
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
     * {@inheritDoc}
     *
     * <p>
     * The inverse operation is {@link #fromShortBits(short)}. In case this value is greater than
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
        return Integer.compare(intValue(), o.intValue());
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

    @Override
    public final int hashCode() {
        return Short.hashCode(value);
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof Uint16 && value == ((Uint16)obj).value;
    }

    @Override
    public final String toString() {
        return toCanonicalString();
    }

    private Object readResolve() {
        return instanceFor(value);
    }
}
