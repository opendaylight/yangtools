/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.type;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableMap;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.function.Function;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

final class NumberUtil {
    private static final Comparator<Number> NUMBER_COMPARATOR = (o1, o2) -> {
        checkArgument(o1.getClass().equals(o2.getClass()), "Incompatible Number classes %s and %s",
            o1.getClass(), o2.getClass());

        if (o1 instanceof Byte) {
            return ((Byte)o1).compareTo((Byte) o2);
        } else if (o1 instanceof Short) {
            return ((Short)o1).compareTo((Short) o2);
        } else if (o1 instanceof Integer) {
            return ((Integer)o1).compareTo((Integer) o2);
        } else if (o1 instanceof Long) {
            return ((Long)o1).compareTo((Long) o2);
        } else if (o1 instanceof Uint8) {
            return ((Uint8)o1).compareTo((Uint8) o2);
        } else if (o1 instanceof Uint16) {
            return ((Uint16)o1).compareTo((Uint16) o2);
        } else if (o1 instanceof Uint32) {
            return ((Uint32)o1).compareTo((Uint32) o2);
        } else if (o1 instanceof Uint64) {
            return ((Uint64)o1).compareTo((Uint64) o2);
        } else if (o1 instanceof BigDecimal) {
            return ((BigDecimal)o1).compareTo((BigDecimal) o2);
        } else {
            throw new IllegalArgumentException("Unsupported Number class " + o1.getClass());
        }
    };

    private static final ImmutableMap<Class<? extends Number>, Function<Number, Number>> CONVERTERS;

    static {
        final ImmutableMap.Builder<Class<? extends Number>, Function<Number, Number>> b = ImmutableMap.builder();
        b.put(Byte.class, input -> {
            if (input instanceof Byte) {
                return input;
            }

            return Byte.valueOf(input.toString());
        });
        b.put(Short.class, input -> {
            if (input instanceof Short) {
                return input;
            }
            if (input instanceof Byte) {
                return input.shortValue();
            }

            return Short.valueOf(input.toString());
        });
        b.put(Integer.class, input -> {
            if (input instanceof Integer) {
                return input;
            }
            if (input instanceof Byte || input instanceof Short) {
                return input.intValue();
            }

            return Integer.valueOf(input.toString());
        });
        b.put(Long.class, input ->  {
            if (input instanceof Long) {
                return input;
            }
            if (input instanceof Byte || input instanceof Short || input instanceof Integer) {
                return input.longValue();
            }

            return Long.valueOf(input.toString());
        });
        b.put(BigDecimal.class, input -> {
            if (input instanceof BigDecimal) {
                return input;
            }
            if (input instanceof Byte || input instanceof Short || input instanceof Integer || input instanceof Long) {
                return BigDecimal.valueOf(input.longValue());
            }

            return new BigDecimal(input.toString());
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
