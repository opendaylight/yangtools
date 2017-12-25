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
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * Dedicated type for YANG's 'type uint16' type.
 *
 * @author Robert Varga
 */
@Beta
public final class Uint16 extends Number implements Comparable<Uint16>, Immutable {
    private static final long serialVersionUID = 1L;
    private static final int MIN_VALUE = 0;
    private static final int MAX_VALUE = 65535;

    /**
     * Cache of first 256 values.
     */
    private static final Uint16[] CACHE = new Uint16[Uint8.MAX_VALUE];

    /**
     * Commonly encountered values.
     */
    private static final Uint16[] COMMON = new Uint16[] {
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

    @VisibleForTesting
    Uint16(final short value) {
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

    public static Uint16 valueOf(final byte byteVal) {
        checkArgument(byteVal >= MIN_VALUE, "Negative values are not allowed");
        return instanceFor(byteVal);
    }

    public static Uint16 valueOf(final short shortVal) {
        checkArgument(shortVal >= MIN_VALUE, "Negative values are not allowed");
        return instanceFor(shortVal);
    }

    public static Uint16 valueOf(final int intVal) {
        checkArgument(intVal >= MIN_VALUE && intVal <= MAX_VALUE, "Value %s is outside of allowed range", intVal);
        return instanceFor((short)(intVal & 0xffff));
    }

    public static Uint16 valueOf(final long longVal) {
        checkArgument(longVal >= MIN_VALUE && longVal <= MAX_VALUE, "Value %s is outside of allowed range", longVal);
        return instanceFor((short)(longVal & 0xffff));
    }

    public static Uint16 valueOf(final Uint8 uint) {
        return instanceFor(uint.shortValue());
    }

    public static Uint16 valueOf(final Uint32 uint) {
        return valueOf(uint.longValue());
    }

    public static Uint16 valueOf(final Uint64 uint) {
        return valueOf(uint.longValue());
    }

    public static Uint16 valueOf(final String string) {
        return valueOf(string, 10);
    }

    public static Uint16 valueOf(final String string, final int radix) {
        return valueOf(Integer.parseInt(string, radix));
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
    @SuppressWarnings("checkstyle:parameterName")
    public int compareTo(final Uint16 o) {
        return Integer.compare(intValue(), o.intValue());
    }

    @Override
    public int hashCode() {
        return Short.hashCode(value);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        return obj instanceof Uint16 && value == ((Uint16)obj).value;
    }

    @Override
    public String toString() {
        return String.valueOf(intValue());
    }

    private Object readResolve() {
        return instanceFor(value);
    }
}
