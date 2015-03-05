/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import java.util.List;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;

class DerivedUnsignedIntegerType extends DerivedType<UnsignedIntegerTypeDefinition> implements
        UnsignedIntegerTypeDefinition {

    public DerivedUnsignedIntegerType(final ExtendedType definition) {
        super(UnsignedIntegerTypeDefinition.class, definition);
    }

    @Override
    UnsignedIntegerTypeDefinition createDerived(final ExtendedType base) {
        return new DerivedUnsignedIntegerType(base);
    }

    @Override
    public List<RangeConstraint> getRangeConstraints() {
        return delegate().getRangeConstraints();
    }
}