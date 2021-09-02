/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import org.opendaylight.yangtools.yang.common.Decimal64;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint8;

final class Decimal64RangeGenerator extends AbstractBigRangeGenerator<Decimal64> {
    Decimal64RangeGenerator() {
        super(Decimal64.class);
    }

    @Override
    protected String format(final Decimal64 value) {
        return "org.opendaylight.yangtools.yang.common.Decimal64.valueOf(\"" + value + "\")";
    }

    @Override
    @Deprecated
    protected Decimal64 convert(final Number value) {
        if (value instanceof Byte || value instanceof Short || value instanceof Integer
            || value instanceof Uint8 || value instanceof Uint16) {
            return Decimal64.valueOf(value.intValue());
        } else {
            return Decimal64.valueOf(value.longValue());
        }
    }
}
