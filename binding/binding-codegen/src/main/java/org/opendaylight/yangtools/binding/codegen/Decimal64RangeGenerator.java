/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import com.google.common.collect.Range;
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.model.api.Decimal64Type;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.yang.common.Decimal64;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;

/**
 * Decimal64 boundary check generator. It requires instantiation of boundary values -- these are implemented by
 * generating an array constant within the class, which contains {@link Range} instances, which hold pre-instantiated
 * boundary values.
 */
final class Decimal64RangeGenerator extends AbstractRangeGenerator<Decimal64> {
    /**
     * {@code org.opendaylight.yangtools.yang.common.Decimal64} as a JavaTypeName.
     */
    private static final @NonNull JavaTypeName DECIMAL64 = JavaTypeName.create(Decimal64.class);

    private final int fractionDigits;

    private Decimal64RangeGenerator(final int fractionDigits) {
        super(Decimal64.class);
        this.fractionDigits = fractionDigits;
    }

    Decimal64RangeGenerator(final Decimal64Type type) {
        this(type.fractionDigits());
    }

    Decimal64RangeGenerator(final Decimal64 value) {
        this(value.scale());
    }

    @Override
    @Deprecated
    protected Decimal64 convert(final Number value) {
        if (value instanceof Byte || value instanceof Short || value instanceof Integer
            || value instanceof Uint8 || value instanceof Uint16) {
            return Decimal64.valueOf(fractionDigits, value.intValue());
        }
        return Decimal64.valueOf(fractionDigits, value.longValue());
    }

    @Override
    protected String generateRangeCheckerImplementation(final String checkerName,
            final RangeConstraint<?> constraint, final Function<JavaTypeName, String> classImporter) {
        final var constraints = constraint.getAllowedRanges().asRanges();
        final var codeHelpers = classImporter.apply(JavaFileTemplate.CODEHELPERS);

        final var sb = new StringBuilder();
        sb.append("private static void ").append(checkerName).append("(final ").append(classImporter.apply(DECIMAL64))
            .append(" value) {\n");
        sb.append("    final var unscaled = ").append(codeHelpers).append(".checkScale(value, ").append(fractionDigits)
            .append(");\n");

        final var msg = new StringBuilder().append("\"[");
        final var it = constraints.iterator();
        while (true) {
            final var range = it.next();
            final var min = getValue(range.lowerEndpoint());
            final var max = getValue(range.upperEndpoint());
            msg.append('[').append(min).append("..").append(max).append(']');

            sb.append("    if (unscaled >= ").append(min.unscaledValue()).append("L && unscaled <= ")
                .append(max.unscaledValue()).append("L) {\n");
            sb.append("        return;\n");
            sb.append("    }\n");

            if (!it.hasNext()) {
                break;
            }
            msg.append(", ");
        }

        sb.append("    ").append(codeHelpers).append(".throwInvalidRange(").append(msg).append("]\", value);\n");
        sb.append("}\n");

        return sb.toString();
    }
}
