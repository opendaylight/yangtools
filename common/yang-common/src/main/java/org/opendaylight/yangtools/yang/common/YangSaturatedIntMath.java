/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

/**
 * Saturated mathematical operations where the operand is an integer. Where a corresponding {@link YangStrictIntMath}
 * operation would throw an exception due to range restrictions, operations defined here return the result type's
 * mininum value (on underflow) or maximum value (on overflow).
 *
 * @param <T> result type
 */
// FIXME: also permits Decimal64
public sealed interface YangSaturatedIntMath<T extends YangNumber<T>> permits YangInteger {
    // FIXME: expose these and more
    //    T plusS(byte val);
    //
    //    T plusS(short val);
    //
    //    T plusS(int val);
    //
    //    T plusS(long val);
    //
    //    T plusS(Uint8 val);
    //
    //    T plusS(Uint16 val);
    //
    //    T plusS(Uint32 val);
    //
    //    T plusS(Uint64 val);
    //
    //    T plusS(BigDecimal val);
}
