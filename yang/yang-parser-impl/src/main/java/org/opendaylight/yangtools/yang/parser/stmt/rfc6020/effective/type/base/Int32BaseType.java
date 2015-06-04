/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.base;

import com.google.common.base.Optional;

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

public class Int32BaseType implements YangBaseType {

    private static final Int32BaseType INSTANCE = new Int32BaseType();

    private static final QName QNAME = QName.create(YangConstants.RFC6020_YANG_MODULE, TypeUtils.INT32);
    private static final SchemaPath SCHEMA_PATH = SchemaPath.create(true, QNAME);

    private static final Number MIN_RANGE = Integer.MIN_VALUE;
    private static final Number MAX_RANGE = Integer.MAX_VALUE;

    private static final String UNITS = "";
    private static final Object DEFAULT_VALUE = null;

    private static final String DESCRIPTION = TypeUtils.INT32 + " represents integer values between " + MIN_RANGE
            + " and " + MAX_RANGE + ", inclusively.";
    private static final String REFERENCE = "https://tools.ietf.org/html/rfc6020#section-9.2";

    private static final List<RangeConstraint> RANGE_CONSTRAINTS = initRangeConstraints();
    private static final List<LengthConstraint> LENGTH_CONSTRAINTS = Collections.emptyList();
    private static final List<PatternConstraint> PATTERN_CONSTRAINTS = Collections.emptyList();
    private static final List<UnknownSchemaNode> UNKNOWN_SCHEMA_NODES = Collections.emptyList();

    private static final Integer FRACTION_DIGITS = null;

    private static final Status STATUS = Status.CURRENT;

    private Int32BaseType() {
    }

    public static YangBaseType getInstance() {
        return INSTANCE;
    }

    private static List<RangeConstraint> initRangeConstraints() {

        final String rangeDescription = "Integer values between " + MIN_RANGE + " and " + MAX_RANGE + ", inclusively.";

        final RangeConstraint defaultRange = new RangeConstraintEffectiveImpl(MIN_RANGE, MAX_RANGE,
                Optional.of(rangeDescription), Optional.of(RangeConstraintEffectiveImpl.DEFAULT_REFERENCE));

        return Collections.singletonList(defaultRange);
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
