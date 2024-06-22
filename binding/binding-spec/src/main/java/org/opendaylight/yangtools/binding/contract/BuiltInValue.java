/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.contract;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.BaseIdentity;
import org.opendaylight.yangtools.binding.BindingInstanceIdentifier;
import org.opendaylight.yangtools.binding.BitsTypeObject;
import org.opendaylight.yangtools.binding.EnumTypeObject;
import org.opendaylight.yangtools.binding.UnionTypeObject;
import org.opendaylight.yangtools.yang.common.Decimal64;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * A value with its corresponding {@link BuiltInType}.
 *
 * @param <T> Java type
 */
@Beta
@NonNullByDefault
public final class BuiltInValue<T> {
    private final BuiltInType<T> type;
    private final T value;

    private BuiltInValue(final BuiltInType<T> type, final T value) {
        this.type = requireNonNull(type);
        this.value = requireNonNull(value);
    }

    /**
     * Returns the {@link BuiltInType}.
     *
     * @return the {@link BuiltInType}
     */

    public BuiltInType<T> type() {
        return type;
    }

    /**
     * Returns the value.
     *
     * @return the value
     */
    public T value() {
        return value;
    }

    public static BuiltInValue<?> of(final Object obj) {
        return switch (obj) {
            case Byte value -> new BuiltInValue<>(BuiltInType.INT8, value);
            case Short value -> new BuiltInValue<>(BuiltInType.INT16, value);
            case Integer value -> new BuiltInValue<>(BuiltInType.INT32, value);
            case Long value -> new BuiltInValue<>(BuiltInType.INT64, value);
            case Uint8 value -> new BuiltInValue<>(BuiltInType.UINT8, value);
            case Uint16 value -> new BuiltInValue<>(BuiltInType.UINT16, value);
            case Uint32 value -> new BuiltInValue<>(BuiltInType.UINT32, value);
            case Uint64 value -> new BuiltInValue<>(BuiltInType.UINT64, value);
            case Decimal64 value -> new BuiltInValue<>(BuiltInType.DECIMAL64, value);
            case String value -> new BuiltInValue<>(BuiltInType.STRING, value);
            case Boolean value -> new BuiltInValue<>(BuiltInType.BOOLEAN, value);
            case EnumTypeObject value -> new BuiltInValue<>(BuiltInType.ENUMERATION, value);
            case BitsTypeObject value -> new BuiltInValue<>(BuiltInType.BITS, value);
            case byte[] value -> new BuiltInValue<>(BuiltInType.BINARY, value);
            case BaseIdentity value -> new BuiltInValue<>(BuiltInType.IDENTITYREF, value);
            case Empty value -> new BuiltInValue<>(BuiltInType.EMPTY, value);
            case UnionTypeObject value -> new BuiltInValue<>(BuiltInType.UNION, value);
            case BindingInstanceIdentifier value -> new BuiltInValue<>(BuiltInType.INSTANCE_IDENTIFIER, value);
            default -> throw new IllegalArgumentException("Invalid value " + obj);
        };
    }
}
