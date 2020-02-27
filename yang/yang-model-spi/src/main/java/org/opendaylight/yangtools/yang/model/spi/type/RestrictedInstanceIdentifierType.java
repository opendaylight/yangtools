/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.type;

import java.util.Collection;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;

final class RestrictedInstanceIdentifierType extends AbstractRestrictedType<InstanceIdentifierTypeDefinition>
        implements InstanceIdentifierTypeDefinition {
    private final boolean requireInstance;

    RestrictedInstanceIdentifierType(final InstanceIdentifierTypeDefinition baseType, final SchemaPath path,
            final Collection<? extends UnknownSchemaNode> unknownSchemaNodes, final boolean requireInstance) {
        super(baseType, path, unknownSchemaNodes);
        this.requireInstance = requireInstance;
    }

    @Override
    public boolean requireInstance() {
        return requireInstance;
    }

    @Override
    public int hashCode() {
        return InstanceIdentifierTypeDefinition.hashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return InstanceIdentifierTypeDefinition.equals(this, obj);
    }

    @Override
    public String toString() {
        return InstanceIdentifierTypeDefinition.toString(this);
    }
}
