/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import static java.util.Objects.requireNonNull;

import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;

public final class InstanceIdentifierTypeBuilder
        extends RequireInstanceRestrictedTypeBuilder<InstanceIdentifierTypeDefinition> {

    InstanceIdentifierTypeBuilder(final InstanceIdentifierTypeDefinition baseType,
            final SchemaPath path) {
        super(requireNonNull(baseType), path);
    }

    @Override
    InstanceIdentifierTypeDefinition buildType() {
        final InstanceIdentifierTypeDefinition base = getBaseType();
        if (getRequireInstance() == base.requireInstance()) {
            return base;
        }

        return new RestrictedInstanceIdentifierType(base, getPath(), getUnknownSchemaNodes(), getRequireInstance());
    }
}
