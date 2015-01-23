/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.HashSet;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.parser.builder.api.UnknownSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.util.AbstractSchemaNodeBuilder;

public final class IdentitySchemaNodeBuilder extends AbstractSchemaNodeBuilder {
    private IdentitySchemaNodeImpl instance;
    private IdentitySchemaNode baseIdentity;
    private IdentitySchemaNodeBuilder baseIdentityBuilder;
    private final Set<IdentitySchemaNode> derivedIdentities = new HashSet<>();
    private String baseIdentityName;

    IdentitySchemaNodeBuilder(final String moduleName, final int line, final QName qname, final SchemaPath path) {
        super(moduleName, line, qname);
        this.schemaPath = Preconditions.checkNotNull(path, "Schema Path must not be null");
    }

    IdentitySchemaNodeBuilder(final String moduleName, final IdentitySchemaNode base) {
        super(moduleName, 0, base.getQName());
        schemaPath = base.getPath();
        derivedIdentities.addAll(base.getDerivedIdentities());
        unknownNodes.addAll(base.getUnknownSchemaNodes());
    }

    @Override
    public IdentitySchemaNode build() {
        if (instance != null) {
            return instance;
        }

        instance = new IdentitySchemaNodeImpl(qname, schemaPath, derivedIdentities);

        instance.description = description;
        instance.reference = reference;
        instance.status = status;

        if (baseIdentityBuilder != null) {
            baseIdentityBuilder.addDerivedIdentity(instance);
            baseIdentity = baseIdentityBuilder.build();
        }
        instance.baseIdentity = baseIdentity;

        // UNKNOWN NODES
        for (UnknownSchemaNodeBuilder b : addedUnknownNodes) {
            unknownNodes.add(b.build());
        }
        instance.unknownNodes = ImmutableList.copyOf(unknownNodes);

        return instance;
    }

    public String getBaseIdentityName() {
        return baseIdentityName;
    }

    public void setBaseIdentityName(final String baseIdentityName) {
        this.baseIdentityName = baseIdentityName;
    }

    public void setBaseIdentity(final IdentitySchemaNodeBuilder baseType) {
        this.baseIdentityBuilder = baseType;
    }

    void addDerivedIdentity(final IdentitySchemaNode derivedIdentity) {
        if (derivedIdentity != null) {
            derivedIdentities.add(derivedIdentity);
        }
    }

    @Override
    public String toString() {
        return "identity " + qname.getLocalName();
    }

}
