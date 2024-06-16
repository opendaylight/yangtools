/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

/**
 * Saturated mathematical operations where the operand is an integer. These methods follow Java overflowing math,
 * rendering code vulnerable to integer overflow attacks. If you use these, you are on your own -- and you have been
 * warned.
 *
 * @param <T> result type
 */
// FIXME: also permits Decimal64
public sealed interface YangOverflowIntMath<T extends YangNumber<T>> permits YangInteger {
    // FIXME: expose these and more
    //    T plusO(byte val);
    //
    //    T plusO(short val);
    //
    //    T plusO(int val);
    //
    //    T plusO(long val);
    //
    //    T plusO(Uint8 val);
    //
    //    T plusO(Uint16 val);
    //
    //    T plusO(Uint32 val);
    //
    //    T plusO(Uint64 val);
    //
    //    T plusO(BigDecimal val);
}
