/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;

public final class EmptyConstrainedTypeBuilder extends ConstrainedTypeBuilder<EmptyTypeDefinition> {
    EmptyConstrainedTypeBuilder(final EmptyTypeDefinition baseType, final SchemaPath path) {
        super(baseType, path);
    }

    @Override
    public EmptyConstrainedType build() {
        return new EmptyConstrainedType(getBaseType(), getPath(), getUnknownSchemaNodes());
    }
}
