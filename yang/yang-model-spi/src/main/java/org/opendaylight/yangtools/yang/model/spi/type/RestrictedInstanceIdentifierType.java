/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.type;

import java.util.Collection;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;

final class RestrictedInstanceIdentifierType extends AbstractRestrictedType<InstanceIdentifierTypeDefinition>
        implements InstanceIdentifierTypeDefinition {
    private final boolean requireInstance;

    RestrictedInstanceIdentifierType(final InstanceIdentifierTypeDefinition baseType, final QName qname,
            final Collection<? extends UnknownSchemaNode> unknownSchemaNodes, final boolean requireInstance) {
        super(baseType, qname, unknownSchemaNodes);
        this.requireInstance = requireInstance;
    }

    private RestrictedInstanceIdentifierType(final RestrictedInstanceIdentifierType original, final QName qname) {
        super(original, qname);
        this.requireInstance = original.requireInstance;
    }

    @Override
    RestrictedInstanceIdentifierType bindTo(final QName newQName) {
        return new RestrictedInstanceIdentifierType(this, newQName);
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
