/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import java.util.List;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.util.ExtendedType;

class DerivedDecimal extends NormalizatedDerivedType<DecimalTypeDefinition> implements DecimalTypeDefinition {

    public DerivedDecimal(final ExtendedType definition) {
        super(DecimalTypeDefinition.class, definition);
    }

    @Override
    DecimalTypeDefinition createDerived(final ExtendedType base) {
        return new DerivedDecimal(base);
    }

    @Override
    public List<RangeConstraint> getRangeConstraints() {
        return delegate().getRangeConstraints();
    }

    @Override
    public Integer getFractionDigits() {
        return delegate().getFractionDigits();
    }
}