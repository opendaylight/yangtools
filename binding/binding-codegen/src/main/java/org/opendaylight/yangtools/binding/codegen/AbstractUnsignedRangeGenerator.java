/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

abstract class AbstractUnsignedRangeGenerator<T extends Number & Comparable<T>>
        extends AbstractPrimitiveRangeGenerator<T> {
    AbstractUnsignedRangeGenerator(final Class<T> typeClass, final String primitiveName, final T minValue,
            final T maxValue) {
        super(typeClass, primitiveName, minValue, maxValue);
    }

    @Override
    final BlockFragment primitiveRef() {
        final var primitiveName = primitiveName();
        return bb -> bb.str(".").str(primitiveName).str("Value()");
    }
}
