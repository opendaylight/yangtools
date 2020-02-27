/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.type;

import java.util.Collection;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.Int32TypeDefinition;

final class DerivedInt32Type extends AbstractRangeRestrictedDerivedType<Int32TypeDefinition, Integer>
        implements Int32TypeDefinition {
    DerivedInt32Type(final Int32TypeDefinition baseType, final SchemaPath path, final Object defaultValue,
            final String description, final String reference, final Status status, final String units,
            final Collection<? extends UnknownSchemaNode> unknownSchemaNodes) {
        super(baseType, path, defaultValue, description, reference, status, units, unknownSchemaNodes);
    }

    @Override
    public int hashCode() {
        return Int32TypeDefinition.hashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return Int32TypeDefinition.equals(this, obj);
    }
}
