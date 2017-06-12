/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;

final class NumberUtil {
    private static final Comparator<Number> NUMBER_COMPARATOR = (o1, o2) -> {
        Preconditions.checkArgument(o1.getClass().equals(o2.getClass()), "Incompatible Number classes %s and %s",
            o1.getClass(), o2.getClass());

        if (o1 instanceof Byte) {
            return ((Byte)o1).compareTo((Byte) o2);
        } else if (o1 instanceof Short) {
            return ((Short)o1).compareTo((Short) o2);
        } else if (o1 instanceof Integer) {
            return ((Integer)o1).compareTo((Integer) o2);
        } else if (o1 instanceof Long) {
            return ((Long)o1).compareTo((Long) o2);
        } else if (o1 instanceof BigDecimal) {
            return ((BigDecimal)o1).compareTo((BigDecimal) o2);
        } else if (o1 instanceof BigInteger) {
            return ((BigInteger)o1).compareTo((BigInteger) o2);
        } else {
            throw new IllegalArgumentException("Unsupported Number class " + o1.getClass());
        }
    };

    private static final Map<Class<? extends Number>, Function<Number, Number>> CONVERTERS;

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
        b.put(BigInteger.class, input -> {
            if (input instanceof BigInteger) {
                return input;
            }
            if (input instanceof Byte || input instanceof Short ||
                    input instanceof Integer || input instanceof Long) {
                return BigInteger.valueOf(input.longValue());
            }

            return new BigInteger(input.toString());
        });
        CONVERTERS = b.build();
    }

    private NumberUtil() {
        throw new UnsupportedOperationException();
    }

    static Function<Number, Number> converterTo(final Class<? extends Number> clazz) {
        return CONVERTERS.get(clazz);
    }

    static boolean isRangeCovered(final Number min, final Number max, final Number superMin, final Number superMax) {
        return NumberUtil.NUMBER_COMPARATOR.compare(min, superMin) >= 0 &&
                NumberUtil.NUMBER_COMPARATOR.compare(max, superMax) <= 0;
    }
}
