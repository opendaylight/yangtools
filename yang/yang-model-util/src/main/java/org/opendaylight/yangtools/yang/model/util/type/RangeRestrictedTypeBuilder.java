/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.ImmutableRangeSet.Builder;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.ConstraintMetaDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.stmt.UnresolvedNumber;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueRange;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeRestrictedTypeDefinition;

public abstract class RangeRestrictedTypeBuilder<T extends RangeRestrictedTypeDefinition<T, N>,
        N extends Number & Comparable<N>> extends AbstractRestrictedTypeBuilder<T> {
    private ConstraintMetaDefinition constraint;
    private List<ValueRange> ranges;

    RangeRestrictedTypeBuilder(final T baseType, final @NonNull SchemaPath path) {
        super(baseType, path);
    }

    @SuppressWarnings("checkstyle:hiddenField")
    public final void setRangeConstraint(final @NonNull ConstraintMetaDefinition constraint,
            final @NonNull List<ValueRange> ranges) {
        checkState(this.ranges == null, "Range constraint already defined as %s %s", this.ranges, this.constraint);

        this.constraint = requireNonNull(constraint);
        this.ranges = ImmutableList.copyOf(ranges);
        touch();
    }

    final RangeConstraint<N> calculateRangeConstraint(final RangeConstraint<N> baseRangeConstraint) {
        if (ranges == null) {
            return baseRangeConstraint;
        }

        // Run through alternatives and resolve them against the base type
        final RangeSet<N> baseRangeSet = baseRangeConstraint.getAllowedRanges();
        Verify.verify(!baseRangeSet.isEmpty(), "Base type %s does not define constraints", getBaseType());

        final Range<N> baseRange = baseRangeSet.span();
        final List<ValueRange> resolvedRanges = ensureResolvedRanges(ranges, baseRange);

        // Next up, ensure the of boundaries match base constraints
        final RangeSet<N> typedRanges = ensureTypedRanges(resolvedRanges, baseRange.lowerEndpoint().getClass());

        // Now verify if new ranges are strict subset of base ranges
        if (!baseRangeSet.enclosesAll(typedRanges)) {
            throw new InvalidRangeConstraintException(typedRanges,
                "Range constraint %s is not a subset of parent constraint %s", typedRanges, baseRangeSet);
        }

        return new ResolvedRangeConstraint<>(constraint, typedRanges);
    }

    private static <C extends Number & Comparable<C>> List<ValueRange> ensureResolvedRanges(
            final List<ValueRange> unresolved, final Range<C> baseRange) {
        // First check if we need to resolve anything at all
        for (ValueRange c : unresolved) {
            if (c.lowerBound() instanceof UnresolvedNumber || c.upperBound() instanceof UnresolvedNumber) {
                return resolveRanges(unresolved, baseRange);
            }
        }

        // No need, just return the same list
        return unresolved;
    }

    private static <T extends Number & Comparable<T>> List<ValueRange> resolveRanges(final List<ValueRange> unresolved,
            final Range<T> baseRange) {
        final List<ValueRange> ret = new ArrayList<>(unresolved.size());
        for (ValueRange range : unresolved) {
            final Number min = range.lowerBound();
            final Number max = range.upperBound();

            if (max instanceof UnresolvedNumber || min instanceof UnresolvedNumber) {
                final @NonNull Number rMin = min instanceof UnresolvedNumber
                        ?  ((UnresolvedNumber)min).resolveRange(baseRange) : min;
                final @NonNull Number rMax = max instanceof UnresolvedNumber
                        ?  ((UnresolvedNumber)max).resolveRange(baseRange) : max;
                ret.add(ValueRange.of(rMin, rMax));
            } else {
                ret.add(range);
            }
        }

        return ret;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Number & Comparable<T>> RangeSet<T> ensureTypedRanges(final List<ValueRange> ranges,
            final Class<? extends Number> clazz) {
        final Builder<T> builder = ImmutableRangeSet.builder();
        for (ValueRange range : ranges) {
            if (!clazz.isInstance(range.lowerBound()) || !clazz.isInstance(range.upperBound())) {
                return typedRanges(ranges, clazz);
            }

            builder.add(Range.closed((T) range.lowerBound(), (T)range.upperBound()));
        }

        return builder.build();
    }

    @SuppressWarnings("unchecked")
    private static <T extends Number & Comparable<T>> RangeSet<T> typedRanges(final List<ValueRange> ranges,
            final Class<? extends Number> clazz) {
        final Function<Number, ? extends Number> function = NumberUtil.converterTo(clazz);
        Preconditions.checkArgument(function != null, "Unsupported range class %s", clazz);

        final Builder<T> builder = ImmutableRangeSet.builder();

        for (ValueRange range : ranges) {
            if (!clazz.isInstance(range.lowerBound()) || !clazz.isInstance(range.upperBound())) {
                final Number min;
                final Number max;

                try {
                    min = function.apply(range.lowerBound());
                    max = function.apply(range.upperBound());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(String.format("Constraint %s does not fit into range of %s",
                        range, clazz.getSimpleName()), e);
                }

                builder.add(Range.closed((T)min, (T)max));
            } else {
                builder.add(Range.closed((T) range.lowerBound(), (T)range.upperBound()));
            }
        }

        return builder.build();
    }
}
