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
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.RangeConstraintEffectiveImpl;

abstract class UnsignedIntegerBaseType implements IntegerTypeDefinition {

    protected static final String UNITS = "";
    protected static final Object DEFAULT_VALUE = null;

    protected static final String REFERENCE = "https://tools.ietf.org/html/rfc6020#section-9.2";

    protected static final List<UnknownSchemaNode> UNKNOWN_SCHEMA_NODES = Collections.emptyList();

    protected static final Status STATUS = Status.CURRENT;

    protected final QName QNAME;
    protected final SchemaPath SCHEMA_PATH;

    protected final Number MIN_RANGE = 0;
    protected final Number MAX_RANGE;
    protected final List<RangeConstraint> RANGE_CONSTRAINTS;
    protected final String DESCRIPTION;

    protected UnsignedIntegerBaseType(QName qName, SchemaPath schemaPath, Number maxRange) {

        QNAME = qName;
        SCHEMA_PATH = schemaPath;

        MAX_RANGE = maxRange;

        RANGE_CONSTRAINTS = initRangeConstraints();

        DESCRIPTION = QNAME.getLocalName() + " represents integer values between " + MIN_RANGE + " and " + MAX_RANGE
                + ", inclusively.";
    }

    protected List<RangeConstraint> initRangeConstraints() {

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
    public IntegerTypeDefinition getBaseType() {
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
}
