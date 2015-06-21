/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.java.api.generator;

import com.google.common.collect.Range;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;

final class LengthGenerator {
    private LengthGenerator() {
        throw new UnsupportedOperationException();
    }

    private static String lengthCheckerName(final String member) {
        return "check" + member + "Length";
    }

    private static Collection<String> createExpressions(final Collection<LengthConstraint> constraints) {
        final Collection<String> ret = new ArrayList<>(constraints.size());

        for (LengthConstraint l : constraints) {
            final StringBuilder sb = new StringBuilder("length >");

            // We have to deal with restrictions being out of integer's range
            if (l.getMin().longValue() <= Integer.MAX_VALUE) {
                sb.append('=');
            }
            sb.append(' ').append(l.getMin().intValue());

            final int max = l.getMax().intValue();
            if (max < Integer.MAX_VALUE) {
                sb.append(" && length <= ").append(max);
            }

            ret.add(sb.toString());
        }

        return ret;
    }

    private static String createLengthString(final Collection<LengthConstraint> constraints) {
        final List<Range<BigInteger>> ranges = new ArrayList<>(constraints.size());

        for (LengthConstraint c : constraints) {
            ranges.add(Range.closed(new BigInteger(c.getMin().toString()), new BigInteger(c.getMax().toString())));
        }

        return ranges.toString();
    }

    private static String generateArrayLengthChecker(final String member, final Collection<LengthConstraint> constraints) {
        final StringBuilder sb = new StringBuilder();
        final Collection<String> expressions = createExpressions(constraints);

        sb.append("private static void ").append(lengthCheckerName(member)).append("(final byte[] value) {\n");

        if (!expressions.isEmpty()) {
            sb.append("    final int length = value.length;\n");

            for (String exp : expressions) {
                sb.append("    if (").append(exp).append(") {\n");
                sb.append("        return;\n");
                sb.append("    }\n");
            }

            sb.append("    throw new IllegalArgumentException(String.format(\"Invalid length: %s, expected: ")
              .append(createLengthString(constraints)).append(".\", java.util.Arrays.toString(value)));\n");
        }

        sb.append("}\n");

        return sb.toString();
    }

    private static String generateStringLengthChecker(final String member, final Collection<LengthConstraint> constraints) {
        final StringBuilder sb = new StringBuilder();
        final Collection<String> expressions = createExpressions(constraints);

        sb.append("private static void ").append(lengthCheckerName(member)).append("(final String value) {\n");

        if (!expressions.isEmpty()) {
            sb.append("    final int length = value.length();\n");

            for (String exp : expressions) {
                sb.append("    if (").append(exp).append(") {\n");
                sb.append("        return;\n");
                sb.append("    }\n");
            }

            sb.append("    throw new IllegalArgumentException(String.format(\"Invalid length: %s, expected: ")
              .append(createLengthString(constraints)).append(".\", value));\n");
        }

        sb.append("}\n");

        return sb.toString();
    }

    static String generateLengthChecker(final String member, final Type type, final Collection<LengthConstraint> constraints) {
        if (TypeUtils.getBaseYangType(type).getName().contains("[")) {
            return generateArrayLengthChecker(member, constraints);
        } else {
            return generateStringLengthChecker(member, constraints);
        }
    }

    static String generateLengthCheckerCall(@Nullable final String member, @Nonnull final String valueReference) {
        return lengthCheckerName(member) + '(' + valueReference + ");\n";
    }
}
