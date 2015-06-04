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
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.TypeUtils;

public class InstanceIdentifierBaseType implements InstanceIdentifierTypeDefinition {

    private static final InstanceIdentifierBaseType INSTANCE_WITH_REQUIRED_TRUE = new InstanceIdentifierBaseType(true);
    private static final InstanceIdentifierBaseType INSTANCE_WITH_REQUIRED_FALSE = new InstanceIdentifierBaseType(false);

    private static final QName QNAME = QName.create(YangConstants.RFC6020_YANG_MODULE, TypeUtils.INSTANCE_IDENTIFIER);
    private static final SchemaPath SCHEMA_PATH = SchemaPath.create(true, QNAME);

    private static final String UNITS = "";

    private static final String DESCRIPTION = "The instance-identifier built-in type is used to "
            + "uniquely identify a particular instance node in the data tree.";
    private static final String REFERENCE = "https://tools.ietf.org/html/rfc6020#section-9.13";

    private static final List<UnknownSchemaNode> UNKNOWN_SCHEMA_NODES = Collections.emptyList();

    private static final Status STATUS = Status.CURRENT;

    private final boolean requireInstance;

    private InstanceIdentifierBaseType(boolean requireInstance) {
        this.requireInstance = requireInstance;
    }

    public static InstanceIdentifierBaseType getInstance(final boolean requireInstance) {
        return requireInstance ? INSTANCE_WITH_REQUIRED_TRUE : INSTANCE_WITH_REQUIRED_FALSE;
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
    public InstanceIdentifierTypeDefinition getBaseType() {
        return null;
    }

    @Override
    public String getUnits() {
        return UNITS;
    }

    @Override
    public Object getDefaultValue() {
        return requireInstance;
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
    public RevisionAwareXPath getPathStatement() {
        return null;
    }

    @Override
    public boolean requireInstance() {
        return requireInstance;
    }
}
