/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.ri;

import java.math.BigDecimal;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

public final class BaseYangTypes {
    /**
     * {@code Type} representation of {@code boolean} YANG type.
     */
    public static final Type BOOLEAN_TYPE = Types.BOOLEAN;

    /**
     * {@code Type} representation of {@code empty} YANG type.
     */
    public static final Type EMPTY_TYPE = Types.typeForClass(Empty.class);

    public static final Type ENUM_TYPE = Types.typeForClass(Enum.class);

    /**
     * {@code Type} representation of {@code int8} YANG type.
     */
    public static final Type INT8_TYPE = Types.typeForClass(Byte.class);

    /**
     * {@code Type} representation of {@code int16} YANG type.
     */
    public static final Type INT16_TYPE = Types.typeForClass(Short.class);

    /**
     * {@code Type} representation of {@code int32} YANG type.
     */
    public static final Type INT32_TYPE = Types.typeForClass(Integer.class);

    /**
     * {@code Type} representation of {@code int64} YANG type.
     */
    public static final Type INT64_TYPE = Types.typeForClass(Long.class);

    /**
     * {@code Type} representation of {@code string} YANG type.
     */
    public static final Type STRING_TYPE = Types.STRING;

    /**
     * {@code Type} representation of {@code decimal64} YANG type.
     */
    public static final Type DECIMAL64_TYPE = Types.typeForClass(BigDecimal.class);

    /**
     * {@code Type} representation of {@code uint8} YANG type.
     */
    public static final Type UINT8_TYPE = Types.typeForClass(Uint8.class);

    /**
     * {@code Type} representation of {@code uint16} YANG type.
     */
    public static final Type UINT16_TYPE = Types.typeForClass(Uint16.class);

    /**
     * {@code Type} representation of {@code uint32} YANG type.
     */
    public static final Type UINT32_TYPE = Types.typeForClass(Uint32.class);

    /**
     * {@code Type} representation of {@code uint64} YANG type.
     */
    public static final Type UINT64_TYPE = Types.typeForClass(Uint64.class);

    /**
     * {@code Type} representation of {@code binary} YANG type.
     */
    public static final Type BINARY_TYPE = Types.BYTE_ARRAY;

    // FIXME: why is this a ParameterizedType (vs. what BindingTypes defines?)
    public static final Type INSTANCE_IDENTIFIER = Types.parameterizedTypeFor(BindingTypes.INSTANCE_IDENTIFIER);

    private BaseYangTypes() {
        // Hidden on purpose
    }

    /**
     * Searches {@code Type} value to which is YANG {@code type} mapped.
     *
     * @param type string with YANG type name
     * @return Java {@code Type} representation of {@code type}, or null if the type is not mapped.
     * @throws NullPointerException if type is null
     */
    public static Type javaTypeForYangType(final String type) {
        switch (type) {
            case "binary":
                return BINARY_TYPE;
            case "boolean":
                return BOOLEAN_TYPE;
            case "decimal64":
                return DECIMAL64_TYPE;
            case "empty":
                return EMPTY_TYPE;
            case "enumeration":
                return ENUM_TYPE;
            case "instance-identifier":
                return INSTANCE_IDENTIFIER;
            case "int8":
                return INT8_TYPE;
            case "int16":
                return INT16_TYPE;
            case "int32":
                return INT32_TYPE;
            case "int64":
                return INT64_TYPE;
            case "string":
                return STRING_TYPE;
            case "uint8":
                return UINT8_TYPE;
            case "uint16":
                return UINT16_TYPE;
            case "uint32":
                return UINT32_TYPE;
            case "uint64":
                return UINT64_TYPE;
            default:
                return null;
        }
    }
}
