/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import java.util.Collection;
import java.util.List;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;

public final class BitsConstrainedType extends ConstrainedType<BitsTypeDefinition> implements BitsTypeDefinition {
    BitsConstrainedType(final BitsTypeDefinition baseType, final SchemaPath path,
            final Collection<UnknownSchemaNode> unknownSchemaNodes) {
        super(baseType, path, unknownSchemaNodes);
    }

    @Override
    public List<Bit> getBits() {
        return getBaseType().getBits();
    }

    @Override
    public BitsDerivedTypeBuilder newDerivedTypeBuilder(final SchemaPath path) {
        return new BitsDerivedTypeBuilder(getBaseType(), path);
    }
}
