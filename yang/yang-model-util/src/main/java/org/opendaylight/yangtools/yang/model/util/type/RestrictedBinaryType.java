/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.collect.RangeMap;
import java.util.Collection;
import org.opendaylight.yangtools.yang.model.api.ConstraintMetaDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;

final class RestrictedBinaryType extends AbstractLengthRestrictedType<BinaryTypeDefinition>
        implements BinaryTypeDefinition {
    RestrictedBinaryType(final BinaryTypeDefinition baseType, final SchemaPath path,
            final Collection<UnknownSchemaNode> unknownSchemaNodes,
            final RangeMap<Integer, ConstraintMetaDefinition> lengthConstraints) {
        super(baseType, path, unknownSchemaNodes, lengthConstraints);
    }

    @Override
    public int hashCode() {
        return TypeDefinitions.hashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return TypeDefinitions.equals(this, obj);
    }

    @Override
    public String toString() {
        return TypeDefinitions.toString(this);
    }
}
