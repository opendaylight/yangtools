/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.util;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.parser.builder.api.DocumentedNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeAwareBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeDefinitionBuilder;

/**
 * Basic implementation for TypeAwareBuilder builders.
 */
public abstract class AbstractTypeAwareBuilder extends AbstractBuilder implements TypeAwareBuilder, DocumentedNodeBuilder {
    protected QName qname;
    protected TypeDefinition<?> type;
    protected TypeDefinitionBuilder typedef;
    protected String description;
    protected String reference;
    protected Status status = Status.CURRENT;
    protected boolean augmenting;
    protected boolean addedByUses;
    protected boolean configuration;

    protected AbstractTypeAwareBuilder(final String moduleName, final int line, final QName qname) {
        super(moduleName, line);
        this.qname = qname;
    }

    protected AbstractTypeAwareBuilder(final String moduleName, final int line, final QName qname,
            final SchemaPath path, final DataSchemaNode base) {
        super(moduleName, line);

        this.qname = qname;
        this.description = base.getDescription();
        this.reference = base.getReference();
        this.status = base.getStatus();
        this.augmenting = base.isAugmenting();
        this.addedByUses = base.isAddedByUses();
        this.configuration = base.isConfiguration();
        unknownNodes.addAll(base.getUnknownSchemaNodes());
    }

    @Override
    public QName getQName() {
        return qname;
    }

    @Override
    public TypeDefinition<?> getType() {
        return type;
    }

    @Override
    public TypeDefinitionBuilder getTypedef() {
        return typedef;
    }

    @Override
    public void setType(TypeDefinition<?> type) {
        this.type = type;
        this.typedef = null;
    }

    @Override
    public void setTypedef(TypeDefinitionBuilder typedef) {
        this.typedef = typedef;
        this.type = null;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getReference() {
        return this.reference;
    }

    @Override
    public void setReference(String reference) {
        this.reference = reference;
    }

    @Override
    public Status getStatus() {
        return this.status;
    }

    @Override
    public void setStatus(Status status) {
        this.status = status;
    }
}
