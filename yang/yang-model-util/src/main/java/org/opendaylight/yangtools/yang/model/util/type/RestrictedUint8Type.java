/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import java.util.Collection;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.api.type.Uint8TypeDefinition;

final class RestrictedUint8Type extends AbstractRangeRestrictedType<Uint8TypeDefinition, Short>
        implements Uint8TypeDefinition {

    RestrictedUint8Type(final Uint8TypeDefinition baseType, final SchemaPath path,
        final Collection<UnknownSchemaNode> unknownSchemaNodes,
        final @Nullable RangeConstraint<Short> rangeConstraint) {
        super(baseType, path, unknownSchemaNodes, rangeConstraint);
    }

    @Override
    public int hashCode() {
        return Uint8TypeDefinition.hashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return Uint8TypeDefinition.equals(this, obj);
    }

    @Override
    public String toString() {
        return Uint8TypeDefinition.toString(this);
    }
}
