/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;

public final class IdentityrefDerivedType extends DerivedType<IdentityrefTypeDefinition> implements IdentityrefTypeDefinition {
    IdentityrefDerivedType(final IdentityrefTypeDefinition baseType, final SchemaPath path, final Object defaultValue,
        final String description, final String reference, final Status status, final String units) {
        super(baseType, path, defaultValue, description, reference, status, units);
    }

    @Override
    public IdentitySchemaNode getIdentity() {
        return getBaseType().getIdentity();
    }
}
