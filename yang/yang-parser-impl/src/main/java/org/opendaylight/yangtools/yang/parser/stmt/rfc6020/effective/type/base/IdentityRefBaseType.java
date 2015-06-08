/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.base;

import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.TypeUtils;

public class IdentityRefBaseType implements IdentityrefTypeDefinition {

    private static final QName QNAME = QName.create(YangConstants.RFC6020_YANG_MODULE, TypeUtils.IDENTITY_REF);

    private static final String UNITS = "";

    private static final String DESCRIPTION = "The identityref type is used to reference an existing identity.";
    private static final String REFERENCE = "https://tools.ietf.org/html/rfc6020#section-9.10";

    private static final List<UnknownSchemaNode> UNKNOWN_SCHEMA_NODES = Collections.emptyList();

    private static final Status STATUS = Status.CURRENT;

    private final SchemaPath schemaPath;
    private final IdentitySchemaNode identity;

    private IdentityRefBaseType(final SchemaPath schemaPath, final IdentitySchemaNode baseIdentity) {

        this.schemaPath = Preconditions.checkNotNull(schemaPath,
                String.format("SchemaPath in type %s must not be null", QNAME.getLocalName()));
        this.identity = Preconditions.checkNotNull(baseIdentity,
                String.format("Base identity in identityref must be specified in path %s.", schemaPath));
    }

    public static IdentityRefBaseType getInstance(final SchemaPath schemaPath, final IdentitySchemaNode baseIdentity) {
        return new IdentityRefBaseType(schemaPath, baseIdentity);
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
    public IdentityrefTypeDefinition getBaseType() {
        return null;
    }

    @Override
    public String getUnits() {
        return UNITS;
    }

    @Override
    public Object getDefaultValue() {
        return identity;
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
    public IdentitySchemaNode getIdentity() {
        return identity;
    }
}
