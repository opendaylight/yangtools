/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.util;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeAwareBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeDefinitionBuilder;

/**
 * Basic implementation for TypeAwareBuilder builders.
 */
public abstract class AbstractTypeAwareBuilder extends AbstractBuilder implements TypeAwareBuilder {
    protected QName qname;
    protected TypeDefinition<?> type;
    protected TypeDefinitionBuilder typedef;

    private QName baseTypeName;

    protected AbstractTypeAwareBuilder(final String moduleName, final int line, final QName qname) {
        super(moduleName, line);
        this.qname = qname;
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
    public QName getTypeQName() {
        return baseTypeName;
    }

    @Override
    public void setTypeQName(QName qname) {
        this.baseTypeName = qname;
    }

}
