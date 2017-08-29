/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;

abstract class AbstractIntegerBaseType<C extends Number & Comparable<C>>
        extends AbstractRangeRestrictedBaseType<IntegerTypeDefinition, C> implements IntegerTypeDefinition {
    AbstractIntegerBaseType(final QName qname, final C minValue, final C maxValue) {
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
