/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.base;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.TypeUtils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.RangeConstraintEffectiveImpl;

public class Decimal64BaseType implements YangBaseType {

    private static final Decimal64BaseType INSTANCE = new Decimal64BaseType();

    private static final QName QNAME = QName.create(YangConstants.RFC6020_YANG_MODULE, TypeUtils.DECIMAL64);
    private static final SchemaPath SCHEMA_PATH = SchemaPath.create(true, QNAME);

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
    private static final List<LengthConstraint> LENGTH_CONSTRAINTS = Collections.emptyList();
    private static final List<PatternConstraint> PATTERN_CONSTRAINTS = Collections.emptyList();
    private static final List<UnknownSchemaNode> UNKNOWN_SCHEMA_NODES = Collections.emptyList();

    private static final Integer FRACTION_DIGITS = null;

    private static final Status STATUS = Status.CURRENT;

    private Decimal64BaseType() {
    }

    public static YangBaseType getInstance() {
        return INSTANCE;
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
        return SCHEMA_PATH;
    }

    @Override
    public TypeDefinition<?> getBaseType() {
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
    public List<LengthConstraint> getLengthConstraints() {
        return LENGTH_CONSTRAINTS;
    }

    @Override
    public List<PatternConstraint> getPatternConstraints() {
        return PATTERN_CONSTRAINTS;
    }

    @Override
    public Integer getFractionDigits() {
        return FRACTION_DIGITS;
    }
}
