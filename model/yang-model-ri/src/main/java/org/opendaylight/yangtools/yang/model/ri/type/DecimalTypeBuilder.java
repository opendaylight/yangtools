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
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Decimal64;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ConstraintMetaDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;

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
    DecimalTypeDefinition buildType() {
        final var local = fractionDigits;
        checkState(local != null, "Fraction digits not defined");
        final int scale = local;

        return new BaseDecimalType(getQName(), getUnknownSchemaNodes(), scale,
            ensureResolvedScale(calculateRangeConstraint(BaseDecimalType.constraintsForDigits(scale)), scale));
    }

    private static @NonNull RangeConstraint<Decimal64> ensureResolvedScale(
            final @NonNull RangeConstraint<Decimal64> constraint, final int scale) {
        // Check if we need to resolve anything at all
        final var ranges = constraint.getAllowedRanges().asRanges();
        for (final var range : ranges) {
            if (range.lowerEndpoint().scale() != scale || range.upperEndpoint().scale() != scale) {
                return resolveScale(constraint, ranges, scale);
            }
        }

        // Everything is good - return same constraint
        return constraint;
    }

    private static @NonNull ResolvedRangeConstraint<Decimal64> resolveScale(final ConstraintMetaDefinition meta,
            final Set<Range<Decimal64>> ranges, final int scale) {
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
        return new ResolvedRangeConstraint<>(meta, builder.build());
    }
}
