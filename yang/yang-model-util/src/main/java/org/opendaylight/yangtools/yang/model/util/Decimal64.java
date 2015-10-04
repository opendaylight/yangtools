/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;

/**
 * The <code>default</code> implementation of Decimal Type Definition interface.
 *
 * @see DecimalTypeDefinition
 */
public final class Decimal64 implements DecimalTypeDefinition {
    private static final String DESCRIPTION = "The decimal64 type represents a subset of the real numbers, which can "
            + "be represented by decimal numerals. The value space of decimal64 is the set of numbers that can "
            + "be obtained by multiplying a 64-bit signed integer by a negative power of ten, i.e., expressible as "
            + "'i x 10^-n' where i is an integer64 and n is an integer between 1 and 18, inclusively.";
    private static final String REFERENCE = "https://tools.ietf.org/html/rfc6020#section-9.3";
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
        final String description = "Decimal values between " + min + " and " + max +", inclusively";

        return ImmutableList.of(BaseConstraints.newRangeConstraint(new BigDecimal(min), new BigDecimal(max),
            Optional.of(description), Optional.of("https://tools.ietf.org/html/rfc6020#section-9.3.4")));
    }

    private final List<RangeConstraint> rangeStatements;
    private final int fractionDigits;
    private final SchemaPath path;

    /**
     * Default Decimal64 Type Constructor. <br>
     * <br>
     * The initial range statements are set to implicit ranges for Decimal64 implied by the number of fraction
     * digits. Fraction digits MUST be defined as integer between 1 and 18 inclusively as defined interface
     * {@link DecimalTypeDefinition}<br>
     * If the fraction digits are not defined inner the definition boundaries
     * the constructor will throw {@link IllegalArgumentException}
     *
     * @param path
     * @param fractionDigits integer between 1 and 18 inclusively
     * @throws IllegalArgumentException
     *
     * @see DecimalTypeDefinition
     * @exception IllegalArgumentException
     * @deprecated Use static factory {@link #create(SchemaPath, Integer)}.
     */
    @Deprecated
    public Decimal64(final SchemaPath path, final Integer fractionDigits) {
        Preconditions.checkArgument(fractionDigits >= 1 && fractionDigits <= 18,
                "The number of fraction digits %s is outside of allowed range [1, 18]", fractionDigits);

        this.path = Preconditions.checkNotNull(path);
        this.fractionDigits = fractionDigits;
        this.rangeStatements = IMPLICIT_RANGE_STATEMENTS.get(fractionDigits - 1);
    }

    public static Decimal64 create(final SchemaPath path, final Integer fractionDigits) {
        return new Decimal64(path, fractionDigits);
    }

    /**
     * Returns unmodifiable List with default definition of Range Statements.
     *
     * @return unmodifiable List with default definition of Range Statements.
     */
    private List<RangeConstraint> defaultRangeStatements() {
        final List<RangeConstraint> rangeStmts = new ArrayList<RangeConstraint>();
        final BigDecimal min = new BigDecimal("-922337203685477580.8");
        final BigDecimal max = new BigDecimal("922337203685477580.7");
        final String rangeDescription = "Integer values between " + min + " and " + max + ", inclusively.";
        rangeStmts.add(BaseConstraints.newRangeConstraint(min, max, Optional.of(rangeDescription),
                Optional.of("https://tools.ietf.org/html/rfc6020#section-9.2.4")));
        return Collections.unmodifiableList(rangeStmts);
    }

    @Override
    public DecimalTypeDefinition getBaseType() {
        return null;
    }

    @Override
    public String getUnits() {
        return "";
    }

    @Override
    public Object getDefaultValue() {
        return null;
    }

    @Override
    public QName getQName() {
        return BaseTypes.DECIMAL64_QNAME;
    }

    @Override
    public SchemaPath getPath() {
        return path;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public String getReference() {
        return REFERENCE;
    }

    @Override
    public Status getStatus() {
        return Status.CURRENT;
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return Collections.emptyList();
    }

    @Override
    public List<RangeConstraint> getRangeConstraints() {
        return rangeStatements;
    }

    @Override
    public Integer getFractionDigits() {
        return fractionDigits;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + BaseTypes.DECIMAL64_QNAME.hashCode();
        result = prime * result + path.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Decimal64 other = (Decimal64) obj;
        return path.equals(other.path);
    }

    @Override
    public String toString() {
        return Decimal64.class.getSimpleName() + "[qname=" + BaseTypes.DECIMAL64_QNAME + ", fractionDigits=" + fractionDigits + "]";
    }
}
