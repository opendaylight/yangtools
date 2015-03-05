/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import java.util.List;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;

class DerivedEnumType extends DerivedType<EnumTypeDefinition> implements EnumTypeDefinition {

    public DerivedEnumType(final ExtendedType definition) {
        super(EnumTypeDefinition.class, definition);
    }

    @Override
    EnumTypeDefinition createDerived(final ExtendedType base) {
        return new DerivedEnumType(base);
    }

    @Override
    public List<EnumPair> getValues() {
        return getBaseType().getValues();
    }
}