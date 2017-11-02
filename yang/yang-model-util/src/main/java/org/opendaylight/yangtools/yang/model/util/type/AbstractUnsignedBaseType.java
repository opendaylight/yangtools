/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;

abstract class AbstractUnsignedBaseType<N extends Number & Comparable<N>, T extends UnsignedIntegerTypeDefinition<N, T>>
        extends AbstractRangeRestrictedBaseType<T, N> implements UnsignedIntegerTypeDefinition<N, T> {
    AbstractUnsignedBaseType(final QName qname, final N minValue, final N maxValue) {
        super(qname, minValue, maxValue);
    }

    @Override
    public final int hashCode() {
        return TypeDefinitions.hashCode(this);
    }

    @Override
    public final boolean equals(final Object obj) {
        return TypeDefinitions.equals(this, obj);
    }

    @Override
    public final String toString() {
        return TypeDefinitions.toString(this);
    }
}
