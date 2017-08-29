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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.stmt.UnresolvedNumber;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueRange;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.LengthRestrictedTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.BaseConstraints;

public abstract class LengthRestrictedTypeBuilder<T extends LengthRestrictedTypeDefinition<T>>
        extends AbstractRestrictedTypeBuilder<T> {
    private List<ValueRange> lengthAlternatives;

    LengthRestrictedTypeBuilder(final T baseType, final SchemaPath path) {
        super(Preconditions.checkNotNull(baseType), path);
    }

    public final void setLengthAlternatives(@Nonnull final List<ValueRange> lengthAlternatives) {
        Preconditions.checkState(this.lengthAlternatives == null, "Length alternatives already defined as %s",
                lengthAlternatives);
        this.lengthAlternatives = ImmutableList.copyOf(lengthAlternatives);
        touch();
    }

    private static List<ValueRange> ensureResolvedLengths(final List<ValueRange> unresolved,
            final List<ValueRange> baseRangeConstraints) {
        // First check if we need to resolve anything at all
        for (ValueRange c : unresolved) {
            if (c.lowerBound() instanceof UnresolvedNumber || c.upperBound() instanceof UnresolvedNumber) {
                return resolveLengths(unresolved, baseRangeConstraints);
            }
        }

        // No need, just return the same list
        return unresolved;
    }

    private static List<ValueRange> resolveLengths(final List<ValueRange> unresolved,
            final List<ValueRange> baseLengthConstraints) {
        final List<ValueRange> resolved = new ArrayList<>(unresolved.size());

        for (ValueRange c : unresolved) {
            final Number min = c.lowerBound();
            final Number max = c.upperBound();

            final ValueRange res;
            if (min instanceof UnresolvedNumber || max instanceof UnresolvedNumber) {
                final Number rMax = max instanceof UnresolvedNumber
                    ? ((UnresolvedNumber)max).resolveLength(baseLengthConstraints) : max;
                final Number rMin = min instanceof UnresolvedNumber
                    ? ((UnresolvedNumber)min).resolveLength(baseLengthConstraints) : min;

                res = ValueRange.of(rMin, rMax);
            } else {
                res = c;
            }

            resolved.add(res);
        }

        return ImmutableList.copyOf(resolved);
    }

    private static List<LengthConstraint> ensureTypedLengths(final List<LengthConstraint> lengths,
            final Class<? extends Number> clazz) {
        for (LengthConstraint c : lengths) {
            if (!clazz.isInstance(c.getMin()) || !clazz.isInstance(c.getMax())) {
                return typedLengths(lengths, clazz);
            }
        }

        return lengths;
    }

    private static List<LengthConstraint> typedLengths(final List<LengthConstraint> lengths,
            final Class<? extends Number> clazz) {
        final Function<Number, Number> function = NumberUtil.converterTo(clazz);
        Preconditions.checkArgument(function != null, "Unsupported range class %s", clazz);

        final Builder<LengthConstraint> builder = ImmutableList.builder();

        for (LengthConstraint c : lengths) {
            if (!clazz.isInstance(c.getMin()) || !clazz.isInstance(c.getMax())) {
                final Number min;
                final Number max;

                try {
                    min = function.apply(c.getMin());
                    max = function.apply(c.getMax());
                } catch (NumberFormatException e) {
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
        for (LengthConstraint c : where) {
            if (NumberUtil.isRangeCovered(what.getMin(), what.getMax(), c.getMin(), c.getMax())) {
                return true;
            }
        }

        return false;
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

        // Now verify if new ranges are strict subset of base ranges
        for (LengthConstraint c : typedLengths) {
            if (!lengthCovered(baseLengths, c)) {
                throw new InvalidLengthConstraintException(c,
                        "Length constraint %s is not a subset of parent constraints %s", c, baseLengths);
            }
        }

        return buildType(typedLengths);
    }

    abstract T buildType(List<LengthConstraint> lengthConstraints);

    abstract List<LengthConstraint> typeLengthConstraints();

    private List<LengthConstraint> findLenghts() {
        List<LengthConstraint> ret = ImmutableList.of();
        T wlk = getBaseType();
        while (wlk != null && ret.isEmpty()) {
            ret = wlk.getLengthConstraints();
            wlk = wlk.getBaseType();
        }

        return ret.isEmpty() ? typeLengthConstraints() : ret;
    }
}
