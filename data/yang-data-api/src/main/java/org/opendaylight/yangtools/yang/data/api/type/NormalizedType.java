/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.type;

import org.eclipse.jdt.annotation.Nullable;

/**
 *
 */
public sealed interface NormalizedType
        permits BooleanType, Decimal64Type, EmptyType, EnumType, IdentityrefType, InstanceIdentifierType, Int8Type,
                Int16Type, Int32Type, Int64Type, LengthConstrainedType, Uint8Type, Uint16Type, Uint32Type, Uint64Type,
                UnionType {

    @Nullable Object defaultValue();
}
