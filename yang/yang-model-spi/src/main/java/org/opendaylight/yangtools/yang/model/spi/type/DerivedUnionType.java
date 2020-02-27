/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.type;

import java.util.Collection;
import java.util.List;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;

final class DerivedUnionType extends AbstractDerivedType<UnionTypeDefinition> implements UnionTypeDefinition {
    DerivedUnionType(final UnionTypeDefinition baseType, final SchemaPath path, final Object defaultValue,
        final String description, final String reference, final Status status, final String units,
        final Collection<? extends UnknownSchemaNode> unknownSchemaNodes) {
        super(baseType, path, defaultValue, description, reference, status, units, unknownSchemaNodes);
    }

    @Override
    public List<TypeDefinition<?>> getTypes() {
        return baseType().getTypes();
    }

    @Override
    public int hashCode() {
        return UnionTypeDefinition.hashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return UnionTypeDefinition.equals(this, obj);
    }
}
