/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.type;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableMap;
import java.util.Comparator;
import java.util.function.Function;
import org.opendaylight.yangtools.yang.common.Decimal64;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

final class NumberUtil {
    private static final Comparator<Number> NUMBER_COMPARATOR = (o1, o2) -> {
        // FIXME: Uint8 et al are not final
        checkArgument(o1.getClass().equals(o2.getClass()), "Incompatible Number classes %s and %s",
            o1.getClass(), o2.getClass());

        return switch (o1) {
            case Byte b -> b.compareTo((Byte) o2);
            case Short s -> s.compareTo((Short) o2);
            case Integer i -> i.compareTo((Integer) o2);
            case Long l -> l.compareTo((Long) o2);
            case Uint8 u8 -> u8.compareTo((Uint8) o2);
            case Uint16 u16 -> u16.compareTo((Uint16) o2);
            case Uint32 u32 -> u32.compareTo((Uint32) o2);
            case Uint64 u64 -> u64.compareTo((Uint64) o2);
            case Decimal64 d64 -> d64.compareTo((Decimal64) o2);
            default -> throw new IllegalArgumentException("Unsupported Number class " + o1.getClass());
        };
    };

    private static final ImmutableMap<Class<? extends Number>, Function<Number, Number>> CONVERTERS;

    static {
        final ImmutableMap.Builder<Class<? extends Number>, Function<Number, Number>> b = ImmutableMap.builder();
        b.put(Byte.class, input -> input instanceof Byte val ? val : Byte.valueOf(input.toString()));
        b.put(Short.class, input ->
            switch (input) {
                case Short val -> val;
                case Byte val -> val.shortValue();
                default -> Short.valueOf(input.toString());
            });
        b.put(Integer.class, input ->
            switch (input) {
                case Integer val -> val;
                case Short val -> val.intValue();
                case Byte val -> val.intValue();
                default -> Integer.valueOf(input.toString());
            });
        b.put(Long.class, input ->
            switch (input) {
                case Long val -> val;
                case Integer val -> val.longValue();
                case Short val -> val.longValue();
                case Byte val -> val.longValue();
                default -> Long.valueOf(input.toString());
            });
        b.put(Decimal64.class, input -> {
            if (input instanceof Decimal64) {
                return input;
            }
            if (input instanceof Byte || input instanceof Short || input instanceof Integer || input instanceof Long) {
                // FIXME: this is not right, as we need to know fraction-digits
                return Decimal64.valueOf(1, input.longValue());
            }

            return Decimal64.valueOf(input.toString());
        });
        b.put(Uint8.class, input -> {
            if (input instanceof Uint8) {
                return input;
            }
            // FIXME: revise this
            if (input instanceof Byte || input instanceof Short || input instanceof Integer || input instanceof Long
                    || input instanceof Uint16 || input instanceof Uint32 || input instanceof Uint64) {
                return Uint8.valueOf(input.longValue());
            }

            return Uint8.valueOf(input.toString());
        });
        b.put(Uint16.class, input -> {
            if (input instanceof Uint16) {
                return input;
            }
            // FIXME: revise this
            if (input instanceof Byte || input instanceof Short || input instanceof Integer || input instanceof Long
                    || input instanceof Uint8 || input instanceof Uint32 || input instanceof Uint64) {
                return Uint16.valueOf(input.longValue());
            }

            return Uint16.valueOf(input.toString());
        });
        b.put(Uint32.class, input -> {
            if (input instanceof Uint32) {
                return input;
            }
            // FIXME: revise this
            if (input instanceof Byte || input instanceof Short || input instanceof Integer || input instanceof Long
                    || input instanceof Uint8 || input instanceof Uint16 || input instanceof Uint64) {
                return Uint32.valueOf(input.longValue());
            }

            return Uint32.valueOf(input.toString());
        });
        b.put(Uint64.class, input -> {
            if (input instanceof Uint64) {
                return input;
            }
            // FIXME: revise this
            if (input instanceof Byte || input instanceof Short || input instanceof Integer || input instanceof Long
                    || input instanceof Uint8 || input instanceof Uint16 || input instanceof Uint32) {
                return Uint64.valueOf(input.longValue());
            }

            return Uint64.valueOf(input.toString());
        });
        CONVERTERS = b.build();
    }

    private NumberUtil() {
        // Hidden on purpose
    }

    @SuppressWarnings("unchecked")
    static <T extends Number> Function<Number, T> converterTo(final Class<T> clazz) {
        return (Function<Number, T>) CONVERTERS.get(clazz);
    }

    static boolean isRangeCovered(final Number min, final Number max, final Number superMin, final Number superMax) {
        return NumberUtil.NUMBER_COMPARATOR.compare(min, superMin) >= 0
            && NumberUtil.NUMBER_COMPARATOR.compare(max, superMax) <= 0;
    }
}
