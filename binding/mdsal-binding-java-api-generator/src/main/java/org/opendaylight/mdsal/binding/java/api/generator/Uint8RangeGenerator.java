/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import org.opendaylight.yangtools.yang.common.Uint8;

final class Uint8RangeGenerator extends AbstractUnsignedRangeGenerator<Uint8> {
    Uint8RangeGenerator() {
        super(Uint8.class, short.class.getName(), Uint8.ZERO, Uint8.MAX_VALUE);
    }

    @Override
    @Deprecated
    protected Uint8 convert(final Number value) {
        if (value instanceof Byte) {
            return Uint8.valueOf(value.byteValue());
        }
        return Uint8.valueOf(value.toString());
    }

    @Override
    protected String format(final Uint8 value) {
        return "(short)" + value.toCanonicalString();
    }
}
