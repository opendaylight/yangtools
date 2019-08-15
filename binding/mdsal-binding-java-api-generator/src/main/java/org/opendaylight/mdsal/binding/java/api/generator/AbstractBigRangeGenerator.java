/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import com.google.common.collect.Range;
import java.lang.reflect.Array;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;
import org.opendaylight.yangtools.yang.binding.CodeHelpers;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;

/**
 * Abstract base for generators which require instantiation of boundary values to check. These are implemented
 * by generating an array constant within the class, which contains {@link Range} instances, which hold pre-instantiated
 * boundary values.
 *
 * @param <T> type of the class
 */
abstract class AbstractBigRangeGenerator<T extends Number & Comparable<T>> extends AbstractRangeGenerator<T> {
    protected AbstractBigRangeGenerator(final Class<T> typeClass) {
        super(typeClass);
    }

    private static String range(final Function<Class<?>, String> classImporter) {
        return classImporter.apply(Range.class);
    }

    private String itemType(final Function<Class<?>, String> classImporter) {
        return range(classImporter) + '<' + getTypeName() + '>';
    }

    private String arrayType(final Function<Class<?>, String> classImporter) {
        return itemType(classImporter) + "[]";
    }

    @Override
    protected final String generateRangeCheckerImplementation(final String checkerName,
            final RangeConstraint<?> constraint, final Function<Class<?>, String> classImporter) {
        final Set<? extends Range<? extends Number>> constraints = constraint.getAllowedRanges().asRanges();
        final String fieldName = checkerName.toUpperCase(Locale.ENGLISH) + "_RANGES";
        final StringBuilder sb = new StringBuilder();

        // Field to hold the Range objects in an array
        sb.append("private static final ").append(arrayType(classImporter)).append(' ').append(fieldName).append(";\n");

        // Static initializer block for the array
        sb.append("static {\n");
        sb.append("    @SuppressWarnings(\"unchecked\")\n");
        sb.append("    final ").append(arrayType(classImporter)).append(" a = (").append(arrayType(classImporter))
            .append(") ").append(classImporter.apply(Array.class)).append(".newInstance(").append(range(classImporter))
            .append(".class, ").append(constraints.size()).append(");\n");

        int offset = 0;
        for (Range<? extends Number> r : constraints) {
            final String min = format(getValue(r.lowerEndpoint()));
            final String max = format(getValue(r.upperEndpoint()));

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

        sb.append("    ").append(classImporter.apply(CodeHelpers.class)).append(".throwInvalidRange(").append(fieldName)
            .append(", value);\n");
        sb.append("}\n");

        return sb.toString();
    }
}
