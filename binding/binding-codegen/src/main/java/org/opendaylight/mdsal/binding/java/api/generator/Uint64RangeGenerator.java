/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import java.util.function.Function;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

final class Uint64RangeGenerator extends AbstractUnsignedRangeGenerator<Uint64> {
    Uint64RangeGenerator() {
        super(Uint64.class, long.class.getName(), Uint64.ZERO, Uint64.MAX_VALUE);
    }

    @Override
    @Deprecated
    protected Uint64 convert(final Number value) {
        if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long
                || value instanceof Uint8 || value instanceof Uint16 || value instanceof Uint32) {
            return Uint64.valueOf(value.longValue());
        }
        return Uint64.valueOf(value.toString());
    }

    @Override
    protected String format(final Uint64 value) {
        throw new UnsupportedOperationException();
    }

    @Override
    String codeHelpersThrow() {
        return "throwInvalidRangeUnsigned";
    }

    @Override
    void appendMaxCheck(final StringBuilder sb, final Uint64 max, final Function<Class<?>, String> classImporter) {
        appendCompare(sb, classImporter, max, "<=");
    }

    @Override
    void appendMinCheck(final StringBuilder sb, final Uint64 min, final Function<Class<?>, String> classImporter) {
        appendCompare(sb, classImporter, min, ">=");
    }

    private static StringBuilder appendCompare(final StringBuilder sb, final Function<Class<?>, String> classImporter,
            final Uint64 val, final String operator) {
        return sb.append(classImporter.apply(Long.class)).append(".compareUnsigned(value, ").append(val.longValue())
                .append("L) ").append(operator).append(" 0");
    }
}
