/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Range;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.ri.Types;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class LengthGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(LengthGenerator.class);

    private LengthGenerator() {
        // Hidden on purpose
    }

    @NonNullByDefault
    private static String lengthCheckerName(final String member) {
        return "check" + member + "Length";
    }

    private static Collection<String> createExpressions(final LengthConstraint constraint) {
        final Set<Range<Integer>> constraints = constraint.getAllowedRanges().asRanges();
        final Collection<String> ret = new ArrayList<>(constraints.size());

        for (Range<Integer> l : constraints) {
            // We have to deal with restrictions being out of integer's range
            final String expr = createExpression(l.lowerEndpoint(), l.upperEndpoint());
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

    private static @NonNull BlockBuilder generateArrayLengthChecker(final String member,
            final LengthConstraint constraint, final JavaFileTemplate template) {
        final var expressions = createExpressions(constraint);

        final var bb = new BlockBuilder()
            .str("private static void ").str(lengthCheckerName(member)).str("(final byte[] value)").oB();

        if (!expressions.isEmpty()) {
            bb.eol("    final int length = value.length;");

            for (var exp : expressions) {
                bb.str("    if (").str(exp).str(")").oB();
                bb.eol("        return;");
                bb.str("    ").cB();
            }

            bb.str("    ").str(template.importedName(JavaFileTemplate.CODEHELPERS)).str(".throwInvalidLength(")
                .quoted(createLengthString(constraint)).eol(", value);");
        }

        return bb.cB();
    }

    private static @NonNull BlockBuilder generateStringLengthChecker(final String member,
            final LengthConstraint constraint, final JavaFileTemplate template) {
        final var bb = new BlockBuilder()
            .str("private static void ").str(lengthCheckerName(member)).str("(final ")
                .str(template.importedName(Types.STRING)).str(" value)").oB();

        final var expressions = createExpressions(constraint);
        if (!expressions.isEmpty()) {
            bb.eol("    final int length = value.codePointCount(0, value.length());");

            for (var exp : expressions) {
                bb
                    .str("    if (").str(exp).str(")").oB()
                    .eol("        return;")
                    .str("    ").cB();
            }

            bb.str("    ").str(template.importedName(JavaFileTemplate.CODEHELPERS)).str(".throwInvalidLength(")
                .quoted(createLengthString(constraint)).eol(", value);");
        }

        return bb.cB();
    }

    static @NonNull BlockBuilder generateLengthChecker(final String member, final @NonNull Type type,
            final LengthConstraint constraint, final JavaFileTemplate template) {
        return JavaFileTemplate.isArrayType(TypeUtils.getBaseYangType(type))
                ? generateArrayLengthChecker(member, constraint, template)
                : generateStringLengthChecker(member, constraint, template);
    }

    @NonNullByDefault
    static void appendCheckerCall(final StringBuilder sb, final String member, final String valueReference) {
        sb.append(lengthCheckerName(member)).append('(').append(requireNonNull(valueReference)).append(");\n");
    }
}
