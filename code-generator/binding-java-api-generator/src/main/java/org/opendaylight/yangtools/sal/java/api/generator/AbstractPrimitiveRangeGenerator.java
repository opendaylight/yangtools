/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.java.api.generator;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractPrimitiveRangeGenerator<T extends Number & Comparable<T>> extends AbstractRangeGenerator<T> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractPrimitiveRangeGenerator.class);
    private final T minValue;
    private final T maxValue;

    protected AbstractPrimitiveRangeGenerator(final Class<T> typeClass, final T minValue, final T maxValue) {
        super(typeClass);
        this.minValue = Preconditions.checkNotNull(minValue);
        this.maxValue = Preconditions.checkNotNull(maxValue);
    }

    private boolean needsMaximumEnforcement(final T maxToEnforce) {
        return maxValue.compareTo(maxToEnforce) > 0;
    }

    private boolean needsMinimumEnforcement(final T minToEnforce) {
        return minValue.compareTo(minToEnforce) < 0;
    }

    private final Collection<String> createConditionals(final Collection<RangeConstraint> restrictions) {
        final Collection<String> ret = new ArrayList<>(restrictions.size());

        for (RangeConstraint r : restrictions) {
            final T min = getValue(r.getMin());
            final boolean needMin = needsMinimumEnforcement(min);

            final T max = getValue(r.getMax());
            final boolean needMax = needsMaximumEnforcement(max);

            if (!needMin && !needMax) {
                LOG.debug("Type {} indicates [{}, {}] does not require enforcement", getTypeName(), min, max);
                continue;
            }

            final StringBuilder sb = new StringBuilder();
            if (needMin) {
                sb.append("value >= ").append(format(min));
                if (needMax) {
                    sb.append(" && ");
                }
            }
            if (needMax) {
                sb.append("value <= ").append(format(max));
            }

            ret.add(sb.toString());
        }

        return ret;
    }

    @Override
    protected final String generateRangeCheckerImplementation(final String checkerName, final Collection<RangeConstraint> restrictions) {
        final StringBuilder sb = new StringBuilder();
        final Collection<String> conditionals = createConditionals(restrictions);

        sb.append("private static void ").append(checkerName).append("(final ").append(getTypeName()).append(" value) {\n");

        if (!conditionals.isEmpty()) {
            for (String c : conditionals) {
                sb.append("    if (").append(c).append(") {\n");
                sb.append("        return;\n");
                sb.append("    }\n");
            }

            sb.append("    throw new IllegalArgumentException(String.format(\"Invalid value %s does not match any required ranges\", value));\n");
        }

        sb.append("}\n\n");

        return sb.toString();
    }
}
