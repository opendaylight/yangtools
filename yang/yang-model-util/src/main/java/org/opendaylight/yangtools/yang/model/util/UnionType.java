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
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;

public final class UnionType implements UnionTypeDefinition {
    private final QName name = BaseTypes.constructQName("union");
    private final SchemaPath path = BaseTypes.schemaPath(name);
    private static final String DESCRIPTION = "The union built-in type represents a value that corresponds to one of its member types.";
    private static final String REFERENCE = "https://tools.ietf.org/html/rfc6020#section-9.12";
    private final List<TypeDefinition<?>> types;

    public UnionType(List<TypeDefinition<?>> types) {
        if (types == null) {
            throw new IllegalArgumentException("When the type is 'union', the 'type' statement MUST be present.");
        }
        this.types = types;
    }

    @Override
    public UnionTypeDefinition getBaseType() {
        return null;
    }

    @Override
    public String getUnits() {
        return null;
    }

    @Override
    public Object getDefaultValue() {
        return null;
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
    public List<TypeDefinition<?>> getTypes() {
        return types;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((types == null) ? 0 : types.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        UnionType other = (UnionType) obj;
        if (!types.equals(other.types)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("type ");
        builder.append(name);
        builder.append(" (types=[");
        for (TypeDefinition<?> td : types) {
            builder.append(", " + td.getQName().getLocalName());
        }
        builder.append("]");
        return builder.toString();
    }

}
