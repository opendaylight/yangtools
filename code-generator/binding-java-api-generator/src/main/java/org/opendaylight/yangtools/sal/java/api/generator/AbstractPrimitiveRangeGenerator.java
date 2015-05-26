/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.java.api.generator;

import com.google.common.base.Preconditions;

abstract class AbstractPrimitiveRangeGenerator<T extends Number & Comparable<T>> extends AbstractRangeGenerator<T> {
    private final T maxValue;

    protected AbstractPrimitiveRangeGenerator(final Class<T> clazz, final T maxValue) {
        super(clazz);
        this.maxValue = Preconditions.checkNotNull(maxValue);
    }

    @Override
    protected final boolean needsMaximumEnforcement(final T maxToEnforce) {
        return maxValue.compareTo(maxToEnforce) < 0;
    }
}
