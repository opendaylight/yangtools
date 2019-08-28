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
import com.google.common.primitives.UnsignedLong;
import java.math.BigInteger;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.concepts.Variant;

/**
 * Dedicated type for YANG's 'type uint64' type.
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public class Uint64 extends Number implements CanonicalValue<Uint64> {
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

    /**
     * Cache of first 256 values.
     */
    private static final Uint64[] CACHE = new Uint64[Uint8.MAX_VALUE_SHORT];
    /**
     * Commonly encountered values.
     */
    private static final Uint64[] COMMON = {
        new Uint64(Short.MAX_VALUE + 1L),
        new Uint64(32768),
        new Uint64(65535),
        new Uint64(65536),
        new Uint64(Integer.MAX_VALUE),
        new Uint64(Integer.MAX_VALUE + 1L),
        new Uint64(Long.MAX_VALUE),
        new Uint64(-1L)
    };

    public static final Uint64 MIN_VALUE = valueOf(MIN_VALUE_LONG);
    public static final Uint64 MAX_VALUE = fromLongBits(-1);

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

    Uint64(final long value) {
        this.value = value;
    }

    protected Uint64(final Uint64 other) {
        this.value = other.value;
    }

    private static Uint64 instanceFor(final long value) {
        final int slot = (int)value;
        return slot >= 0 && slot < CACHE.length ? fromCache(slot, value) : fromCommon(value);
    }

    private static Uint64 fromCommon(final long value) {
        for (Uint64 c : COMMON) {
            if (c.value == value) {
                return c;
            }
        }
        return LRU.getUnchecked(value);
    }

    private static Uint64 fromCache(final int slot, final long value) {
        // FIXME: 4.0.0: use VarHandles here
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

    public static Uint64 fromUnsignedLong(final UnsignedLong ulong) {
        return instanceFor(ulong.longValue());
    }

    public static Uint64 valueOf(final byte byteVal) {
        checkArgument(byteVal >= MIN_VALUE_LONG, "Negative values are not allowed");
        return instanceFor(byteVal);
    }

    public static Uint64 valueOf(final short shortVal) {
        checkArgument(shortVal >= MIN_VALUE_LONG, "Negative values are not allowed");
        return instanceFor(shortVal);
    }

    public static Uint64 valueOf(final int intVal) {
        checkArgument(intVal >= MIN_VALUE_LONG, "Value %s is outside of allowed range", intVal);
        return instanceFor(intVal);
    }

    public static Uint64 valueOf(final long longVal) {
        checkArgument(longVal >= MIN_VALUE_LONG, "Value %s is outside of allowed range", longVal);
        return instanceFor(longVal);
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
        checkArgument(bigInt.signum() >= 0, "Negative values not allowed");
        checkArgument(bigInt.bitLength() <= Long.SIZE, "Value %s is outside of allowed range", bigInt);

        return instanceFor(bigInt.longValue());
    }

    @Override
    public final int intValue() {
        return (int)value;
    }

    @Override
    public final long longValue() {
        return value;
    }

    @Override
    public final float floatValue() {
        // TODO: ditch Guava
        return UnsignedLong.fromLongBits(value).floatValue();
    }

    @Override
    public final double doubleValue() {
        // TODO: ditch Guava
        return UnsignedLong.fromLongBits(value).doubleValue();
    }

    public final UnsignedLong toUnsignedLong() {
        return UnsignedLong.fromLongBits(value);
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public final int compareTo(final Uint64 o) {
        return Long.compareUnsigned(value, o.value);
    }

    @Override
    public final String toCanonicalString() {
        return Long.toUnsignedString(value);
    }

    @Override
    public final CanonicalValueSupport<Uint64> support() {
        return SUPPORT;
    }

    @Override
    public final int hashCode() {
        return Long.hashCode(value);
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof Uint64 && value == ((Uint64)obj).value;
    }

    @Override
    public final String toString() {
        return toCanonicalString();
    }

    private Object readResolve() {
        return instanceFor(value);
    }
}
