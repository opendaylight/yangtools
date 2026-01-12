/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.type;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import java.util.ArrayList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ConstraintMetaDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.UnresolvedNumber;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueRange;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueRanges;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeRestrictedTypeDefinition;

public abstract class RangeRestrictedTypeBuilder<T extends RangeRestrictedTypeDefinition<T, N>,
        N extends Number & Comparable<N>> extends AbstractRestrictedTypeBuilder<T> {
    private ConstraintMetaDefinition constraint;
    private ValueRanges ranges;

    RangeRestrictedTypeBuilder(final T baseType, final QName qname) {
        super(baseType, qname);
    }

    @SuppressWarnings("checkstyle:hiddenField")
    public final void setRangeConstraint(final @NonNull ConstraintMetaDefinition constraint,
            final @NonNull ValueRanges ranges) {
        checkState(this.ranges == null, "Range constraint already defined as %s %s", this.ranges, this.constraint);

        this.constraint = requireNonNull(constraint);
        this.ranges = requireNonNull(ranges);
        touch();
    }

    @Override
    final T buildType() {
        final var localRanges = ranges;
        return localRanges == null ? buildUnconstrainedType()
            : buildConstrainedType(verifyNotNull(constraint), localRanges);
    }

    abstract @NonNull T buildConstrainedType(@NonNull ConstraintMetaDefinition constraint, @NonNull ValueRanges ranges);

    abstract @NonNull T buildUnconstrainedType();

    final RangeSet<N> calculateRanges(final @NonNull RangeConstraint<N> baseConstraint,
            final @NonNull ValueRanges contraintRanges) {
        // Run through alternatives and resolve them against the base type
        final var baseRangeSet = baseConstraint.getAllowedRanges();
        verify(!baseRangeSet.isEmpty(), "Base type %s does not define constraints", getBaseType());

        final var baseRange = baseRangeSet.span();
        final var resolvedRanges = ensureResolvedRanges(contraintRanges, baseRange);

        // Next up, ensure the of boundaries match base constraints
        final var typedRanges = RangeRestrictedTypeBuilder.<N>ensureTypedRanges(resolvedRanges,
            baseRange.lowerEndpoint().getClass());

        // Now verify if new ranges are strict subset of base ranges
        if (!baseRangeSet.enclosesAll(typedRanges)) {
            throw new InvalidRangeConstraintException(typedRanges,
                "Range constraint %s is not a subset of parent constraint %s", typedRanges, baseRangeSet);
        }
        return typedRanges;
    }

    private static <C extends Number & Comparable<C>> ValueRanges ensureResolvedRanges(
            final ValueRanges unresolved, final Range<C> baseRange) {
        // First check if we need to resolve anything at all
        for (var c : unresolved) {
            if (c.lowerBound() instanceof UnresolvedNumber || c.upperBound() instanceof UnresolvedNumber) {
                return resolveRanges(unresolved, baseRange);
            }
        }

        // No need, just return the same list
        return unresolved;
    }

    private static <T extends Number & Comparable<T>> ValueRanges resolveRanges(final ValueRanges unresolved,
            final Range<T> baseRange) {
        final var ret = new ArrayList<ValueRange>(unresolved.size());
        for (var range : unresolved) {
            final var min = range.lowerBound();
            final var max = range.upperBound();

            if (max instanceof UnresolvedNumber || min instanceof UnresolvedNumber) {
                ret.add(ValueRange.of(
                    min instanceof UnresolvedNumber uMin ? uMin.resolveRange(baseRange) : min,
                    max instanceof UnresolvedNumber uMax ? uMax.resolveRange(baseRange) : max));
            } else {
                ret.add(range);
            }
        }
        return ValueRanges.of(ret);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Number & Comparable<T>> RangeSet<T> ensureTypedRanges(final ValueRanges ranges,
            final Class<? extends Number> clazz) {
        final var builder = ImmutableRangeSet.<T>builder();
        for (var range : ranges) {
            if (!clazz.isInstance(range.lowerBound()) || !clazz.isInstance(range.upperBound())) {
                return typedRanges(ranges, clazz);
            }

            builder.add(Range.closed((T) range.lowerBound(), (T)range.upperBound()));
        }

        return builder.build();
    }

    @SuppressWarnings("unchecked")
    private static <T extends Number & Comparable<T>> RangeSet<T> typedRanges(final ValueRanges ranges,
            final Class<? extends Number> clazz) {
        final var function = NumberUtil.converterTo(clazz);
        checkArgument(function != null, "Unsupported range class %s", clazz);

        final var builder = ImmutableRangeSet.<T>builder();
        for (var range : ranges) {
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
