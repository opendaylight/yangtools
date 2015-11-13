/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
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
        for (LengthConstraint c : unresolved) {
            if (c.getMax() instanceof UnresolvedNumber || c.getMin() instanceof UnresolvedNumber) {
                return resolveRanges(unresolved, baseRangeConstraints);
            }
        }

        // No need, just return the same list
        return unresolved;
    }

    private static List<LengthConstraint> resolveRanges(final List<LengthConstraint> unresolved,
            final List<LengthConstraint> baseRangeConstraints) {
        final Builder<LengthConstraint> builder = ImmutableList.builder();

        for (LengthConstraint c : unresolved) {
            final Number max = c.getMax();
            final Number min = c.getMin();

            if (max instanceof UnresolvedNumber || min instanceof UnresolvedNumber) {
                final Number rMax = max instanceof UnresolvedNumber ?
                        ((UnresolvedNumber)max).resolveLength(baseRangeConstraints) : max;
                final Number rMin = min instanceof UnresolvedNumber ?
                        ((UnresolvedNumber)min).resolveLength(baseRangeConstraints) : min;

                builder.add(BaseConstraints.newLengthConstraint(rMin, rMax, Optional.fromNullable(c.getDescription()),
                    Optional.fromNullable(c.getReference())));
            } else {
                builder.add(c);
            }

        }

        return builder.build();
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

    private static List<LengthConstraint> typedLengths(final List<LengthConstraint> lengths, final Class<? extends Number> clazz) {
        final Function<Number, Number> function = NumberUtil.converterTo(clazz);
        Preconditions.checkArgument(function != null, "Unsupported range class %s", clazz);

        final Builder<LengthConstraint> builder = ImmutableList.builder();

        for (LengthConstraint c : lengths) {
            if (!clazz.isInstance(c.getMin()) || !clazz.isInstance(c.getMax())) {
                final Number min, max;

                try {
                    min = function.apply(c.getMin());
                    max = function.apply(c.getMax());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(String.format("Constraint %s does not fit into range of %s",
                        c, clazz.getSimpleName()), e);
                }
                builder.add(BaseConstraints.newLengthConstraint(min, max, Optional.fromNullable(c.getDescription()),
                    Optional.fromNullable(c.getReference())));
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

    private static List<LengthConstraint> ensureAdjacentMerged(final List<LengthConstraint> ranges) {
        if (ranges.size() < 2) {
            return ranges;
        }

        final Iterator<LengthConstraint> it = ranges.iterator();

        LengthConstraint current = it.next();
        do {
            final LengthConstraint next = it.next();
            if (current.getMax().equals(next.getMin())) {
                // FIXME: run merge and return
            }

            current = next;
        } while (it.hasNext());

        return ranges;
    }

    final List<LengthConstraint> calculateLenghtConstraints(final List<LengthConstraint> baseLengthConstraints) {
        if (lengthAlternatives == null || lengthAlternatives.isEmpty()) {
            return baseLengthConstraints;
        }

        // Run through alternatives and resolve them against the base type
        Verify.verify(!baseLengthConstraints.isEmpty(), "Base type %s does not define constraints", getBaseType());
        final List<LengthConstraint> resolvedLengths = ensureResolvedLengths(lengthAlternatives, baseLengthConstraints);

        // Next up, ensure the of boundaries match base constraints
        final Class<? extends Number> clazz = baseLengthConstraints.get(0).getMin().getClass();
        final List<LengthConstraint> typedLengths = ensureTypedLengths(resolvedLengths, clazz);

        // Now verify if new ranges are strict subset of base ranges
        for (LengthConstraint c : typedLengths) {
            Preconditions.checkArgument(lengthCovered(baseLengthConstraints, c),
                "Range constraint %s is not a subset of parent constraints %s", c, baseLengthConstraints);
        }

        return ensureAdjacentMerged(typedLengths);
    }
}
