/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.value;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Decimal64;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;

/**
 * An interner covering basic data types supported for LeafNodes. While there may be any number of instances of this
 * class, all of them share the same global backing store of weakly-referenced values.
 */
@Beta
public final class ValueInterner {
    private static final @NonNull ValueInterner NOOP =
        new ValueInterner(null, null, null, null, null, false, false, false);

    private final Interner<String> strings;
    private final Interner<Decimal64> decimal64s;
    private final Interner<Short> int16s;
    private final Interner<Integer> int32s;
    private final Interner<Long> int64s;
    private final boolean uint16;
    private final boolean uint32;
    private final boolean uint64;

    private ValueInterner(final Interner<String> strings, final Interner<Decimal64> decimal64s,
            final Interner<Short> int16s, final Interner<Integer> int32s, final Interner<Long> int64s,
            final boolean uint16, final boolean uint32, final boolean uint64) {
        this.strings = requireNonNull(strings);
        this.decimal64s = requireNonNull(decimal64s);
        this.int16s = requireNonNull(int16s);
        this.int32s = requireNonNull(int32s);
        this.int64s = requireNonNull(int64s);
        this.uint16 = uint16;
        this.uint32 = uint32;
        this.uint64 = uint64;
    }

    public static @NonNull Builder builder() {
        return new Builder();
    }

    public static @NonNull ValueInterner noop() {
        return NOOP;
    }

    public @NonNull String internValue(final String value) {
        return intern(strings, value);
    }

    public @NonNull Decimal64 internValue(final Decimal64 value) {
        return intern(decimal64s, value);
    }

    public @NonNull Short internValue(final Short value) {
        return intern(int16s, value);
    }

    public @NonNull Integer internValue(final Integer value) {
        return intern(int32s, value);
    }

    public @NonNull Long internValue(final Long value) {
        return intern(int64s, value);
    }

    public @NonNull Uint16 internValue(final Uint16 value) {
        return uint16 ? value.intern() : requireNonNull(value);
    }

    public @NonNull Uint32 internValue(final Uint32 value) {
        return uint32 ? value.intern() : requireNonNull(value);
    }

    public @NonNull Uint64 internValue(final Uint64 value) {
        return uint64 ? value.intern() : requireNonNull(value);
    }

    @Override
    public String toString() {
        final var helper = MoreObjects.toStringHelper(this);
        if (decimal64s != null) {
            helper.addValue("decimal64");
        }
        if (int16s != null) {
            helper.addValue("int16");
        }
        if (int32s != null) {
            helper.addValue("int32");
        }
        if (int64s != null) {
            helper.addValue("int64");
        }
        if (strings != null) {
            helper.addValue("string");
        }
        if (uint16) {
            helper.addValue("int16");
        }
        if (uint32) {
            helper.addValue("int32");
        }
        if (uint64) {
            helper.addValue("int64");
        }
        return helper.toString();
    }

    private static <T> @NonNull T intern(final Interner<T> interner, final T value) {
        final var obj = requireNonNull(value);
        return interner != null ? interner.intern(obj) : obj;
    }

    public static final class Builder {
        private static final Interner<String> STRINGS = Interners.newWeakInterner();
        private static final Interner<Short> INT16S = Interners.newWeakInterner();
        private static final Interner<Integer> INT32S = Interners.newWeakInterner();
        private static final Interner<Long> INT64S = Interners.newWeakInterner();
        private static final Interner<Decimal64> DECIMAL64S = Interners.newWeakInterner();

        private Interner<String> strings;
        private Interner<Decimal64> decimal64s;
        private Interner<Short> int16s;
        private Interner<Integer> int32s;
        private Interner<Long> int64s;
        private boolean uint16;
        private boolean uint32;
        private boolean uint64;

        Builder() {
            // Hidden on purpose
        }

        public @NonNull Builder globalString() {
            strings = STRINGS;
            return this;
        }

        public @NonNull Builder globalDecimal64() {
            decimal64s = DECIMAL64S;
            return this;
        }

        public @NonNull Builder globalInt16() {
            int16s = INT16S;
            return this;
        }

        public @NonNull Builder globalInt32() {
            int32s = INT32S;
            return this;
        }

        public @NonNull Builder globalInt64() {
            int64s = INT64S;
            return this;
        }

        public @NonNull Builder globalUint16() {
            uint16 = true;
            return this;
        }

        public @NonNull Builder globalUint32() {
            uint32 = true;
            return this;
        }

        public @NonNull Builder globalUint64() {
            uint64 = true;
            return this;
        }

        public @NonNull ValueInterner build() {
            return new ValueInterner(strings, decimal64s, int16s, int32s, int64s, uint16, uint32, uint64);
        }
    }
}
