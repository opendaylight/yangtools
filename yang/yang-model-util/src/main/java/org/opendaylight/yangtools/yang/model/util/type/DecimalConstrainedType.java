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
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;

public final class DecimalConstrainedType extends RangedConstrainedType<DecimalTypeDefinition>
        implements DecimalTypeDefinition {
    DecimalConstrainedType(final DecimalTypeDefinition baseType, final SchemaPath path,
        final Collection<UnknownSchemaNode> unknownSchemaNodes, final Collection<RangeConstraint> rangeConstraints) {
        super(baseType, path, unknownSchemaNodes, rangeConstraints);
    }

    @Override
    public DecimalDerivedTypeBuilder newDerivedTypeBuilder(final SchemaPath path) {
        return new DecimalDerivedTypeBuilder(getBaseType(), path, getRangeConstraints());
    }

    @Override
    public Integer getFractionDigits() {
        return getBaseType().getFractionDigits();
    }
}
