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
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.primitives.UnsignedInteger;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * Dedicated type for YANG's 'type uint32' type.
 *
 * @author Robert Varga
 */
@Beta
public final class Uint32 extends Number implements Comparable<Uint32>, Immutable {
    private static final long serialVersionUID = 1L;
    private static final long MIN_VALUE = 0;
    private static final long MAX_VALUE = 0xffffffffL;

    /**
     * Cache of first 256 values.
     */
    private static final Uint32[] CACHE = new Uint32[Uint8.MAX_VALUE];
    /**
     * Commonly encountered values.
     */
    private static final Uint32[] COMMON = {
        new Uint32(Short.MAX_VALUE),
        new Uint32(32768),
        new Uint32(65535),
        new Uint32(65536),
        new Uint32(Integer.MAX_VALUE),
    };

    /**
     * Tunable weak LRU cache for other values. By default it holds {@value #DEFAULT_LRU_SIZE} entries. This can be
     * changed via {@value #LRU_SIZE_PROPERTY} system property.
     */
    private static final int DEFAULT_LRU_SIZE = 1024;
    private static final String LRU_SIZE_PROPERTY = "org.opendaylight.yangtools.yang.common.Uint32.LRU.size";
    private static final int MAX_LRU_SIZE = 0xffffff;
    private static final int LRU_SIZE;

    static {
        final int p = Integer.getInteger(LRU_SIZE_PROPERTY, DEFAULT_LRU_SIZE);
        LRU_SIZE = p >= 0 ? Math.min(p, MAX_LRU_SIZE) : DEFAULT_LRU_SIZE;
    }

    private static final LoadingCache<Integer, Uint32> LRU = CacheBuilder.newBuilder().weakValues()
        .maximumSize(LRU_SIZE).build(new CacheLoader<Integer, Uint32>() {
                @Override
                public Uint32 load(final Integer key) {
                    return new Uint32(key);
                }
            });

    private final int value;

    private Uint32(final int value) {
        this.value = value;
    }

    private static Uint32 instanceFor(final int value) {
        final long longSlot = Integer.toUnsignedLong(value);
        if (longSlot >= CACHE.length) {
            for (Uint32 c : COMMON) {
                if (c.value == value) {
                    return c;
                }
            }

            return LRU.getUnchecked(value);
        }

        final int slot = (int)longSlot;
        Uint32 ret = CACHE[slot];
        if (ret == null) {
            synchronized (CACHE) {
                ret = CACHE[slot];
                if (ret == null) {
                    ret = new Uint32(value);
                    CACHE[slot] = ret;
                }
            }
        }

        return ret;
    }

    public static Uint32 fromIntBits(final int bits) {
        return instanceFor(bits);
    }

    public static Uint32 fromUnsignedInteger(final UnsignedInteger uint) {
        return instanceFor(uint.intValue());
    }

    public static Uint32 valueOf(final byte byteVal) {
        checkArgument(byteVal >= MIN_VALUE, "Negative values are not allowed");
        return instanceFor(byteVal);
    }

    public static Uint32 valueOf(final short shortVal) {
        checkArgument(shortVal >= MIN_VALUE, "Negative values are not allowed");
        return instanceFor(shortVal);
    }

    public static Uint32 valueOf(final int intVal) {
        checkArgument(intVal >= MIN_VALUE, "Value %s is outside of allowed range", intVal);
        return instanceFor(intVal);
    }

    public static Uint32 valueOf(final long longVal) {
        checkArgument(longVal >= MIN_VALUE && longVal <= MAX_VALUE, "Value %s is outside of allowed range", longVal);
        return instanceFor((int)longVal);
    }

    public static Uint32 valueOf(final Uint8 uint) {
        return instanceFor(uint.shortValue());
    }

    public static Uint32 valueOf(final Uint16 uint) {
        return instanceFor(uint.intValue());
    }

    public static Uint32 valueOf(final String string) {
        return valueOf(string, 10);
    }

    public static Uint32 valueOf(final String string, final int radix) {
        return instanceFor(Integer.parseUnsignedInt(string, radix));
    }

    @Override
    public int intValue() {
        return value;
    }

    @Override
    public long longValue() {
        return Integer.toUnsignedLong(value);
    }

    @Override
    public float floatValue() {
        return longValue();
    }

    @Override
    public double doubleValue() {
        return longValue();
    }

    public UnsignedInteger toUnsignedInteger() {
        return UnsignedInteger.fromIntBits(value);
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public int compareTo(final Uint32 o) {
        return Integer.compareUnsigned(value, o.value);
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        return obj instanceof Uint32 && value == ((Uint32)obj).value;
    }

    @Override
    public String toString() {
        return Integer.toUnsignedString(value);
    }

    private Object readResolve() {
        return instanceFor(value);
    }
}
