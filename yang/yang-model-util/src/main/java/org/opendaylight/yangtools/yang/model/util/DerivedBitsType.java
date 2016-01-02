/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import java.util.List;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;

/**
 * @deprecated Deprecated {@link DerivedType} subclass.
 */
@Deprecated
class DerivedBitsType extends DerivedType<BitsTypeDefinition> implements BitsTypeDefinition {

    public DerivedBitsType(final ExtendedType definition) {
        super(BitsTypeDefinition.class, definition);
    }

    @Override
    BitsTypeDefinition createDerived(final ExtendedType base) {
        return new DerivedBitsType(base);
    }

    @Override
    public List<Bit> getBits() {
        return getBaseType().getBits();
    }
}