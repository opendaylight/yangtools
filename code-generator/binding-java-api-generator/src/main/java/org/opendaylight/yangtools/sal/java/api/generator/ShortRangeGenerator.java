/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.java.api.generator;

final class ShortRangeGenerator extends AbstractSubIntegerRangeGenerator<Short> {
    ShortRangeGenerator() {
        super(Short.class, Short.MIN_VALUE, Short.MAX_VALUE, short.class.getName());
    }

    @Override
    protected Short convert(final Number value) {
        return value.shortValue();
    }
}
