/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.data;

import org.opendaylight.yangtools.yang.common.Decimal64;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * An scalar value, corresponding to a {@code leaf} statement and covering most of YANG built-in types.
 *
 */
// FIXME: EnumerationValue
// FIXME: Int8
// FIXME: Int16
// FIXME: Int32
// FIXME: Int64
// FIXME: Union???
// FIXME: 16.0.0: this should be an abstract class once Decimal64/Empty/Uint8/Uint16/Uint32/Uint64 serial form is solved
public sealed interface ScalarValue extends Value
    permits Binary, Bits, Decimal64, Empty, InstanceIdentifier, QName, Uint8, Uint16, Uint32, Uint64, YangBoolean,
            YangString {

}
