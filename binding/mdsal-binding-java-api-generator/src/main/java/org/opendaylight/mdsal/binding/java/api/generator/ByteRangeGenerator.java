/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

final class ByteRangeGenerator extends AbstractSubIntegerRangeGenerator<Byte> {
    ByteRangeGenerator() {
        super(Byte.class, byte.class.getName(), Byte.MIN_VALUE, Byte.MAX_VALUE);
    }

    @Override
    @Deprecated
    protected Byte convert(final Number value) {
        return value.byteValue();
    }
}
