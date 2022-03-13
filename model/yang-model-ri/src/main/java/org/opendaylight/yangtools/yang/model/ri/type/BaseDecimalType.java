/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.type;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import java.util.Collection;
import java.util.Optional;
import org.opendaylight.yangtools.yang.common.Decimal64;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ConstraintMetaDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;

final class BaseDecimalType extends AbstractRangeRestrictedBaseType<DecimalTypeDefinition, Decimal64>
        implements DecimalTypeDefinition {
    private static final ConstraintMetaDefinition BUILTIN_CONSTRAINT = new ConstraintMetaDefinition() {

        @Override
        public Optional<String> getReference() {
            return Optional.of("https://tools.ietf.org/html/rfc6020#section-9.3.4");
        }

        @Override
        public Optional<String> getDescription() {
            return Optional.empty();
        }

        @Override
        public Optional<String> getErrorMessage() {
            return Optional.empty();
        }

        @Override
        public Optional<String> getErrorAppTag() {
            return Optional.empty();
        }
    };

    private static final ImmutableList<RangeConstraint<Decimal64>> IMPLICIT_RANGE_STATEMENTS;

    static {
        final var builder = ImmutableList.<RangeConstraint<Decimal64>>builderWithExpectedSize(18);
        for (int scale = 1; scale < 18; ++scale) {
            builder.add(new ResolvedRangeConstraint<>(BUILTIN_CONSTRAINT, ImmutableRangeSet.of(Range.closed(
                Decimal64.minValueIn(scale), Decimal64.maxValueIn(scale)))));
        }
        IMPLICIT_RANGE_STATEMENTS = builder.build();
    }

    static RangeConstraint<Decimal64> constraintsForDigits(final int fractionDigits) {
        return verifyNotNull(IMPLICIT_RANGE_STATEMENTS.get(fractionDigits - 1));
    }

    private final int fractionDigits;

    BaseDecimalType(final QName qname, final Collection<? extends UnknownSchemaNode> unknownSchemaNodes,
            final int fractionDigits, final RangeConstraint<Decimal64> rangeConstraint) {
        super(qname, unknownSchemaNodes, rangeConstraint);
        this.fractionDigits = fractionDigits;
    }

    @Override
    public int getFractionDigits() {
        return fractionDigits;
    }

    @Override
    public int hashCode() {
        return DecimalTypeDefinition.hashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return DecimalTypeDefinition.equals(this, obj);
    }

    @Override
    public String toString() {
        return DecimalTypeDefinition.toString(this);
    }
}
