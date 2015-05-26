/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.java.api.generator;

import java.util.Collection;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;

abstract class AbstractBigRangeGenerator<T extends Number & Comparable<T>> extends AbstractRangeGenerator<T> {
    protected AbstractBigRangeGenerator(final Class<T> typeClass) {
        super(typeClass);
    }

    private String itemType() {
        final StringBuilder sb = new StringBuilder("com.google.common.collect.Range<");
        sb.append(getTypeName()).append('>');

        return sb.toString();
    }

    private String arrayType() {
        return new StringBuilder(itemType()).append("[]").toString();
    }

    @Override
    protected final String generateRangeCheckerImplementation(final String checkerName, @Nonnull final Collection<RangeConstraint> restrictions) {
        final StringBuilder sb = new StringBuilder();

        // Field to hold the Range objects in an array
        sb.append("private static final ").append(arrayType()).append(' ').append(checkerName).append(";\n");

        // Static initializer block for the array
        sb.append("static {\n");
        sb.append("    @SuppressWarnings(\"unchecked\")\n");
        sb.append("    final ").append(arrayType()).append(" a = (").append(arrayType())
        .append(") java.lang.reflect.Array.newInstance(com.google.common.collect.Range.class, ").append(restrictions.size()).append(");\n");

        int i = 0;
        for (RangeConstraint r : restrictions) {
            final String min = format(getValue(r.getMin()));
            final String max = format(getValue(r.getMax()));

            sb.append("    a[").append(i++).append("] = com.google.common.collect.Range.closed(").append(min).append(", ").append(max).append(");\n");
        }

        sb.append("    ").append(checkerName).append(" = a;\n");
        sb.append("}\n\n");

        // Static enforcement method
        sb.append("private static void ").append(checkerName).append("(final ").append(getTypeName()).append(" value) {\n");
        sb.append("    for (").append(itemType()).append(" r : ").append(checkerName).append(") {\n");
        sb.append("        if (r.contains(value)) {\n");
        sb.append("            return;\n");
        sb.append("        }\n");
        sb.append("    }\n");
        sb.append("\n");
        sb.append("    throw new IllegalArgumentException(String.format(\"Invalid value %s, expected: %s.\", value, java.util.Arrays.asList(").append(checkerName).append(")));\n");
        sb.append("}\n\n");

        return sb.toString();
    }
}
