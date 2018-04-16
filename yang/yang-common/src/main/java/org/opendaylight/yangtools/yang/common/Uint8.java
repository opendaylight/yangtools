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
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Variant;

/**
 * Dedicated type for YANG's 'type uint8' type.
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public class Uint8 extends Number implements CanonicalValue<Uint8> {
    private static final class Support extends AbstractCanonicalValueSupport<Uint8> {
        Support() {
            super(Uint8.class);
        }

        @Override
        public Variant<Uint8, CanonicalValueViolation> fromString(final String str) {
            try {
                return Variant.ofFirst(Uint8.valueOf(str));
            } catch (IllegalArgumentException e) {
                return CanonicalValueViolation.variantOf(e);
            }
        }
    }

    private static final CanonicalValueSupport<Uint8> SUPPORT = new Support();

    static final short MIN_VALUE = 0;
    static final short MAX_VALUE = 255;

    private static final long serialVersionUID = 1L;
    private static final Uint8[] CACHE = new Uint8[MAX_VALUE + 1];

    private final byte value;

    private Uint8(final byte value) {
        this.value = value;
    }

    protected Uint8(final Uint8 other) {
        this.value = other.value;
    }

    private static Uint8 instanceFor(final byte value) {
        final int slot = Byte.toUnsignedInt(value);

        Uint8 ret = CACHE[slot];
        if (ret == null) {
            synchronized (CACHE) {
                ret = CACHE[slot];
                if (ret == null) {
                    ret = new Uint8(value);
                    CACHE[slot] = ret;
                }
            }
        }

        return ret;
    }

    public static Uint8 fromByteBits(final byte bits) {
        return instanceFor(bits);
    }

    public static Uint8 valueOf(final byte byteVal) {
        checkArgument(byteVal >= MIN_VALUE, "Negative values are not allowed");
        return instanceFor(byteVal);
    }

    public static Uint8 valueOf(final short shortVal) {
        checkArgument(shortVal >= MIN_VALUE && shortVal <= MAX_VALUE, "Value %s is outside of allowed range", shortVal);
        return instanceFor((byte)(shortVal & 0xff));
    }

    public static Uint8 valueOf(final int intVal) {
        checkArgument(intVal >= MIN_VALUE && intVal <= MAX_VALUE, "Value %s is outside of allowed range", intVal);
        return instanceFor((byte)(intVal & 0xff));
    }

    public static Uint8 valueOf(final long longVal) {
        checkArgument(longVal >= MIN_VALUE && longVal <= MAX_VALUE, "Value %s is outside of allowed range", longVal);
        return instanceFor((byte)(longVal & 0xff));
    }

    public static Uint8 valueOf(final Uint16 uint) {
        return valueOf(uint.intValue());
    }

    public static Uint8 valueOf(final Uint32 uint) {
        return valueOf(uint.longValue());
    }

    public static Uint8 valueOf(final Uint64 uint) {
        return valueOf(uint.longValue());
    }

    public static Uint8 valueOf(final String string) {
        return valueOf(string, 10);
    }

    public static Uint8 valueOf(final String string, final int radix) {
        return valueOf(Short.parseShort(string, radix));
    }

    @Override
    public final byte byteValue() {
        return value;
    }

    @Override
    public final int intValue() {
        return Byte.toUnsignedInt(value);
    }

    @Override
    public final long longValue() {
        return Byte.toUnsignedLong(value);
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
    public final int compareTo(final Uint8 o) {
        return intValue() - o.intValue();
    }

    @Override
    public final String toCanonicalString() {
        return String.valueOf(intValue());
    }

    @Override
    public final CanonicalValueSupport<Uint8> support() {
        return SUPPORT;
    }

    @Override
    public final int hashCode() {
        return Byte.hashCode(value);
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof Uint8 && value == ((Uint8)obj).value;
    }

    @Override
    public final String toString() {
        return toCanonicalString();
    }

    private Object readResolve() {
        return instanceFor(value);
    }
}
