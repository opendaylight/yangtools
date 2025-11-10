/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import com.google.common.collect.Range;
import java.lang.reflect.Array;
import java.util.Locale;
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNull;
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
     * {@code java.lang.reflect.Array} as a JavaTypeName.
     */
    private static final @NonNull JavaTypeName ARRAY = JavaTypeName.create(Array.class);
    /**
     * {@code org.opendaylight.yangtools.yang.common.Decimal64} as a JavaTypeName.
     */
    private static final @NonNull JavaTypeName DECIMAL64 = JavaTypeName.create(Decimal64.class);
    /**
     * {@code com.google.common.collect.Range} as a JavaTypeName.
     */
    private static final @NonNull JavaTypeName RANGE = JavaTypeName.create(Range.class);

    Decimal64RangeGenerator() {
        super(Decimal64.class);
    }

    private static String range(final Function<JavaTypeName, String> classImporter) {
        return classImporter.apply(RANGE);
    }

    private static String itemType(final Function<JavaTypeName, String> classImporter) {
        return range(classImporter) + '<' + classImporter.apply(DECIMAL64) + '>';
    }

    private static String arrayType(final Function<JavaTypeName, String> classImporter) {
        return itemType(classImporter) + "[]";
    }

    private static String format(final Function<JavaTypeName, String> classImporter, final Decimal64 value) {
        return classImporter.apply(DECIMAL64) +  ".of(" + value.scale() + ", " + value.unscaledValue() + "L)";
    }

    @Override
    @Deprecated
    protected Decimal64 convert(final Number value) {
        if (value instanceof Byte || value instanceof Short || value instanceof Integer
            || value instanceof Uint8 || value instanceof Uint16) {
            // FIXME: this is not quite right
            return Decimal64.valueOf(1, value.intValue());
        }
        // FIXME: this is not quite right
        return Decimal64.valueOf(1, value.longValue());
    }

    @Override
    protected String generateRangeCheckerImplementation(final String checkerName,
            final RangeConstraint<?> constraint, final Function<JavaTypeName, String> classImporter) {
        final var constraints = constraint.getAllowedRanges().asRanges();
        final var fieldName = checkerName.toUpperCase(Locale.ENGLISH) + "_RANGES";
        final var sb = new StringBuilder();

        // Field to hold the Range objects in an array
        sb.append("private static final ").append(arrayType(classImporter)).append(' ').append(fieldName).append(";\n");

        // Static initializer block for the array
        sb.append("static {\n");
        sb.append("    @SuppressWarnings(\"unchecked\")\n");
        sb.append("    final ").append(arrayType(classImporter)).append(" a = (").append(arrayType(classImporter))
            .append(") ").append(classImporter.apply(ARRAY)).append(".newInstance(").append(range(classImporter))
            .append(".class, ").append(constraints.size()).append(");\n");

        int offset = 0;
        for (var range : constraints) {
            final var min = format(classImporter, getValue(range.lowerEndpoint()));
            final var max = format(classImporter, getValue(range.upperEndpoint()));

            sb.append("    a[").append(offset++).append("] = ").append(range(classImporter)).append(".closed(")
                .append(min).append(", ").append(max).append(");\n");
        }

        sb.append("    ").append(fieldName).append(" = a;\n");
        sb.append("}\n");

        // Static enforcement method
        sb.append("private static void ").append(checkerName).append("(final ").append(getTypeName())
            .append(" value) {\n");
        sb.append("    for (").append(itemType(classImporter)).append(" r : ").append(fieldName).append(") {\n");
        sb.append("        if (r.contains(value)) {\n");
        sb.append("            return;\n");
        sb.append("        }\n");
        sb.append("    }\n");

        sb.append("    ").append(classImporter.apply(JavaFileTemplate.CODEHELPERS)).append(".throwInvalidRange(")
            .append(fieldName).append(", value);\n");
        sb.append("}\n");

        return sb.toString();
    }
}
