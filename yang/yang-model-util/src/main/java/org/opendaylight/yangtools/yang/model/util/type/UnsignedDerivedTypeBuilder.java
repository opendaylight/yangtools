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
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;

public final class UnsignedDerivedTypeBuilder extends RangedDerivedTypeBuilder<UnsignedIntegerTypeDefinition> {
    UnsignedDerivedTypeBuilder(final UnsignedIntegerTypeDefinition baseType, final SchemaPath path,
        final List<RangeConstraint> rangeConstraints) {
        super(baseType, path, rangeConstraints);
    }

    @Override
    public UnsignedDerivedType build() {
        return new UnsignedDerivedType(getBaseType(), getPath(), getDefaultValue(), getDescription(), getReference(),
            getStatus(), getUnits(), getRangeConstraints());
    }
}
