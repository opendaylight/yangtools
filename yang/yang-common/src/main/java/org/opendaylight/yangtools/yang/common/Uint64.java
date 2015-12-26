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
import com.google.common.primitives.UnsignedLong;
import java.math.BigInteger;

@Beta
public final class Uint64 extends Number implements Comparable<Uint64> {
    private static final long serialVersionUID = 1L;
    private static final long MIN_VALUE = 0;

    /**
     * Cache of first 256 values.
     */
    private static final Uint64 CACHE[] = new Uint64[Uint8.MAX_VALUE];
    /**
     * Commonly encountered values.
     */
    private static final Uint64 COMMON[] = new Uint64[] {
            new Uint64(Short.MAX_VALUE + 1L),
            new Uint64(32768),
            new Uint64(65535),
            new Uint64(65536),
            new Uint64(Integer.MAX_VALUE),
            new Uint64(Integer.MAX_VALUE + 1L),
            new Uint64(Long.MAX_VALUE),
    };

    /**
     * Tunable weak LRU cache for other values. By default it holds {@value #DEFAULT_LRU_SIZE} entries. This can be
     * changed via {@value #LRU_SIZE_PROPERTY} system property.
     */
    private static final int DEFAULT_LRU_SIZE = 1024;
    private static final String LRU_SIZE_PROPERTY = "org.opendaylight.yangtools.yang.common.Uint64.LRU.size";
    private static final int MAX_LRU_SIZE = 0xffffff;
    private static final int LRU_SIZE;
    static {
        final int p = Integer.getInteger(LRU_SIZE_PROPERTY, DEFAULT_LRU_SIZE);
        LRU_SIZE = p >= 0 ? Math.min(p, MAX_LRU_SIZE) : DEFAULT_LRU_SIZE;
    }
    private static final LoadingCache<Long, Uint64> LRU = CacheBuilder.newBuilder().weakValues().maximumSize(LRU_SIZE)
            .build(new CacheLoader<Long, Uint64>() {
                @Override
                public Uint64 load(final Long key) {
                    return new Uint64(key);
                }
            });

    private final long value;

    private Uint64(final long value) {
        this.value = value;
    }

    private static Uint64 instanceFor(final long value) {
        final int slot = (int)value;
        if (value < 0 || slot >= CACHE.length) {
            for (Uint64 c : COMMON) {
                if (c.value == value) {
                    return c;
                }
            }

            return LRU.getUnchecked(value);
        }

        Uint64 ret = CACHE[slot];
        if (ret == null) {
            synchronized (CACHE) {
                ret = CACHE[slot];
                if (ret == null) {
                    ret = new Uint64(value);
                    CACHE[slot] = ret;
                }
            }
        }

        return ret;
    }

    public static Uint64 fromLongBits(final long bits) {
        return instanceFor(bits);
    }

    public static Uint64 valueOf(final byte b) {
        Preconditions.checkArgument(b >= MIN_VALUE, "Negative values are not allowed");
        return instanceFor(b);
    }

    public static Uint64 valueOf(final short s) {
        Preconditions.checkArgument(s >= MIN_VALUE, "Negative values are not allowed");
        return instanceFor(s);
    }

    public static Uint64 valueOf(final int i) {
        Preconditions.checkArgument(i >= MIN_VALUE, "Value %s is outside of allowed range", i);
        return instanceFor(i);
    }

    public static Uint64 valueOf(final long l) {
        Preconditions.checkArgument(l >= MIN_VALUE, "Value %s is outside of allowed range", l);
        return instanceFor(l);
    }

    public static Uint64 valueOf(final Uint8 uint) {
        return instanceFor(uint.shortValue());
    }

    public static Uint64 valueOf(final Uint16 uint) {
        return instanceFor(uint.intValue());
    }

    public static Uint64 valueOf(final Uint32 uint) {
        return instanceFor(uint.longValue());
    }

    public static Uint64 valueOf(final String string) {
        return valueOf(string, 10);
    }

    public static Uint64 valueOf(final String string, final int radix) {
        return instanceFor(Long.parseUnsignedLong(string, radix));
    }

    public static Uint64 valueOf(final BigInteger bigInt) {
        Preconditions.checkArgument(bigInt.signum() >= 0, "Negative values not allowed");
        Preconditions.checkArgument(bigInt.bitLength() <= Long.SIZE, "Value %s is outside of allowed range", bigInt);

        return instanceFor(bigInt.longValue());
    }

    @Override
    public int intValue() {
        return (int)value;
    }

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
    public int compareTo(final Uint64 o) {
        return Long.compareUnsigned(value, o.value);
    }

    @Override
    public int hashCode() {
        return Long.hashCode(value);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        return (o instanceof Uint64) && value == ((Uint64)o).value;
    }

    @Override
    public String toString() {
        return Long.toUnsignedString(value);
    }

    private Object readResolve() {
        return instanceFor(value);
    }
}
