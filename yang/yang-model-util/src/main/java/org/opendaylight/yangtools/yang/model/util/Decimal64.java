/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;

import com.google.common.base.Optional;

/**
 * The <code>default</code> implementation of Decimal Type Definition interface.
 *
 *
 * @see DecimalTypeDefinition
 */
public final class Decimal64 implements DecimalTypeDefinition {
    private static final QName NAME = BaseTypes.DECIMAL64_QNAME;
    private final SchemaPath path;
    private static final String UNITS = "";
    private static final BigDecimal DEFAULT_VALUE = null;

    private static final String DESCRIPTION = "The decimal64 type represents a subset of the real numbers, which can "
            + "be represented by decimal numerals. The value space of decimal64 is the set of numbers that can "
            + "be obtained by multiplying a 64-bit signed integer by a negative power of ten, i.e., expressible as "
            + "'i x 10^-n' where i is an integer64 and n is an integer between 1 and 18, inclusively.";

    private static final String REFERENCE = "https://tools.ietf.org/html/rfc6020#section-9.3";

    private final List<RangeConstraint> rangeStatements;
    private final Integer fractionDigits;
    private static final int MAX_NUMBER_OF_FRACTION_DIGITS = 18;

    /**
     * Default Decimal64 Type Constructor. <br>
     * <br>
     * The initial range statements are set to Decimal64
     * <code>min=-922337203685477580.8</code> and
     * <code>max=922337203685477580.7</code> <br>
     * The fractions digits MUST be defined as integer between 1 and
     * {@link #MAX_NUMBER_OF_FRACTION_DIGITS} inclusively as defined interface
     * {@link DecimalTypeDefinition} <br>
     * If the fraction digits are not defined inner the definition boundaries
     * the constructor will throw {@link IllegalArgumentException}
     *
     * @param path
     * @param fractionDigits
     *            integer between 1 and 18 inclusively
     *
     * @see DecimalTypeDefinition
     * @exception IllegalArgumentException
     * @deprecated Use static factory {@link #create(SchemaPath, Integer)}.
     */
    @Deprecated
    public Decimal64(final SchemaPath path, final Integer fractionDigits) {
        if (!((fractionDigits.intValue() >= 1) && (fractionDigits.intValue() <= MAX_NUMBER_OF_FRACTION_DIGITS))) {
            throw new IllegalArgumentException(
                    "The fraction digits outside of boundaries. Fraction digits MUST be integer between 1 and 18 inclusively");
        }
        this.fractionDigits = fractionDigits;
        rangeStatements = getDefaultRangeStatements(fractionDigits);
        this.path = path;
    }

    public static Decimal64 create(final SchemaPath path, final Integer fractionDigits) {
        return new Decimal64(path, fractionDigits);
    }

    /**
     * Returns unmodifiable List with default definition of Range Statements.
     *
     * @return unmodifiable List with default definition of Range Statements.
     */
    private List<RangeConstraint> getDefaultRangeStatements(final Integer fractionDigits) {
        final List<RangeConstraint> rangeStmts = new ArrayList<>();
        final BigDecimal min = new BigDecimal(new BigInteger("-9223372036854775808"), fractionDigits);
        final BigDecimal max = new BigDecimal(new BigInteger("9223372036854775807"), fractionDigits);
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
        return UNITS;
    }

    @Override
    public Object getDefaultValue() {
        return DEFAULT_VALUE;
    }

    @Override
    public QName getQName() {
        return NAME;
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
        result = prime * result + ((NAME == null) ? 0 : NAME.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
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
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return Decimal64.class.getSimpleName() + "[qname=" + NAME + ", fractionDigits=" + fractionDigits + "]";
    }
}
