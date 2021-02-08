/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.type;

import java.util.Collection;
import java.util.Optional;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeRestrictedTypeDefinition;

abstract class AbstractRangeRestrictedDerivedType<T extends RangeRestrictedTypeDefinition<T, N>,
        N extends Number & Comparable<N>> extends AbstractDerivedType<T>
        implements RangeRestrictedTypeDefinition<T, N> {

    AbstractRangeRestrictedDerivedType(final T baseType, final QName qname,
            final Object defaultValue, final String description, final String reference, final Status status,
            final String units, final Collection<? extends UnknownSchemaNode> unknownSchemaNodes) {
        super(baseType, qname, defaultValue, description, reference, status, units, unknownSchemaNodes);
    }

    AbstractRangeRestrictedDerivedType(final AbstractRangeRestrictedDerivedType<T, N> original, final QName qname) {
        super(original, qname);
    }

    @Override
    public final Optional<RangeConstraint<N>> getRangeConstraint() {
        return baseType().getRangeConstraint();
    }
}
