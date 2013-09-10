/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import java.util.Collections;
import java.util.List;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;

/**
 * The <code>default</code> implementation of Identityref Type Definition
 * interface.
 * 
 * @see IdentityrefTypeDefinition
 */
public final class IdentityrefType implements IdentityrefTypeDefinition {
    private final QName name = BaseTypes.constructQName("identityref");
    private final SchemaPath path;
    private static final String DESCRIPTION = "The identityref type is used to reference an existing identity.";
    private static final String REFERENCE = "https://tools.ietf.org/html/rfc6020#section-9.10";
    private final IdentityrefTypeDefinition baseType;
    private final QName identity;
    private static final String UNITS = "";

    public IdentityrefType(QName identity, SchemaPath schemaPath) {
        this.identity = identity;
        this.path = schemaPath;
        this.baseType = this;
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
    public QName getQName() {
        return name;
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
    public QName getIdentity() {
        return identity;
    }

    @Override
    public IdentityrefTypeDefinition getBaseType() {
        return baseType;
    }

    @Override
    public String toString() {
        return "identityref " + identity.getLocalName();
    }

}
