/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.ExtendedType;

class DerivedBoolean extends NormalizatedDerivedType<BooleanTypeDefinition> implements BooleanTypeDefinition {

    public DerivedBoolean(final ExtendedType definition) {
        super(BooleanTypeDefinition.class, definition);
    }

    @Override
    BooleanTypeDefinition createDerived(final ExtendedType base) {
        return new DerivedBoolean(base);
    }
}