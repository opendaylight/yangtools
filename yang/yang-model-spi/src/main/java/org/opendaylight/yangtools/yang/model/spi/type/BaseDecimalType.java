/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.type;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;
import org.opendaylight.yangtools.yang.model.api.ConstraintMetaDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;

final class BaseDecimalType extends AbstractRangeRestrictedBaseType<DecimalTypeDefinition, BigDecimal>
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

    private static final ImmutableList<RangeConstraint<BigDecimal>> IMPLICIT_RANGE_STATEMENTS = ImmutableList.of(
        createRangeConstraint("-922337203685477580.8", "922337203685477580.7"),
        createRangeConstraint("-92233720368547758.08", "92233720368547758.07"),
        createRangeConstraint("-9223372036854775.808", "9223372036854775.807"),
        createRangeConstraint("-922337203685477.5808", "922337203685477.5807"),
        createRangeConstraint("-92233720368547.75808", "92233720368547.75807"),
        createRangeConstraint("-9223372036854.775808", "9223372036854.775807"),
        createRangeConstraint("-922337203685.4775808", "922337203685.4775807"),
        createRangeConstraint("-92233720368.54775808", "92233720368.54775807"),
        createRangeConstraint("-9223372036.854775808", "9223372036.854775807"),
        createRangeConstraint("-922337203.6854775808", "922337203.6854775807"),
        createRangeConstraint("-92233720.36854775808", "92233720.36854775807"),
        createRangeConstraint("-9223372.036854775808", "9223372.036854775807"),
        createRangeConstraint("-922337.2036854775808", "922337.2036854775807"),
        createRangeConstraint("-92233.72036854775808", "92233.72036854775807"),
        createRangeConstraint("-9223.372036854775808", "9223.372036854775807"),
        createRangeConstraint("-922.3372036854775808", "922.3372036854775807"),
        createRangeConstraint("-92.23372036854775808", "92.23372036854775807"),
        createRangeConstraint("-9.223372036854775808", "9.223372036854775807"));

    private static RangeConstraint<BigDecimal> createRangeConstraint(final String min, final String max) {
        return new ResolvedRangeConstraint<>(BUILTIN_CONSTRAINT, ImmutableRangeSet.of(
            Range.closed(new BigDecimal(min), new BigDecimal(max))));
    }

    static RangeConstraint<BigDecimal> constraintsForDigits(final int fractionDigits) {
        return verifyNotNull(IMPLICIT_RANGE_STATEMENTS.get(fractionDigits - 1));
    }

    private final int fractionDigits;

    BaseDecimalType(final SchemaPath path, final Collection<? extends UnknownSchemaNode> unknownSchemaNodes,
            final int fractionDigits, final RangeConstraint<BigDecimal> rangeConstraint) {
        super(path, unknownSchemaNodes, rangeConstraint);
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
