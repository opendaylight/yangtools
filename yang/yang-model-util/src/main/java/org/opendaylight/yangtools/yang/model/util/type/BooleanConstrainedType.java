/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import java.util.Collection;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;

public final class BooleanConstrainedType extends ConstrainedType<BooleanTypeDefinition> {
    BooleanConstrainedType(final BooleanTypeDefinition baseType, final SchemaPath path,
            final Collection<UnknownSchemaNode> unknownSchemaNodes) {
        super(baseType, path, unknownSchemaNodes);
    }

    @Override
    public BooleanDerivedTypeBuilder newDerivedTypeBuilder(final SchemaPath path) {
        return new BooleanDerivedTypeBuilder(getBaseType(), getPath());
    }
}
