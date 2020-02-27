/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.type;

import java.util.Collection;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.Int32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;

final class RestrictedInt32Type extends AbstractRangeRestrictedType<Int32TypeDefinition, Integer>
        implements Int32TypeDefinition {
    RestrictedInt32Type(final Int32TypeDefinition baseType, final SchemaPath path,
            final Collection<? extends UnknownSchemaNode> unknownSchemaNodes,
            final @Nullable RangeConstraint<Integer> rangeConstraint) {
        super(baseType, path, unknownSchemaNodes, rangeConstraint);
    }

    @Override
    public int hashCode() {
        return Int32TypeDefinition.hashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return Int32TypeDefinition.equals(this, obj);
    }

    @Override
    public String toString() {
        return Int32TypeDefinition.toString(this);
    }
}
