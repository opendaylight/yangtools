/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri;

import static org.opendaylight.yangtools.binding.model.ri.Types.typeForBuiltIn;

import org.opendaylight.yangtools.binding.contract.BuiltInType;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;

public final class BaseYangTypes {
    /**
     * {@code Type} representation of {@code boolean} YANG type.
     */
    public static final ConcreteType BOOLEAN_TYPE = typeForBuiltIn(BuiltInType.BOOLEAN);

    /**
     * {@code Type} representation of {@code empty} YANG type.
     */
    public static final ConcreteType EMPTY_TYPE = typeForBuiltIn(BuiltInType.EMPTY);

    /**
     * {@code Type} representation of {@code enumeration} YANG type.
     */
    public static final ConcreteType ENUMERATION_TYPE = typeForBuiltIn(BuiltInType.ENUMERATION);

    /**
     * {@code Type} representation of {@code int8} YANG type.
     */
    public static final ConcreteType INT8_TYPE = typeForBuiltIn(BuiltInType.INT8);

    /**
     * {@code Type} representation of {@code int16} YANG type.
     */
    public static final ConcreteType INT16_TYPE = typeForBuiltIn(BuiltInType.INT16);

    /**
     * {@code Type} representation of {@code int32} YANG type.
     */
    public static final ConcreteType INT32_TYPE = typeForBuiltIn(BuiltInType.INT32);

    /**
     * {@code Type} representation of {@code int64} YANG type.
     */
    public static final ConcreteType INT64_TYPE = typeForBuiltIn(BuiltInType.INT64);

    /**
     * {@code Type} representation of {@code string} YANG type.
     */
    public static final ConcreteType STRING_TYPE = typeForBuiltIn(BuiltInType.STRING);

    /**
     * {@code Type} representation of {@code decimal64} YANG type.
     */
    public static final ConcreteType DECIMAL64_TYPE = typeForBuiltIn(BuiltInType.DECIMAL64);

    /**
     * {@code Type} representation of {@code uint8} YANG type.
     */
    public static final ConcreteType UINT8_TYPE = typeForBuiltIn(BuiltInType.UINT8);

    /**
     * {@code Type} representation of {@code uint16} YANG type.
     */
    public static final ConcreteType UINT16_TYPE = typeForBuiltIn(BuiltInType.UINT16);

    /**
     * {@code Type} representation of {@code uint32} YANG type.
     */
    public static final ConcreteType UINT32_TYPE = typeForBuiltIn(BuiltInType.UINT32);

    /**
     * {@code Type} representation of {@code uint64} YANG type.
     */
    public static final ConcreteType UINT64_TYPE = typeForBuiltIn(BuiltInType.UINT64);

    /**
     * {@code Type} representation of {@code binary} YANG type.
     */
    public static final ConcreteType BINARY_TYPE = typeForBuiltIn(BuiltInType.BINARY);

    /**
     * {@code Type} representation of {@code instance-identifier} YANG type.
     */
    public static final ConcreteType INSTANCE_IDENTIFIER = typeForBuiltIn(BuiltInType.INSTANCE_IDENTIFIER);

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
    public static ConcreteType javaTypeForYangType(final String type) {
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
}
