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
 * Common interface for YANG unsigned integer types.
 *
 * @param <T> unsigned integer type
 */
// FIXME: permits Uint16, Uint32, Uint64
@NonNullByDefault
public sealed interface YangUint<T extends YangUint<T>> extends YangInteger<T> permits Uint8 {

}
