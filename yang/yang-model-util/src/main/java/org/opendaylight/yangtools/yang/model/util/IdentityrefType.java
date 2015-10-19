/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import com.google.common.base.Preconditions;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
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
public final class IdentityrefType implements IdentityrefTypeDefinition, Serializable {
    private static final long serialVersionUID = 1L;

    private static final QName NAME = BaseTypes.IDENTITYREF_QNAME;
    private static final String DESCRIPTION = "The identityref type is used to reference an existing identity.";
    private static final String REFERENCE = "https://tools.ietf.org/html/rfc6020#section-9.10";
    private static final String UNITS = "";

    private final IdentitySchemaNode identity;
    private final SchemaPath path;

    private IdentityrefType(final SchemaPath path, final IdentitySchemaNode baseIdentity) {
        this.path = Preconditions.checkNotNull(path, "Path must be specified");
        this.identity = Preconditions.checkNotNull(baseIdentity,"baseIdentity must be specified.");
    }

    /**
     * Constructs a new {@link IdentityrefTypeDefinition} definition.
     *
     * @param path Path to the definition.
     * @param baseIdentity Base Identity, all derived identities are valid arguments for instance of this type.
     * @return New identityref definition.
     */
    public static IdentityrefType create(final SchemaPath path, final IdentitySchemaNode baseIdentity) {
        return new IdentityrefType(path, baseIdentity);
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
    public IdentitySchemaNode getIdentity() {
        return identity;
    }

    @Override
    public IdentityrefTypeDefinition getBaseType() {
        return null;
    }

    @Override
    public String toString() {
        return "identityref " + identity.getQName().getLocalName();
    }
}
