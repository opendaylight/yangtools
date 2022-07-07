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
        checkState(fractionDigits != null, "Fraction digits not defined");
        BaseDecimalType resultRange = new BaseDecimalType(getQName(), getUnknownSchemaNodes(), fractionDigits,
                calculateRangeConstraint(BaseDecimalType.constraintsForDigits(fractionDigits)));
        BaseDecimalType resolvedRange = ensureResolvedScale(resultRange, fractionDigits);

        return resolvedRange;
    }

    private BaseDecimalType ensureResolvedScale(BaseDecimalType decimalType, @NonNull int scale) {
        if (!decimalType.getRangeConstraint().isPresent()) {
            return decimalType;
        }

        RangeConstraint<Decimal64> constraint = decimalType.getRangeConstraint().get();

        // Check if we need to resolve anything at all
        for (Range<Decimal64> range : constraint.getAllowedRanges().asRanges()) {
            if (range.lowerEndpoint().scale() != scale || range.upperEndpoint().scale() != scale) {
                return resolveScale(decimalType, scale);
            }
        }

        // Everything is good - return same decimal type
        return decimalType;
    }

    private BaseDecimalType resolveScale(BaseDecimalType unresolved, int scale) {
        Set<Range<@NonNull Decimal64>> unresolvedRanges = unresolved.getRangeConstraint().get().getAllowedRanges()
                .asRanges();
        final ImmutableRangeSet.Builder<Decimal64> builder = ImmutableRangeSet.builder();
        for (Range<Decimal64> range : unresolvedRanges) {
            Decimal64 lower = range.lowerEndpoint();
            Decimal64 upper = range.upperEndpoint();
            if (lower.scale() != scale) {
                lower = lower.scaleTo(scale);
            }
            if (upper.scale() != scale) {
                upper = upper.scaleTo(scale);
            }
            builder.add(Range.closed(lower, upper));
        }

        ImmutableRangeSet<Decimal64> scaledRangeSet = builder.build();
        ResolvedRangeConstraint<Decimal64> resolved =
                new ResolvedRangeConstraint<>(unresolved.getRangeConstraint().get(), scaledRangeSet);
        return new BaseDecimalType(getQName(), getUnknownSchemaNodes(), scale, resolved);
    }
}
