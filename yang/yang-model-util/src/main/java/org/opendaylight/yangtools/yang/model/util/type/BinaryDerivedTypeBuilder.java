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
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;

public final class BinaryDerivedTypeBuilder extends LengthDerivedTypeBuilder<BinaryTypeDefinition> {
    BinaryDerivedTypeBuilder(final BinaryTypeDefinition baseType, final SchemaPath path,
        final List<LengthConstraint> lengthConstraints) {
        super(baseType, path, lengthConstraints);
    }

    @Override
    public BinaryDerivedType build() {
        return new BinaryDerivedType(getBaseType(), getPath(), getDefaultValue(), getDescription(), getReference(),
            getStatus(), getUnits(), getLengthConstraints());
    }
}
