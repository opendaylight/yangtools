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

import com.google.common.collect.Range;
import java.util.ArrayList;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractPrimitiveRangeGenerator<T extends Number & Comparable<T>> extends AbstractRangeGenerator<T> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractPrimitiveRangeGenerator.class);
    private final @NonNull String primitiveName;
    private final @NonNull T minValue;
    private final @NonNull T maxValue;

    AbstractPrimitiveRangeGenerator(final Class<T> typeClass, final String primitiveName, final T minValue,
            final T maxValue) {
        super(typeClass);
        this.primitiveName = requireNonNull(primitiveName);
        this.minValue = requireNonNull(minValue);
        this.maxValue = requireNonNull(maxValue);
    }

    /**
     * {@return the name of the primitive type, as known by the Java language}
     */
    final @NonNull String primitiveName() {
        return primitiveName;
    }

    private boolean needsMaximumEnforcement(final T maxToEnforce) {
        return maxValue.compareTo(maxToEnforce) > 0;
    }

    private boolean needsMinimumEnforcement(final T minToEnforce) {
        return minValue.compareTo(minToEnforce) < 0;
    }

    private Collection<String> createExpressions(final RangeConstraint<?> constraint, final GeneratedClass javaClass) {
        final var constraints = constraint.getAllowedRanges().asRanges();
        final var ret = new ArrayList<String>(constraints.size());

        for (var r : constraints) {
            final var min = getValue(r.lowerEndpoint());
            final var needMin = needsMinimumEnforcement(min);

            final var max = getValue(r.upperEndpoint());
            final var needMax = needsMaximumEnforcement(max);

            if (!needMin && !needMax) {
                LOG.debug("Type {} indicates [{}, {}] does not require enforcement", getTypeClass().getName(), min,
                    max);
                continue;
            }

            final var sb = new StringBuilder();
            if (needMin) {
                appendMinCheck(sb, min, javaClass);
            }
            if (needMax) {
                if (needMin) {
                    sb.append(" && ");
                }
                appendMaxCheck(sb, max, javaClass);
            }

            ret.add(sb.toString());
        }

        return ret;
    }

    void appendMaxCheck(final StringBuilder sb, final T max, final GeneratedClass javaClass) {
        sb.append("value <= ").append(format(max));
    }

    void appendMinCheck(final StringBuilder sb, final T min, final GeneratedClass javaClass) {
        sb.append("value >= ").append(format(min));
    }

    /**
     * Format a value into a Java-compilable expression which results in the appropriate type.
     *
     * @param value Number value
     * @return Java language string representation
     */
    abstract @NonNull String format(T value);

    /**
     * {@return the {@link org.opendaylight.yangtools.binding.lib.CodeHelpers} {@code throwInvalidRange} variant
     *          to call}
     */
    @NonNull String codeHelpersThrow() {
        return "throwInvalidRange";
    }

    private String createRangeString(final RangeConstraint<?> constraint) {
        final var constraints = constraint.getAllowedRanges().asRanges();
        final var ranges = new ArrayList<Range<T>>(constraints.size());

        for (var c : constraints) {
            ranges.add(Range.closed(getValue(c.lowerEndpoint()), getValue(c.upperEndpoint())));
        }

        return ranges.toString();
    }

    @Override
    final BlockBuilder generateRangeCheckerImplementation(final String checkerName,
            final RangeConstraint<?> constraints, final GeneratedClass javaClass) {
        return javaClass.newBlockBuilder()
            .str("private static void ").str(checkerName).str("(final ").str(primitiveName).str(" value)")
            .jBlock(bb -> {
                final var expressions = createExpressions(constraints, javaClass);
                if (!expressions.isEmpty()) {
                    for (var exp : expressions) {
                        bb
                            .str("    if (").str(exp).str(")").oB()
                            .eol("        return;")
                            .str("    ").cB();
                    }
                    bb.str("    ").str(javaClass.getReferenceString(CODEHELPERS)).str(".").str(codeHelpersThrow())
                        .str("(").jStr(createRangeString(constraints)).eol(", value);");
                }
            }).nl();
    }
}
