/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.BaseTypes;

final class BooleanBaseType extends BaseType<BooleanTypeDefinition> implements BooleanTypeDefinition {
    static final BooleanBaseType INSTANCE = new BooleanBaseType();

    private BooleanBaseType() {
        super(BaseTypes.BINARY_QNAME);
    }

    @Override
    public BooleanConstrainedTypeBuilder newConstrainedTypeBuilder(final SchemaPath path) {
        return new BooleanConstrainedTypeBuilder(this, path);
    }
}
