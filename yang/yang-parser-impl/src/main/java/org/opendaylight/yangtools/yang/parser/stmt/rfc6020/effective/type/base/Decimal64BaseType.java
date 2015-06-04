/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.base;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.TypeUtils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.RangeConstraintEffectiveImpl;

public class Decimal64BaseType implements DecimalTypeDefinition {

    private static final Integer MAX_FRACTION_DIGITS = 18;

    private static final QName QNAME = QName.create(YangConstants.RFC6020_YANG_MODULE, TypeUtils.DECIMAL64);

    private static final BigDecimal MIN_VALUE = new BigDecimal("-922337203685477580.8");
    private static final BigDecimal MAX_VALUE = new BigDecimal("922337203685477580.7");

    private static final String UNITS = "";
    private static final BigDecimal DEFAULT_VALUE = null;

    private static final String DESCRIPTION = "The decimal64 type represents a subset of the real numbers, which can "
            + "be represented by decimal numerals. The value space of decimal64 is the set of numbers that can "
            + "be obtained by multiplying a 64-bit signed integer by a negative power of ten, i.e., expressible as "
            + "'i x 10^-n' where i is an integer64 and n is an integer between 1 and 18, inclusively.";
    private static final String REFERENCE = "https://tools.ietf.org/html/rfc6020#section-9.3";

    private static final List<RangeConstraint> RANGE_CONSTRAINTS = defaultRangeStatements();
    private static final List<UnknownSchemaNode> UNKNOWN_SCHEMA_NODES = Collections.emptyList();

    private final Integer fractionDigits;

    private static final Status STATUS = Status.CURRENT;

    private final SchemaPath schemaPath;

    private Decimal64BaseType(final SchemaPath schemaPath, final Integer fractionDigits) {

        this.schemaPath = Preconditions.checkNotNull(schemaPath,
                String.format("SchemaPath in type %s must not be null", QNAME.getLocalName()));

        if (!((fractionDigits >= 1) && (fractionDigits <= MAX_FRACTION_DIGITS))) {
            throw new IllegalArgumentException(
                    "The fraction digits outside of boundaries. Fraction digits MUST be integer between 1 and 18 inclusively");
        }

        this.fractionDigits = fractionDigits;
    }

    public static Decimal64BaseType create(final SchemaPath path, final Integer fractionDigits) {
        return new Decimal64BaseType(path, fractionDigits);
    }

    private static List<RangeConstraint> defaultRangeStatements() {

        final List<RangeConstraint> rangeStmts = new ArrayList<>();
        final String rangeDescription = "Integer values between " + MIN_VALUE + " and " + MAX_VALUE + ", inclusively.";
        final String rangeReference = RangeConstraintEffectiveImpl.DEFAULT_REFERENCE;

        rangeStmts.add(new RangeConstraintEffectiveImpl(MIN_VALUE, MAX_VALUE, Optional.of(rangeDescription), Optional
                .of(rangeReference)));

        return ImmutableList.copyOf(rangeStmts);
    }

    @Override
    public QName getQName() {
        return QNAME;
    }

    @Override
    public SchemaPath getPath() {
        return schemaPath;
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
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return UNKNOWN_SCHEMA_NODES;
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
        return STATUS;
    }

    @Override
    public List<RangeConstraint> getRangeConstraints() {
        return RANGE_CONSTRAINTS;
    }

    @Override
    public Integer getFractionDigits() {
        return fractionDigits;
    }
}
