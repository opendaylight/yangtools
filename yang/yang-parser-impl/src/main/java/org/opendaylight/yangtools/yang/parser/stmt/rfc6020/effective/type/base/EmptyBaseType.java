/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.base;

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

public class EmptyBaseType implements YangBaseType {

    private static final EmptyBaseType INSTANCE = new EmptyBaseType();

    private static final QName QNAME = QName.create(YangConstants.RFC6020_YANG_MODULE, TypeUtils.EMPTY);
    private static final SchemaPath SCHEMA_PATH = SchemaPath.create(true, QNAME);

    private static final String UNITS = null;
    private static final Object DEFAULT_VALUE = null;

    private static final String DESCRIPTION = "The empty built-in type represents a leaf that does not have any value, it conveys information by its presence or absence.";
    private static final String REFERENCE = "https://tools.ietf.org/html/rfc6020#page-131";

    private static final List<RangeConstraint> RANGE_CONSTRAINTS = Collections.emptyList();
    private static final List<LengthConstraint> LENGTH_CONSTRAINTS = Collections.emptyList();
    private static final List<PatternConstraint> PATTERN_CONSTRAINTS = Collections.emptyList();
    private static final List<UnknownSchemaNode> UNKNOWN_SCHEMA_NODES = Collections.emptyList();

    private static final Integer FRACTION_DIGITS = null;

    private static final Status STATUS = Status.CURRENT;

    private EmptyBaseType() {
    }

    public static YangBaseType getInstance() {
        return INSTANCE;
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
