/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
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
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;

final class RestrictedBinaryType extends AbstractLengthRestrictedType<BinaryTypeDefinition>
        implements BinaryTypeDefinition {
    RestrictedBinaryType(final BinaryTypeDefinition baseType, final SchemaPath path,
            final Collection<? extends UnknownSchemaNode> unknownSchemaNodes,
            final @Nullable LengthConstraint lengthConstraint) {
        super(baseType, path, unknownSchemaNodes, lengthConstraint);
    }

    @Override
    public int hashCode() {
        return BinaryTypeDefinition.hashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return BinaryTypeDefinition.equals(this, obj);
    }

    @Override
    public String toString() {
        return BinaryTypeDefinition.toString(this);
    }
}
