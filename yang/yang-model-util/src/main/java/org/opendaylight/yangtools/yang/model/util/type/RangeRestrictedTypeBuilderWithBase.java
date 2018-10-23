/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeRestrictedTypeDefinition;

abstract class RangeRestrictedTypeBuilderWithBase<T extends RangeRestrictedTypeDefinition<T, N>,
        N extends Number & Comparable<N>> extends RangeRestrictedTypeBuilder<T, N> {

    RangeRestrictedTypeBuilderWithBase(final @NonNull T baseType, final @NonNull SchemaPath path) {
        super(requireNonNull(baseType), path);
    }

    @Override
    final T buildType() {
        return buildType(calculateRangeConstraint(getBaseType().getRangeConstraint().get()));
    }

    abstract T buildType(RangeConstraint<N> rangeConstraints);
}
