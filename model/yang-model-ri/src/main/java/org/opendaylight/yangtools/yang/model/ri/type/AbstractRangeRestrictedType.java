/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.type;

import java.util.Optional;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeRestrictedTypeDefinition;

abstract class AbstractRangeRestrictedType<T extends RangeRestrictedTypeDefinition<T, N>,
        N extends Number & Comparable<N>> extends AbstractRestrictedType<T>
        implements RangeRestrictedTypeDefinition<T, N> {
    private final @Nullable RangeConstraint<N> rangeConstraint;

    AbstractRangeRestrictedType(final T baseType, final QName qname,
            final @Nullable RangeConstraint<N> rangeConstraint) {
        super(baseType, qname);
        this.rangeConstraint = rangeConstraint;
    }

    @Override
    public final Optional<RangeConstraint<N>> getRangeConstraint() {
        return Optional.ofNullable(rangeConstraint);
    }
}
