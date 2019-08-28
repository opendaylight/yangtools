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

    /**
     * Cache of first 256 values.
     */
    private static final Uint32[] CACHE = new Uint32[256];
    /**
     * Commonly encountered values.
     */
    private static final Uint32[] COMMON = {
        new Uint32(Short.MAX_VALUE),
        new Uint32(32768),
        new Uint32(65535),
        new Uint32(65536),
        new Uint32(Integer.MAX_VALUE),
        new Uint32(-1)
    };

    public static final Uint32 MIN_VALUE = valueOf(MIN_VALUE_LONG);
    public static final Uint32 MAX_VALUE = valueOf(MAX_VALUE_LONG);

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

    Uint32(final int value) {
        this.value = value;
    }

    protected Uint32(final Uint32 other) {
        this.value = other.value;
    }

    private static Uint32 instanceFor(final int value) {
        final long longSlot = Integer.toUnsignedLong(value);
        return longSlot < CACHE.length ? fromCache((int)longSlot, value) : fromCommon(value);
    }

    private static Uint32 fromCommon(final int value) {
        for (Uint32 c : COMMON) {
            if (c.value == value) {
                return c;
            }
        }
        return LRU.getUnchecked(value);
    }

    private static Uint32 fromCache(final int slot, final int value) {
        // FIXME: 4.0.0: use VarHandles here
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
        checkArgument(byteVal >= MIN_VALUE_LONG, "Negative values are not allowed");
        return instanceFor(byteVal);
    }

    public static Uint32 valueOf(final short shortVal) {
        checkArgument(shortVal >= MIN_VALUE_LONG, "Negative values are not allowed");
        return instanceFor(shortVal);
    }

    public static Uint32 valueOf(final int intVal) {
        checkArgument(intVal >= MIN_VALUE_LONG, "Value %s is outside of allowed range", intVal);
        return instanceFor(intVal);
    }

    public static Uint32 valueOf(final long longVal) {
        checkArgument(longVal >= MIN_VALUE_LONG && longVal <= MAX_VALUE_LONG, "Value %s is outside of allowed range",
                longVal);
        return instanceFor((int)longVal);
    }

    public static Uint32 valueOf(final Uint8 uint) {
        return instanceFor(uint.shortValue());
    }

    public static Uint32 valueOf(final Uint16 uint) {
        return instanceFor(uint.intValue());
    }

    public static Uint32 valueOf(final Uint64 uint) {
        return valueOf(uint.longValue());
    }

    public static Uint32 valueOf(final String string) {
        return valueOf(string, 10);
    }

    public static Uint32 valueOf(final String string, final int radix) {
        return instanceFor(Integer.parseUnsignedInt(string, radix));
    }

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
