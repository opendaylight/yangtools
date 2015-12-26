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
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Beta
public final class Uint16 extends Number implements Comparable<Uint16> {
    private static final long serialVersionUID = 1L;
    private static final int MIN_VALUE = 0;
    private static final int MAX_VALUE = 65535;

    /**
     * Cache of first 256 values.
     */
    private static final Uint16 CACHE[] = new Uint16[Uint8.MAX_VALUE];
    /**
     * Commonly encountered values.
     */
    private static final Uint16 COMMON[] = new Uint16[] {
            new Uint16(Short.MAX_VALUE),
            new Uint16((short)32768),
            new Uint16((short)65535),
    };

    /**
     * Tunable weak LRU cache for other values. By default it holds {@value #DEFAULT_LRU_SIZE} entries. This can be
     * changed via {@value #LRU_SIZE_PROPERTY} system property.
     */
    private static final int DEFAULT_LRU_SIZE = 1024;
    private static final String LRU_SIZE_PROPERTY = "org.opendaylight.yangtools.yang.common.Uint16.LRU.size";
    private static final int MAX_LRU_SIZE = MAX_VALUE + 1;
    private static final int LRU_SIZE;
    static {
        final int p = Integer.getInteger(LRU_SIZE_PROPERTY, DEFAULT_LRU_SIZE);
        LRU_SIZE = p >= 0 ? Math.min(p, MAX_LRU_SIZE) : DEFAULT_LRU_SIZE;
    }
    private static final LoadingCache<Short, Uint16> LRU = CacheBuilder.newBuilder().weakValues().maximumSize(LRU_SIZE)
            .build(new CacheLoader<Short, Uint16>() {
                @Override
                public Uint16 load(final Short key) {
                    return new Uint16(key);
                }
            });

    private final short value;

    private Uint16(final short value) {
        this.value = value;
    }

    private static Uint16 instanceFor(final short value) {
        final int slot = Short.toUnsignedInt(value);
        if (slot >= CACHE.length) {
            for (Uint16 c : COMMON) {
                if (c.value == value) {
                    return c;
                }
            }

            return LRU.getUnchecked(value);
        }

        Uint16 ret = CACHE[slot];
        if (ret == null) {
            synchronized (CACHE) {
                ret = CACHE[slot];
                if (ret == null) {
                    ret = new Uint16(value);
                    CACHE[slot] = ret;
                }
            }
        }

        return ret;
    }

    public static Uint16 fromShortBits(final short bits) {
        return instanceFor(bits);
    }

    public static Uint16 valueOf(final byte b) {
        Preconditions.checkArgument(b >= MIN_VALUE, "Negative values are not allowed");
        return instanceFor(b);
    }

    public static Uint16 valueOf(final short s) {
        Preconditions.checkArgument(s >= MIN_VALUE, "Negative values are not allowed");
        return instanceFor(s);
    }

    public static Uint16 valueOf(final int i) {
        Preconditions.checkArgument(i >= MIN_VALUE && i <= MAX_VALUE, "Value %s is outside of allowed range", i);
        return instanceFor((short)(i & 0xffff));
    }

    public static Uint16 valueOf(final long l) {
        Preconditions.checkArgument(l >= MIN_VALUE && l <= MAX_VALUE, "Value %s is outside of allowed range", l);
        return instanceFor((short)(l & 0xffff));
    }

    public static Uint16 valueOf(final Uint8 uint) {
        return instanceFor(uint.shortValue());
    }

    public static Uint16 valueOf(final Uint32 uint) {
        return valueOf(uint.longValue());
    }

    public static Uint16 valueOf(final String string) {
        return valueOf(string, 10);
    }

    public static Uint16 valueOf(final String string, final int radix) {
        // FIXME: implement this
        throw new UnsupportedOperationException();
    }

    @Override
    public short shortValue() {
        return value;
    }

    @Override
    public int intValue() {
        return Short.toUnsignedInt(value);
    }

    @Override
    public long longValue() {
        return Short.toUnsignedLong(value);
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
    public int compareTo(final Uint16 o) {
        return Integer.compare(intValue(), o.intValue());
    }

    @Override
    public int hashCode() {
        return Short.hashCode(value);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        return (o instanceof Uint16) && value == ((Uint16)o).value;
    }

    @Override
    public String toString() {
        return String.valueOf(intValue());
    }

    private Object readResolve() {
        return instanceFor(value);
    }
}
