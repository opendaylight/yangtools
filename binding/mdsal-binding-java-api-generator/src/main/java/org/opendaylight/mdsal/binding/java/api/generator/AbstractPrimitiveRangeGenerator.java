/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.binding.CodeHelpers;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractPrimitiveRangeGenerator<T extends Number & Comparable<T>> extends AbstractRangeGenerator<T> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractPrimitiveRangeGenerator.class);
    private final String primitiveName;
    private final T minValue;
    private final T maxValue;

    protected AbstractPrimitiveRangeGenerator(final Class<T> typeClass, final String primitiveName, final T minValue, final T maxValue) {
        super(typeClass);
        this.primitiveName = Preconditions.checkNotNull(primitiveName);
        this.minValue = Preconditions.checkNotNull(minValue);
        this.maxValue = Preconditions.checkNotNull(maxValue);
    }

    /**
     * Return the name of the primitive type, as known by the Java language.
     *
     * @return Primitive type name
     */
    @Nonnull protected final String getPrimitiveName() {
        return primitiveName;
    }

    private boolean needsMaximumEnforcement(final T maxToEnforce) {
        return maxValue.compareTo(maxToEnforce) > 0;
    }

    private boolean needsMinimumEnforcement(final T minToEnforce) {
        return minValue.compareTo(minToEnforce) < 0;
    }

    private Collection<String> createExpressions(final RangeConstraint<?> constraint) {
        final Set<? extends Range<? extends Number>> constraints = constraint.getAllowedRanges().asRanges();
        final Collection<String> ret = new ArrayList<>(constraints.size());

        for (Range<? extends Number> r : constraints) {
            final T min = getValue(r.lowerEndpoint());
            final boolean needMin = needsMinimumEnforcement(min);

            final T max = getValue(r.upperEndpoint());
            final boolean needMax = needsMaximumEnforcement(max);

            if (!needMin && !needMax) {
                LOG.debug("Type {} indicates [{}, {}] does not require enforcement", getTypeName(), min, max);
                continue;
            }

            final StringBuilder sb = new StringBuilder();
            if (needMin) {
                sb.append("value >= ").append(format(min));
            }
            if (needMax) {
                if (needMin) {
                    sb.append(" && ");
                }
                sb.append("value <= ").append(format(max));
            }

            ret.add(sb.toString());
        }

        return ret;
    }

    private String createRangeString(final RangeConstraint<?> constraint) {
        final Set<? extends Range<? extends Number>> constraints = constraint.getAllowedRanges().asRanges();
        final List<Range<T>> ranges = new ArrayList<>(constraints.size());

        for (Range<? extends Number> c : constraints) {
            ranges.add(Range.closed(getValue(c.lowerEndpoint()), getValue(c.upperEndpoint())));
        }

        return ranges.toString();
    }

    @Override
    protected final String generateRangeCheckerImplementation(final String checkerName,
            final RangeConstraint<?> constraints, final Function<Class<?>, String> classImporter) {
        final StringBuilder sb = new StringBuilder();
        final Collection<String> expressions = createExpressions(constraints);

        sb.append("private static void ").append(checkerName).append("(final ").append(primitiveName).append(" value) {\n");

        if (!expressions.isEmpty()) {
            for (String exp : expressions) {
                sb.append("    if (").append(exp).append(") {\n");
                sb.append("        return;\n");
                sb.append("    }\n");
            }

            sb.append("    ").append(classImporter.apply(CodeHelpers.class)).append(".throwInvalidRange(\"")
            .append(createRangeString(constraints)).append("\", value);\n");
        }

        sb.append("}\n");

        return sb.toString();
    }
}
