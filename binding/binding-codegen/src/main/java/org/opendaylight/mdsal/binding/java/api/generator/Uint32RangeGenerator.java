/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;

final class Uint32RangeGenerator extends AbstractUnsignedRangeGenerator<Uint32> {
    Uint32RangeGenerator() {
        super(Uint32.class, long.class.getName(), Uint32.ZERO, Uint32.MAX_VALUE);
    }

    @Override
    @Deprecated
    protected Uint32 convert(final Number value) {
        if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long
                || value instanceof Uint8 || value instanceof Uint16) {
            return Uint32.valueOf(value.longValue());
        }
        return Uint32.valueOf(value.toString());
    }

    @Override
    protected String format(final Uint32 value) {
        return value.toCanonicalString() + 'L';
    }
}
