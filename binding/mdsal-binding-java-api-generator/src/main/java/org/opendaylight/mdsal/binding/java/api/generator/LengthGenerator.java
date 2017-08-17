/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import com.google.common.collect.Range;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class LengthGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(LengthGenerator.class);

    private LengthGenerator() {
        throw new UnsupportedOperationException();
    }

    private static String lengthCheckerName(final String member) {
        return "check" + member + "Length";
    }

    private static Collection<String> createExpressions(final Collection<LengthConstraint> constraints) {
        final Collection<String> ret = new ArrayList<>(constraints.size());

        for (LengthConstraint l : constraints) {
            // We have to deal with restrictions being out of integer's range
            final long min = l.getMin().longValue();
            final long max = l.getMax().longValue();

            if (min > 0 || max < Integer.MAX_VALUE) {
                final StringBuilder sb = new StringBuilder("length >");
                if (min <= Integer.MAX_VALUE) {
                    sb.append('=');
                }
                sb.append(' ').append(min);

                if (max < Integer.MAX_VALUE) {
                    sb.append(" && length <= ").append(max);
                }

                ret.add(sb.toString());
            } else {
                // This range is implicitly capped by String/byte[] length returns
                LOG.debug("Constraint {} implied by int type value domain, skipping", l);
            }
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

    private static String generateArrayLengthChecker(final String member,
            final Collection<LengthConstraint> constraints) {
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

    private static String generateStringLengthChecker(final String member,
            final Collection<LengthConstraint> constraints) {
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

    static String generateLengthChecker(final String member, final Type type,
            final Collection<LengthConstraint> constraints) {
        return TypeUtils.getBaseYangType(type).getName().indexOf('[') != -1
                ? generateArrayLengthChecker(member, constraints) : generateStringLengthChecker(member, constraints);
    }

    static String generateLengthCheckerCall(@Nullable final String member, @Nonnull final String valueReference) {
        return lengthCheckerName(member) + '(' + valueReference + ");\n";
    }
}
