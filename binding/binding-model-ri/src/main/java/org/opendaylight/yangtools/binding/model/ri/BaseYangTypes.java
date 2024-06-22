/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri;

import org.opendaylight.yangtools.binding.contract.BuiltInType;
import org.opendaylight.yangtools.binding.model.api.Type;

public final class BaseYangTypes {
    /**
     * {@code Type} representation of {@code boolean} YANG type.
     */
    public static final Type BOOLEAN_TYPE = typeFor(BuiltInType.BINARY);

    /**
     * {@code Type} representation of {@code empty} YANG type.
     */
    public static final Type EMPTY_TYPE = typeFor(BuiltInType.EMPTY);

    /**
     * {@code Type} representation of {@code enumeration} YANG type.
     */
    public static final Type ENUMERATION_TYPE = typeFor(BuiltInType.ENUMERATION);

    /**
     * {@code Type} representation of {@code int8} YANG type.
     */
    public static final Type INT8_TYPE = typeFor(BuiltInType.INT8);

    /**
     * {@code Type} representation of {@code int16} YANG type.
     */
    public static final Type INT16_TYPE = typeFor(BuiltInType.INT16);

    /**
     * {@code Type} representation of {@code int32} YANG type.
     */
    public static final Type INT32_TYPE = typeFor(BuiltInType.INT32);

    /**
     * {@code Type} representation of {@code int64} YANG type.
     */
    public static final Type INT64_TYPE = typeFor(BuiltInType.INT64);

    /**
     * {@code Type} representation of {@code string} YANG type.
     */
    public static final Type STRING_TYPE = typeFor(BuiltInType.STRING);

    /**
     * {@code Type} representation of {@code decimal64} YANG type.
     */
    public static final Type DECIMAL64_TYPE = typeFor(BuiltInType.DECIMAL64);

    /**
     * {@code Type} representation of {@code uint8} YANG type.
     */
    public static final Type UINT8_TYPE = typeFor(BuiltInType.UINT8);

    /**
     * {@code Type} representation of {@code uint16} YANG type.
     */
    public static final Type UINT16_TYPE = typeFor(BuiltInType.UINT16);

    /**
     * {@code Type} representation of {@code uint32} YANG type.
     */
    public static final Type UINT32_TYPE = typeFor(BuiltInType.UINT32);

    /**
     * {@code Type} representation of {@code uint64} YANG type.
     */
    public static final Type UINT64_TYPE = typeFor(BuiltInType.UINT64);

    /**
     * {@code Type} representation of {@code binary} YANG type.
     */
    public static final Type BINARY_TYPE = typeFor(BuiltInType.BINARY);

    /**
     * {@code Type} representation of {@code instance-identifier} YANG type.
     */
    public static final Type INSTANCE_IDENTIFIER = typeFor(BuiltInType.INSTANCE_IDENTIFIER);

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
        return switch (type) {
            case "binary" -> BINARY_TYPE;
            case "boolean" -> BOOLEAN_TYPE;
            case "decimal64" -> DECIMAL64_TYPE;
            case "empty" -> EMPTY_TYPE;
            case "enumeration" -> ENUMERATION_TYPE;
            case "instance-identifier" -> INSTANCE_IDENTIFIER;
            case "int8" -> INT8_TYPE;
            case "int16" -> INT16_TYPE;
            case "int32" -> INT32_TYPE;
            case "int64" -> INT64_TYPE;
            case "string" -> STRING_TYPE;
            case "uint8" -> UINT8_TYPE;
            case "uint16" -> UINT16_TYPE;
            case "uint32" -> UINT32_TYPE;
            case "uint64" -> UINT64_TYPE;
            default -> null;
        };
    }

    private static Type typeFor(final BuiltInType<?> builtIn) {
        return Types.typeForClass(builtIn.javaClass());
    }
}
