/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

final class NumberUtil {
    private static final Map<Class<? extends Number>, Function<Number, Number>> CONVERTERS;
    static {
        final ImmutableMap.Builder<Class<? extends Number>, Function<Number, Number>> b = ImmutableMap.builder();
        b.put(Byte.class, new Function<Number, Number>() {
            @Override
            public Number apply(final Number input) {
                if (input instanceof Byte) {
                    return input;
                }

                return Byte.valueOf(input.toString());
            }
        });
        b.put(Short.class, new Function<Number, Number>() {
            @Override
            public Number apply(final Number input) {
                if (input instanceof Short) {
                    return input;
                }
                if (input instanceof Byte) {
                    return input.shortValue();
                }

                return Short.valueOf(input.toString());
            }
        });
        b.put(Integer.class, new Function<Number, Number>() {
            @Override
            public Number apply(final Number input) {
                if (input instanceof Integer) {
                    return input;
                }
                if (input instanceof Byte || input instanceof Short) {
                    return input.intValue();
                }

                return Integer.valueOf(input.toString());
            }
        });
        b.put(Long.class, new Function<Number, Number>() {
            @Override
            public Number apply(final Number input) {
                if (input instanceof Long) {
                    return input;
                }
                if (input instanceof Byte || input instanceof Short || input instanceof Integer) {
                    return input.longValue();
                }

                return Long.valueOf(input.toString());
            }
        });
        b.put(BigDecimal.class, new Function<Number, Number>() {
            @Override
            public Number apply(final Number input) {
                if (input instanceof BigDecimal) {
                    return input;
                }
                if (input instanceof Byte || input instanceof Short ||
                        input instanceof Integer || input instanceof Long) {
                    return BigDecimal.valueOf(input.longValue());
                }

                return new BigDecimal(input.toString());
            }
        });
        b.put(BigInteger.class, new Function<Number, Number>() {
            @Override
            public Number apply(final Number input) {
                if (input instanceof BigInteger) {
                    return input;
                }
                if (input instanceof Byte || input instanceof Short ||
                        input instanceof Integer || input instanceof Long) {
                    return BigInteger.valueOf(input.longValue());
                }

                return new BigInteger(input.toString());
            }
        });
        CONVERTERS = b.build();
    }

    private NumberUtil() {
        throw new UnsupportedOperationException();
    }

    static Function<Number, Number> converterTo(final Class<? extends Number> clazz) {
        return CONVERTERS.get(clazz);
    }
}
