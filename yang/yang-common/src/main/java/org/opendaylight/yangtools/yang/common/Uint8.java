/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;

@Beta
public final class Uint8 extends Number implements Comparable<Uint8> {
    private static final long serialVersionUID = 1L;
    static final short MIN_VALUE = 0;
    static final short MAX_VALUE = 255;
    private static final Uint8[] CACHE = new Uint8[MAX_VALUE + 1];

    private final byte value;

    private Uint8(final byte value) {
        this.value = value;
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

    public static Uint8 valueOf(final byte b) {
        Preconditions.checkArgument(b >= MIN_VALUE, "Negative values are not allowed");
        return instanceFor(b);
    }

    public static Uint8 valueOf(final short s) {
        Preconditions.checkArgument(s >= MIN_VALUE && s <= MAX_VALUE, "Value %s is outside of allowed range", s);
        return instanceFor((byte)(s & 0xff));
    }

    public static Uint8 valueOf(final int i) {
        Preconditions.checkArgument(i >= MIN_VALUE && i <= MAX_VALUE, "Value %s is outside of allowed range", i);
        return instanceFor((byte)(i & 0xff));
    }

    public static Uint8 valueOf(final long l) {
        Preconditions.checkArgument(l >= MIN_VALUE && l <= MAX_VALUE, "Value %s is outside of allowed range", l);
        return instanceFor((byte)(l & 0xff));
    }

    public static Uint8 valueOf(final Uint16 uint) {
        return valueOf(uint.intValue());
    }

    public static Uint8 valueOf(final String string) {
        return valueOf(string, 10);
    }

    public static Uint8 valueOf(final String string, final int radix) {
        // FIXME: implement this
        throw new UnsupportedOperationException();
    }

    @Override
    public byte byteValue() {
        return value;
    }

    @Override
    public int intValue() {
        return Byte.toUnsignedInt(value);
    }

    @Override
    public long longValue() {
        return Byte.toUnsignedLong(value);
    }

    @Override
    public float floatValue() {
        return intValue();
    }

    @Override
    public double doubleValue() {
        return intValue();
    }

    @Override
    public int compareTo(final Uint8 o) {
        return intValue() - o.intValue();
    }

    @Override
    public int hashCode() {
        return Byte.hashCode(value);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        return (o instanceof Uint8) && value == ((Uint8)o).value;
    }

    @Override
    public String toString() {
        return String.valueOf(intValue());
    }

    private Object readResolve() {
        return instanceFor(value);
    }
}
