/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import com.google.common.collect.Range;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.binding.CodeHelpers;
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

    private static Collection<String> createExpressions(final LengthConstraint constraint) {
        final Set<Range<Integer>> constraints = constraint.getAllowedRanges().asRanges();
        final Collection<String> ret = new ArrayList<>(constraints.size());

        for (Range<Integer> l : constraints) {
            // We have to deal with restrictions being out of integer's range
            final String expr = createExpression(l.lowerEndpoint().intValue(), l.upperEndpoint().intValue());
            if (expr == null) {
                // This range is implicitly capped by String/byte[] length returns
                LOG.debug("Constraint {} implied by int type value domain, skipping", l);
            } else {
                ret.add(expr);
            }
        }

        return ret;
    }

    private static @Nullable String createExpression(final int min, final int max) {
        if (min == max) {
            return min < Integer.MAX_VALUE ? "length == " + min : null;
        }
        if (min > 0) {
            final StringBuilder sb = new StringBuilder("length >= ").append(min);
            if (max < Integer.MAX_VALUE) {
                sb.append(" && length <= ").append(max);
            }
            return sb.toString();
        }

        return max < Integer.MAX_VALUE ? "length <= " + max : null;
    }

    private static String createLengthString(final LengthConstraint constraint) {
        return new ArrayList<>(constraint.getAllowedRanges().asRanges()).toString();
    }

    private static String generateArrayLengthChecker(final String member, final LengthConstraint constraint,
            final JavaFileTemplate template) {
        final StringBuilder sb = new StringBuilder();
        final Collection<String> expressions = createExpressions(constraint);

        sb.append("private static void ").append(lengthCheckerName(member)).append("(final byte[] value) {\n");

        if (!expressions.isEmpty()) {
            sb.append("    final int length = value.length;\n");

            for (String exp : expressions) {
                sb.append("    if (").append(exp).append(") {\n");
                sb.append("        return;\n");
                sb.append("    }\n");
            }

            sb.append("    ").append(template.importedName(CodeHelpers.class)).append(".throwInvalidLength(\"")
            .append(createLengthString(constraint)).append("\", value);\n");
        }

        return sb.append("}\n").toString();
    }

    private static String generateStringLengthChecker(final String member, final LengthConstraint constraint,
            final JavaFileTemplate template) {
        final StringBuilder sb = new StringBuilder();
        final Collection<String> expressions = createExpressions(constraint);

        sb.append("private static void ").append(lengthCheckerName(member)).append("(final String value) {\n");

        if (!expressions.isEmpty()) {
            sb.append("    final int length = value.length();\n");

            for (String exp : expressions) {
                sb.append("    if (").append(exp).append(") {\n");
                sb.append("        return;\n");
                sb.append("    }\n");
            }

            sb.append("    ").append(template.importedName(CodeHelpers.class)).append(".throwInvalidLength(\"")
            .append(createLengthString(constraint)).append("\", value);\n");
        }

        return sb.append("}\n").toString();
    }

    static String generateLengthChecker(final String member, final Type type,
            final LengthConstraint constraint, final JavaFileTemplate template) {
        return TypeUtils.getBaseYangType(type).getName().indexOf('[') != -1
                ? generateArrayLengthChecker(member, constraint, template)
                        : generateStringLengthChecker(member, constraint, template);
    }

    static String generateLengthCheckerCall(final @Nullable String member, final @NonNull String valueReference) {
        return lengthCheckerName(member) + '(' + valueReference + ");\n";
    }
}
