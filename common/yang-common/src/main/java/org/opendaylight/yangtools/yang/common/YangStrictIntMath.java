/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import java.math.BigInteger;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Strict mathematical operations where the operand is an integer.
 *
 * @param <T> result type
 */
// FIXME: also permits Decimal64
@NonNullByDefault
public sealed interface YangStrictIntMath<T extends YangNumber<T>> permits YangInteger {
    T plus(byte val);

    T plus(short val);

    T plus(int val);

    T plus(long val);

    default T plus(final Uint8 val) {
        return plus(val.shortValue());
    }

    default T plus(final Uint16 val) {
        return plus(val.intValue());
    }

    default T plus(final Uint32 val) {
        return plus(val.longValue());
    }

    T plus(Uint64 val);

    T plus(BigInteger val);

    // FIXME: minus, div, mod
}
