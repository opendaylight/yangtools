/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.type;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Decimal64;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ConstraintMetaDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueRanges;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;

public final class DecimalTypeBuilder extends RangeRestrictedTypeBuilder<DecimalTypeDefinition, Decimal64> {
    private Integer fractionDigits;

    DecimalTypeBuilder(final QName qname) {
        super(null, qname);
    }

    public DecimalTypeBuilder setFractionDigits(final int fractionDigits) {
        checkState(this.fractionDigits == null, "Fraction digits already defined to %s", this.fractionDigits);
        this.fractionDigits = fractionDigits;
        return this;
    }

    @Override
    DecimalTypeDefinition buildConstrainedType(final ConstraintMetaDefinition constraint, final ValueRanges ranges) {
        final int scale = scale();
        return new BaseDecimalType(getQName(), getUnknownSchemaNodes(), scale,
            new ResolvedRangeConstraint<>(constraint, ensureResolvedScale(
                calculateRanges(BaseDecimalType.constraintsForDigits(scale), ranges), scale)));
    }

    @Override
    DecimalTypeDefinition buildUnconstrainedType() {
        final int scale = scale();
        return new BaseDecimalType(getQName(), getUnknownSchemaNodes(), scale,
            BaseDecimalType.constraintsForDigits(scale));
    }

    private int scale() {
        final var local = fractionDigits;
        checkState(local != null, "Fraction digits not defined");
        return local;
    }

    private static @NonNull RangeSet<Decimal64> ensureResolvedScale(final RangeSet<Decimal64> ranges, final int scale) {
        // Check if we need to resolve anything at all
        final var rangeSet = ranges.asRanges();
        for (final var range : rangeSet) {
            if (range.lowerEndpoint().scale() != scale || range.upperEndpoint().scale() != scale) {
                return resolveScale(rangeSet, scale);
            }
        }

        // Everything is good - return same ranges
        return ranges;
    }

    private static @NonNull RangeSet<Decimal64> resolveScale(final Set<Range<Decimal64>> ranges, final int scale) {
        final var builder = ImmutableRangeSet.<Decimal64>builder();
        for (final var range : ranges) {
            boolean reuse = true;

            var lower = range.lowerEndpoint();
            if (lower.scale() != scale) {
                lower = lower.scaleTo(scale);
                reuse = false;
            }
            var upper = range.upperEndpoint();
            if (upper.scale() != scale) {
                upper = upper.scaleTo(scale);
                reuse = false;
            }
            builder.add(reuse ? range : Range.closed(lower, upper));
        }

        return builder.build();
    }
}
