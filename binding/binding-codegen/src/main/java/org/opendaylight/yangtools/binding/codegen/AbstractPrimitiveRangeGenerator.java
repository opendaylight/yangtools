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
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.lib.CodeHelpers;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
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
     * Return the name of the primitive type, as known by the Java language.
     *
     * @return Primitive type name
     */
    protected final @NonNull String getPrimitiveName() {
        return primitiveName;
    }

    private boolean needsMaximumEnforcement(final T maxToEnforce) {
        return maxValue.compareTo(maxToEnforce) > 0;
    }

    private boolean needsMinimumEnforcement(final T minToEnforce) {
        return minValue.compareTo(minToEnforce) < 0;
    }

    private Collection<String> createExpressions(final RangeConstraint<?> constraint,
            final Function<JavaTypeName, String> classImporter) {
        final var constraints = constraint.getAllowedRanges().asRanges();
        final var ret = new ArrayList<String>(constraints.size());

        for (var r : constraints) {
            final var min = getValue(r.lowerEndpoint());
            final var needMin = needsMinimumEnforcement(min);

            final var max = getValue(r.upperEndpoint());
            final var needMax = needsMaximumEnforcement(max);

            if (!needMin && !needMax) {
                LOG.debug("Type {} indicates [{}, {}] does not require enforcement", getTypeName(), min, max);
                continue;
            }

            final var sb = new StringBuilder();
            if (needMin) {
                appendMinCheck(sb, min, classImporter);
            }
            if (needMax) {
                if (needMin) {
                    sb.append(" && ");
                }
                appendMaxCheck(sb, max, classImporter);
            }

            ret.add(sb.toString());
        }

        return ret;
    }

    void appendMaxCheck(final StringBuilder sb, final T max, final Function<JavaTypeName, String> classImporter) {
        sb.append("value <= ").append(format(max));
    }

    void appendMinCheck(final StringBuilder sb, final T min, final Function<JavaTypeName, String> classImporter) {
        sb.append("value >= ").append(format(min));
    }

    /**
     * Format a value into a Java-compilable expression which results in the appropriate
     * type.
     *
     * @param value Number value
     * @return Java language string representation
     */
    protected abstract @NonNull String format(T value);

    /**
     * {@return the {@link CodeHelpers} {@code throwInvalidRange} variant to call}
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
    protected final String generateRangeCheckerImplementation(final String checkerName,
            final RangeConstraint<?> constraints, final Function<JavaTypeName, String> classImporter) {
        final var sb = new StringBuilder();
        final var expressions = createExpressions(constraints, classImporter);

        sb.append("private static void ").append(checkerName).append("(final ").append(primitiveName)
            .append(" value) {\n");

        if (!expressions.isEmpty()) {
            for (var exp : expressions) {
                sb.append("    if (").append(exp).append(") {\n");
                sb.append("        return;\n");
                sb.append("    }\n");
            }

            sb.append("    ").append(classImporter.apply(JavaFileTemplate.CODEHELPERS))
                .append('.').append(codeHelpersThrow()).append("(\"").append(createRangeString(constraints))
                .append("\", value);\n");
        }

        return sb.append("}\n").toString();
    }
}
