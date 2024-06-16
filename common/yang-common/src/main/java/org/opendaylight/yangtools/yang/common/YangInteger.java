/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Base interface for YANG integer types.
 *
 * @param <T> numeric type
 */
@NonNullByDefault
public sealed interface YangInteger<T extends YangInteger<T>>
        extends CanonicalValue<T>, YangNumber<T>, YangStrictIntMath<T>, YangOverflowIntMath<T>, YangSaturatedIntMath<T>
        // FIXME: permits YangInt when we can match the convenience of Byte, Short, Int, Long
        permits YangUint {
    // Just a marker interface for now
}
