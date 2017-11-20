/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.IdentityTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;

final class DerivedIdentityrefType extends AbstractDerivedType<IdentityrefTypeDefinition>
        implements IdentityrefTypeDefinition {
    DerivedIdentityrefType(final IdentityrefTypeDefinition baseType, final SchemaPath path,
        final IdentityTypeDefinition defaultValue, final String description, final String reference,
        final Status status, final String units, final Collection<UnknownSchemaNode> unknownSchemaNodes) {
        super(baseType, path, defaultValue, description, reference, status, units, unknownSchemaNodes);
    }

    @Override
    public Set<IdentitySchemaNode> getIdentities() {
        return baseType().getIdentities();
    }

    @Override
    public Optional<? extends IdentityTypeDefinition> getDefaultValue() {
        return super.getDefaultValue().map(IdentityTypeDefinition.class::cast);
    }

    @Override
    public int hashCode() {
        return IdentityrefTypeDefinition.hashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return IdentityrefTypeDefinition.equals(this, obj);
    }
}
