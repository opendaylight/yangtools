/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import java.util.List;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;

public final class DecimalDerivedTypeBuilder extends RangedDerivedTypeBuilder<DecimalTypeDefinition> {
    DecimalDerivedTypeBuilder(final DecimalTypeDefinition baseType, final SchemaPath path,
        final List<RangeConstraint> rangeConstraints) {
        super(baseType, path, rangeConstraints);
    }

    @Override
    public DecimalDerivedType build() {
        return new DecimalDerivedType(getBaseType(), getPath(), getDefaultValue(), getDescription(), getReference(),
            getStatus(), getUnits(), getRangeConstraints());
    }
}
