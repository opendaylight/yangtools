/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.math.BigDecimal;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.util.BaseConstraints;

final class BaseDecimalType extends AbstractRangedBaseType<DecimalTypeDefinition> implements DecimalTypeDefinition {
    private static final List<List<RangeConstraint>> IMPLICIT_RANGE_STATEMENTS;

    static {
        final Builder<List<RangeConstraint>> b = ImmutableList.builder();
        b.add(createRangeConstraint("-922337203685477580.8", "922337203685477580.7"));
        b.add(createRangeConstraint("-92233720368547758.08", "92233720368547758.07"));
        b.add(createRangeConstraint("-9223372036854775.808", "9223372036854775.807"));
        b.add(createRangeConstraint("-922337203685477.5808", "922337203685477.5807"));
        b.add(createRangeConstraint("-92233720368547.75808", "92233720368547.75807"));
        b.add(createRangeConstraint("-9223372036854.775808", "9223372036854.775807"));
        b.add(createRangeConstraint("-922337203685.4775808", "922337203685.4775807"));
        b.add(createRangeConstraint("-92233720368.54775808", "92233720368.54775807"));
        b.add(createRangeConstraint("-9223372036.854775808", "9223372036.854775807"));
        b.add(createRangeConstraint("-922337203.6854775808", "922337203.6854775807"));
        b.add(createRangeConstraint("-92233720.36854775808", "92233720.36854775807"));
        b.add(createRangeConstraint("-9223372.036854775808", "9223372.036854775807"));
        b.add(createRangeConstraint("-922337.2036854775808", "922337.2036854775807"));
        b.add(createRangeConstraint("-92233.72036854775808", "92233.72036854775807"));
        b.add(createRangeConstraint("-9223.372036854775808", "9223.372036854775807"));
        b.add(createRangeConstraint("-922.3372036854775808", "922.3372036854775807"));
        b.add(createRangeConstraint("-92.23372036854775808", "92.23372036854775807"));
        b.add(createRangeConstraint("-9.223372036854775808", "9.223372036854775807"));
        IMPLICIT_RANGE_STATEMENTS = b.build();
    }

    private static List<RangeConstraint> createRangeConstraint(final String min, final String max) {
        return ImmutableList.of(BaseConstraints.newRangeConstraint(new BigDecimal(min), new BigDecimal(max),
            Optional.absent(), Optional.of("https://tools.ietf.org/html/rfc6020#section-9.3.4")));
    }

    static List<RangeConstraint> constraintsForDigits(final int fractionDigits) {
        return IMPLICIT_RANGE_STATEMENTS.get(fractionDigits - 1);
    }

    private final Integer fractionDigits;

    BaseDecimalType(final SchemaPath path, final List<UnknownSchemaNode> unknownSchemaNodes,
            final Integer fractionDigits, final List<RangeConstraint> rangeConstraints) {
        super(path, unknownSchemaNodes, rangeConstraints);
        this.fractionDigits = fractionDigits;
    }

    @Nonnull
    @Override
    public Integer getFractionDigits() {
        return fractionDigits;
    }

    @Override
    public int hashCode() {
        return TypeDefinitions.hashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return TypeDefinitions.equals(this, obj);
    }

    @Override
    public String toString() {
        return TypeDefinitions.toString(this);
    }
}
