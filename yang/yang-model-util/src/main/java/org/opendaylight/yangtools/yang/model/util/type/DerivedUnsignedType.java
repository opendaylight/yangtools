/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;

final class DerivedUnsignedType extends AbstractDerivedType<UnsignedIntegerTypeDefinition>
        implements UnsignedIntegerTypeDefinition {

    DerivedUnsignedType(final UnsignedIntegerTypeDefinition baseType, final SchemaPath path,
            final Object defaultValue, final String description, final String reference, final Status status,
            final String units, final Collection<UnknownSchemaNode> unknownSchemaNodes) {
        super(baseType, path, defaultValue, description, reference, status, units, unknownSchemaNodes);
    }

    @Nonnull
    @Override
    public List<RangeConstraint> getRangeConstraints() {
        return baseType().getRangeConstraints();
    }

    @Override
    public int hashCode() {
        return TypeDefinitions.hashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return TypeDefinitions.equals(this, obj);
    }
}
