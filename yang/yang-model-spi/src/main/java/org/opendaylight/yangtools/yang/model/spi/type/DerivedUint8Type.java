/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.type;

import java.util.Collection;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.Uint8TypeDefinition;

final class DerivedUint8Type extends AbstractRangeRestrictedDerivedType<Uint8TypeDefinition, Uint8>
        implements Uint8TypeDefinition {
    DerivedUint8Type(final Uint8TypeDefinition baseType, final SchemaPath path,
            final Object defaultValue, final String description, final String reference, final Status status,
            final String units, final Collection<? extends UnknownSchemaNode> unknownSchemaNodes) {
        super(baseType, path, defaultValue, description, reference, status, units, unknownSchemaNodes);
    }

    @Override
    public int hashCode() {
        return Uint8TypeDefinition.hashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return Uint8TypeDefinition.equals(this, obj);
    }
}
