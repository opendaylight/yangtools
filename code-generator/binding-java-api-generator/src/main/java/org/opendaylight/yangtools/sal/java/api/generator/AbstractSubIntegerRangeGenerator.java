/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.java.api.generator;

import com.google.common.base.Preconditions;

abstract class AbstractSubIntegerRangeGenerator<T extends Number & Comparable<T>> extends AbstractPrimitiveRangeGenerator<T> {
    private final String castType;

    protected AbstractSubIntegerRangeGenerator(final Class<T> typeClass, final T minValue, final T maxValue, final String castType) {
        super(typeClass, minValue, maxValue);
        this.castType = Preconditions.checkNotNull(castType);
    }

    @Override
    protected final String format(final T value) {
        // Make sure the number constant is cast to the corresponding primitive type
        return '(' + castType + ')' + value;
    }
}
