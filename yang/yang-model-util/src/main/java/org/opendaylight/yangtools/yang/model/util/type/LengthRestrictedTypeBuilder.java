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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.util.BaseConstraints;
import org.opendaylight.yangtools.yang.model.util.UnresolvedNumber;

public abstract class LengthRestrictedTypeBuilder<T extends TypeDefinition<T>> extends AbstractRestrictedTypeBuilder<T> {
    private List<LengthConstraint> lengthAlternatives;

    LengthRestrictedTypeBuilder(final T baseType, final SchemaPath path) {
        super(Preconditions.checkNotNull(baseType), path);
    }

    public final void setLengthAlternatives(@Nonnull final Collection<LengthConstraint> lengthAlternatives) {
        Preconditions.checkState(this.lengthAlternatives == null, "Range alternatives already defined as %s",
                lengthAlternatives);
        this.lengthAlternatives = ImmutableList.copyOf(lengthAlternatives);
        touch();
    }

    private static List<LengthConstraint> ensureResolvedLengths(final List<LengthConstraint> unresolved,
            final List<LengthConstraint> baseRangeConstraints) {
        // First check if we need to resolve anything at all
        for (final LengthConstraint c : unresolved) {
            if (c.getMax() instanceof UnresolvedNumber || c.getMin() instanceof UnresolvedNumber) {
                return resolveLengths(unresolved, baseRangeConstraints);
            }
        }

        // No need, just return the same list
        return unresolved;
    }

    private static List<LengthConstraint> resolveLengths(final List<LengthConstraint> unresolved,
            final List<LengthConstraint> baseLengthConstraints) {
        final Builder<LengthConstraint> builder = ImmutableList.builder();

        for (final LengthConstraint c : unresolved) {
            final Number max = c.getMax();
            final Number min = c.getMin();

            if (max instanceof UnresolvedNumber || min instanceof UnresolvedNumber) {
                final Number rMax = max instanceof UnresolvedNumber ?
                        ((UnresolvedNumber)max).resolveLength(baseLengthConstraints) : max;
                final Number rMin = min instanceof UnresolvedNumber ?
                        ((UnresolvedNumber)min).resolveLength(baseLengthConstraints) : min;

                builder.add(BaseConstraints.newLengthConstraint(rMin, rMax, Optional.fromNullable(c.getDescription()),
                    Optional.fromNullable(c.getReference()), c.getErrorAppTag(), c.getErrorMessage()));
            } else {
                builder.add(c);
            }
        }

        return builder.build();
    }

    private static List<LengthConstraint> ensureTypedLengths(final List<LengthConstraint> lengths,
            final Class<? extends Number> clazz) {
        for (final LengthConstraint c : lengths) {
            if (!clazz.isInstance(c.getMin()) || !clazz.isInstance(c.getMax())) {
                return typedLengths(lengths, clazz);
            }
        }

        return lengths;
    }

    private static List<LengthConstraint> typedLengths(final List<LengthConstraint> lengths, final Class<? extends Number> clazz) {
        final Function<Number, Number> function = NumberUtil.converterTo(clazz);
        Preconditions.checkArgument(function != null, "Unsupported range class %s", clazz);

        final Builder<LengthConstraint> builder = ImmutableList.builder();

        for (final LengthConstraint c : lengths) {
            if (!clazz.isInstance(c.getMin()) || !clazz.isInstance(c.getMax())) {
                final Number min, max;

                try {
                    min = function.apply(c.getMin());
                    max = function.apply(c.getMax());
                } catch (final NumberFormatException e) {
                    throw new IllegalArgumentException(String.format("Constraint %s does not fit into range of %s",
                        c, clazz.getSimpleName()), e);
                }
                builder.add(BaseConstraints.newLengthConstraint(min, max, Optional.fromNullable(c.getDescription()),
                    Optional.fromNullable(c.getReference()), c.getErrorAppTag(), c.getErrorMessage()));
            } else {
                builder.add(c);
            }
        }

        return builder.build();
    }

    private static boolean lengthCovered(final List<LengthConstraint> where,
            final LengthConstraint what) {
        for (final LengthConstraint c : where) {
            if (NumberUtil.isRangeCovered(what.getMin(), what.getMax(), c.getMin(), c.getMax())) {
                return true;
            }
        }

        return false;
    }

    abstract T buildType(List<LengthConstraint> lengthConstraints);
    abstract List<LengthConstraint> getLengthConstraints(T type);
    abstract List<LengthConstraint> typeLengthConstraints();

    private List<LengthConstraint> findLenghts() {
        List<LengthConstraint> ret = ImmutableList.of();
        T wlk = getBaseType();
        while (wlk != null && ret.isEmpty()) {
            ret = getLengthConstraints(wlk);
            wlk = wlk.getBaseType();
        }

        return ret.isEmpty() ? typeLengthConstraints() : ret;
    }

    @Override
    final T buildType() {
        final List<LengthConstraint> baseLengths = findLenghts();

        if (lengthAlternatives == null || lengthAlternatives.isEmpty()) {
            return buildType(baseLengths);
        }

        // Run through alternatives and resolve them against the base type
        final List<LengthConstraint> resolvedLengths = ensureResolvedLengths(lengthAlternatives, baseLengths);

        // Next up, ensure the of boundaries match base constraints
        final Class<? extends Number> clazz = baseLengths.get(0).getMin().getClass();
        final List<LengthConstraint> typedLengths = ensureTypedLengths(resolvedLengths, clazz);

        // Now verify if new length constraints are strict subset of base ranges
        for (final LengthConstraint c : typedLengths) {
            if (!lengthCovered(baseLengths, c)) {
                throw new InvalidLengthConstraintException(c, "Length constraint %s is not a subset of parent constraints %s",
                c, baseLengths);
            }
        }

        // Now verify if new length constraints match default value
        final Object defaultValueObj = getBaseType().getDefaultValue();
        if (defaultValueObj != null) {
            final String defaultVal = defaultValueObj.toString();
            boolean match = false;
            for (final LengthConstraint l : typedLengths) {
                if (matchLength(defaultVal, l)) {
                    match = true;
                    break;
                }
            }
            if (!match) {
                throw new InvalidLengthConstraintException(typedLengths.iterator().next(),
                        String.format("default value %s does not match specified lengths %s", defaultVal, typedLengths.iterator().next().toString()));
            }
        }

        return buildType(typedLengths);
    }

    private static boolean matchLength(final String defaultValue, final LengthConstraint l) {
        final int length = defaultValue.length();
        return Double.compare(l.getMin().doubleValue(), length) <= 0
                && Double.compare(l.getMax().doubleValue(), length) >= 0;
    }
}
