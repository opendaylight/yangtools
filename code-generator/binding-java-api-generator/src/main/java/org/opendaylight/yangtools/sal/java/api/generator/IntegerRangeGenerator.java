/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.java.api.generator;

final class IntegerRangeGenerator extends AbstractPrimitiveRangeGenerator<Integer> {
    IntegerRangeGenerator() {
        super(Integer.class, int.class.getName(), Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    @Override
    protected String format(final Integer value) {
        return value.toString();
    }

    @Override
    protected Integer convert(final Number value) {
        return value.intValue();
    }
}
