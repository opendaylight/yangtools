/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.util.BaseConstraints;
import org.opendaylight.yangtools.yang.model.util.UnresolvedNumber;

public abstract class RangeRestrictedTypeBuilder<T extends TypeDefinition<T>> extends AbstractRestrictedTypeBuilder<T> {
    private List<RangeConstraint> rangeAlternatives;

    RangeRestrictedTypeBuilder(final T baseType, final SchemaPath path) {
        super(baseType, path);
    }

    public final void setRangeAlternatives(@Nonnull final Collection<RangeConstraint> rangeAlternatives) {
        Preconditions.checkState(this.rangeAlternatives == null, "Range alternatives already defined as %s",
                this.rangeAlternatives);
        this.rangeAlternatives = ImmutableList.copyOf(rangeAlternatives);
        touch();
    }

    private static List<RangeConstraint> ensureResolvedRanges(final List<RangeConstraint> unresolved,
            final List<RangeConstraint> baseRangeConstraints) {
        // First check if we need to resolve anything at all
        for (final RangeConstraint c : unresolved) {
            if (c.getMax() instanceof UnresolvedNumber || c.getMin() instanceof UnresolvedNumber) {
                return resolveRanges(unresolved, baseRangeConstraints);
            }
        }

        // No need, just return the same list
        return unresolved;
    }

    private static List<RangeConstraint> resolveRanges(final List<RangeConstraint> unresolved,
            final List<RangeConstraint> baseRangeConstraints) {
        final Builder<RangeConstraint> builder = ImmutableList.builder();

        for (final RangeConstraint c : unresolved) {
            final Number max = c.getMax();
            final Number min = c.getMin();

            if (max instanceof UnresolvedNumber || min instanceof UnresolvedNumber) {
                final Number rMax = max instanceof UnresolvedNumber ?
                        ((UnresolvedNumber)max).resolveRange(baseRangeConstraints) : max;
                final Number rMin = min instanceof UnresolvedNumber ?
                        ((UnresolvedNumber)min).resolveRange(baseRangeConstraints) : min;

                builder.add(BaseConstraints.newRangeConstraint(rMin, rMax, Optional.fromNullable(c.getDescription()),
                    Optional.fromNullable(c.getReference()), c.getErrorAppTag(), c.getErrorMessage()));
            } else {
                builder.add(c);
            }

        }

        return builder.build();
    }

    private static List<RangeConstraint> ensureTypedRanges(final List<RangeConstraint> ranges,
            final Class<? extends Number> clazz) {
        for (final RangeConstraint c : ranges) {
            if (!clazz.isInstance(c.getMin()) || !clazz.isInstance(c.getMax())) {
                return typedRanges(ranges, clazz);
            }
        }

        return ranges;
    }

    private static List<RangeConstraint> typedRanges(final List<RangeConstraint> ranges, final Class<? extends Number> clazz) {
        final Function<Number, Number> function = NumberUtil.converterTo(clazz);
        Preconditions.checkArgument(function != null, "Unsupported range class %s", clazz);

        final Builder<RangeConstraint> builder = ImmutableList.builder();

        for (final RangeConstraint c : ranges) {
            if (!clazz.isInstance(c.getMin()) || !clazz.isInstance(c.getMax())) {
                final Number min, max;

                try {
                    min = function.apply(c.getMin());
                    max = function.apply(c.getMax());
                } catch (final NumberFormatException e) {
                    throw new IllegalArgumentException(String.format("Constraint %s does not fit into range of %s",
                        c, clazz.getSimpleName()), e);
                }
                builder.add(BaseConstraints.newRangeConstraint(min, max, Optional.fromNullable(c.getDescription()),
                    Optional.fromNullable(c.getReference()), c.getErrorAppTag(), c.getErrorMessage()));
            } else {
                builder.add(c);
            }
        }

        return builder.build();
    }

    private static boolean rangeCovered(final List<RangeConstraint> where,
            final RangeConstraint what) {
        for (final RangeConstraint c : where) {
            if (NumberUtil.isRangeCovered(what.getMin(), what.getMax(), c.getMin(), c.getMax())) {
                return true;
            }
        }

        return false;
    }

    final List<RangeConstraint> calculateRangeConstraints(final List<RangeConstraint> baseRangeConstraints) {
        if (rangeAlternatives == null || rangeAlternatives.isEmpty()) {
            return baseRangeConstraints;
        }

        // Run through alternatives and resolve them against the base type
        Verify.verify(!baseRangeConstraints.isEmpty(), "Base type %s does not define constraints", getBaseType());
        final List<RangeConstraint> resolvedRanges = ensureResolvedRanges(rangeAlternatives, baseRangeConstraints);

        // Next up, ensure the of boundaries match base constraints
        final Class<? extends Number> clazz = baseRangeConstraints.get(0).getMin().getClass();
        final List<RangeConstraint> typedRanges = ensureTypedRanges(resolvedRanges, clazz);

        // Now verify if new ranges are strict subset of base ranges
        for (final RangeConstraint c : typedRanges) {
            if (!rangeCovered(baseRangeConstraints, c)) {
                throw new InvalidRangeConstraintException(c, "Range constraint %s is not a subset of parent constraints %s",
                    c, baseRangeConstraints);
            }
        }

        // Now verify if new ranges match default value
        final Object defaultValueObj = getBaseType().getDefaultValue();
        if (defaultValueObj != null) {
            Number defaultVal = null;
            try {
                defaultVal = getNumber(defaultValueObj);
            } catch (final ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            boolean match = false;
            for (final RangeConstraint c : typedRanges) {
                if (isInRange(defaultVal, c)) {
                    match = true;
                    break;
                }
            }
            if (!match) {
                throw new InvalidRangeConstraintException(typedRanges.iterator().next(),
                        String.format("default value %s does not match specified ranges %s", defaultVal, typedRanges.iterator().next().toString()));
            }
        }

        return typedRanges;
    }

    private static Number getNumber(final Object defaultValueObj) throws ParseException {
        if(defaultValueObj instanceof Number) {
            return (Number) defaultValueObj;
        } else if(defaultValueObj instanceof String) {
            return NumberFormat.getInstance().parse((String)defaultValueObj);
        }

        throw new IllegalArgumentException("Default value must be instace of Number or String");
    }

    private static boolean isInRange(final Number defaultValue, final RangeConstraint c) {
        final double doubleValue = defaultValue.doubleValue();
        return Double.compare(c.getMin().doubleValue(), doubleValue) <= 0
                && Double.compare(c.getMax().doubleValue(), doubleValue) >= 0;
    }
}
