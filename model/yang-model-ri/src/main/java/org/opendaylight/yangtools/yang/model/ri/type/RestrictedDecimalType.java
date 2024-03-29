/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.type;

import java.util.Collection;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.Decimal64;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;

final class RestrictedDecimalType extends AbstractRangeRestrictedType<DecimalTypeDefinition, Decimal64>
        implements DecimalTypeDefinition {
    RestrictedDecimalType(final DecimalTypeDefinition baseType, final QName qname,
            final Collection<? extends UnknownSchemaNode> unknownSchemaNodes,
            final @Nullable RangeConstraint<Decimal64> rangeConstraint) {
        super(baseType, qname, unknownSchemaNodes, rangeConstraint);
    }

    @Override
    public int getFractionDigits() {
        return getBaseType().getFractionDigits();
    }

    @Override
    public int hashCode() {
        return DecimalTypeDefinition.hashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return DecimalTypeDefinition.equals(this, obj);
    }

    @Override
    public String toString() {
        return DecimalTypeDefinition.toString(this);
    }
}
