/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import com.google.common.collect.Range;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;

/**
 * Abstract base for generators which require instantiation of boundary values
 * to check. These are implemented by generating an array constant within the
 * class, which contains {@link Range} instances, which hold pre-instantiated
 * boundary values.
 *
 * @param <T> type of the class
 */
abstract class AbstractBigRangeGenerator<T extends Number & Comparable<T>> extends AbstractRangeGenerator<T> {
    private static final String RANGE = Range.class.getName();

    protected AbstractBigRangeGenerator(final Class<T> typeClass) {
        super(typeClass);
    }

    private StringBuilder itemType() {
        return new StringBuilder(RANGE).append('<').append(getTypeName()).append('>');
    }

    private StringBuilder arrayType() {
        return new StringBuilder(itemType()).append("[]");
    }

    @Override
    protected final String generateRangeCheckerImplementation(final String checkerName, @Nonnull final RangeConstraint<?> constraint) {
        final Set<? extends Range<? extends Number>> constraints = constraint.getAllowedRanges().asRanges();
        final String fieldName = checkerName.toUpperCase() + "_RANGES";
        final StringBuilder sb = new StringBuilder();

        // Field to hold the Range objects in an array
        sb.append("private static final ").append(arrayType()).append(' ').append(fieldName).append(";\n");

        // Static initializer block for the array
        sb.append("static {\n");
        sb.append("    @SuppressWarnings(\"unchecked\")\n");
        sb.append("    final ").append(arrayType()).append(" a = (").append(arrayType())
        .append(") java.lang.reflect.Array.newInstance(").append(RANGE).append(".class, ").append(constraints.size()).append(");\n");

        int i = 0;
        for (Range<? extends Number> r : constraints) {
            final String min = format(getValue(r.lowerEndpoint()));
            final String max = format(getValue(r.upperEndpoint()));

            sb.append("    a[").append(i++).append("] = ").append(RANGE).append(".closed(").append(min).append(", ").append(max).append(");\n");
        }

        sb.append("    ").append(fieldName).append(" = a;\n");
        sb.append("}\n");

        // Static enforcement method
        sb.append("private static void ").append(checkerName).append("(final ").append(getTypeName()).append(" value) {\n");
        sb.append("    for (").append(itemType()).append(" r : ").append(fieldName).append(") {\n");
        sb.append("        if (r.contains(value)) {\n");
        sb.append("            return;\n");
        sb.append("        }\n");
        sb.append("    }\n");
        sb.append("    throw new IllegalArgumentException(String.format(\"Invalid range: %s, expected: %s.\", value, java.util.Arrays.asList(").append(fieldName).append(")));\n");
        sb.append("}\n");

        return sb.toString();
    }
}
