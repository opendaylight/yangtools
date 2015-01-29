/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import java.util.List;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.util.ExtendedType;

class DerivedInteger extends NormalizatedDerivedType<IntegerTypeDefinition> implements IntegerTypeDefinition {

    public DerivedInteger(final ExtendedType definition) {
        super(IntegerTypeDefinition.class, definition);
    }

    @Override
    IntegerTypeDefinition createDerived(final ExtendedType base) {
        return new DerivedInteger(base);
    }

    @Override
    public List<RangeConstraint> getRangeConstraints() {
        return delegate().getRangeConstraints();
    }
}