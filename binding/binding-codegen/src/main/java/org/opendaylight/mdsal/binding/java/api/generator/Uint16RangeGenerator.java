/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint8;

final class Uint16RangeGenerator extends AbstractUnsignedRangeGenerator<Uint16> {
    Uint16RangeGenerator() {
        super(Uint16.class, int.class.getName(), Uint16.ZERO, Uint16.MAX_VALUE);
    }

    @Override
    @Deprecated
    protected Uint16 convert(final Number value) {
        if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Uint8) {
            return Uint16.valueOf(value.intValue());
        }
        return Uint16.valueOf(value.toString());
    }

    @Override
    protected String format(final Uint16 value) {
        return value.toCanonicalString();
    }
}
