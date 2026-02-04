/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.type;

import org.opendaylight.yangtools.yang.common.Decimal64;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;

final class DerivedDecimalType extends AbstractRangeRestrictedDerivedType<DecimalTypeDefinition, Decimal64>
        implements DecimalTypeDefinition {
    DerivedDecimalType(final DecimalTypeDefinition baseType, final QName qname, final Object defaultValue,
            final String description, final String reference, final Status status, final String units) {
        super(baseType, qname, defaultValue, description, reference, status, units);
    }

    @Override
    public int getFractionDigits() {
        return baseType().getFractionDigits();
    }

    @Override
    public int hashCode() {
        return DecimalTypeDefinition.hashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return DecimalTypeDefinition.equals(this, obj);
    }
}
