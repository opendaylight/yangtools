/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.binding.codegen.JavaFileTemplate.CODEHELPERS;

import java.util.ArrayList;
import java.util.Collection;
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
        final var constraints = constraint.getAllowedRanges().asRanges();
        final var ret = new ArrayList<String>(constraints.size());

        for (var range : constraints) {
            // We have to deal with restrictions being out of integer's range
            final var expr = createExpression(range.lowerEndpoint(), range.upperEndpoint());
            if (expr == null) {
                // This range is implicitly capped by String/byte[] length returns
                LOG.debug("Constraint {} implied by int type value domain, skipping", range);
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
            final LengthConstraint constraint, final GeneratedClass javaClass) {
        final var expressions = createExpressions(constraint);

        final var bb = javaClass.newBlockBuilder()
            .str("private static void ").str(lengthCheckerName(member)).str("(final byte[] value)").oB();

        if (!expressions.isEmpty()) {
            bb.eol("    final int length = value.length;");

            for (var exp : expressions) {
                bb.str("    if (").str(exp).str(")").oB();
                bb.eol("        return;");
                bb.str("    ").cB();
            }

            bb.str("    ").str(javaClass.getReferenceString(CODEHELPERS)).str(".throwInvalidLength(")
                .jStr(createLengthString(constraint)).eol(", value);");
        }

        return bb.cB();
    }

    private static @NonNull BlockBuilder generateStringLengthChecker(final String member,
            final LengthConstraint constraint, final GeneratedClass javaClass) {
        final var bb = javaClass.newBlockBuilder()
            .str("private static void ").str(lengthCheckerName(member)).str("(final ")
                .str(javaClass.getReferenceString(Types.STRING)).str(" value)").oB();

        final var expressions = createExpressions(constraint);
        if (!expressions.isEmpty()) {
            bb.eol("    final int length = value.codePointCount(0, value.length());");

            for (var exp : expressions) {
                bb
                    .str("    if (").str(exp).str(")").oB()
                    .eol("        return;")
                    .str("    ").cB();
            }

            bb.str("    ").str(javaClass.getReferenceString(CODEHELPERS)).str(".throwInvalidLength(")
                .jStr(createLengthString(constraint)).eol(", value);");
        }

        return bb.cB();
    }

    static @NonNull BlockBuilder generateLengthChecker(final String member, final @NonNull Type type,
            final LengthConstraint constraint, final @NonNull GeneratedClass javaClass) {
        return TypeUtils.getBaseYangType(type).isArray()
                ? generateArrayLengthChecker(member, constraint, javaClass)
                : generateStringLengthChecker(member, constraint, javaClass);
    }

    @NonNullByDefault
    static void appendCheckerCall(final BlockBuilder bb, final String member, final String valueReference) {
        bb.str(lengthCheckerName(member)).str("(").str(requireNonNull(valueReference)).eol(");");
    }
}
