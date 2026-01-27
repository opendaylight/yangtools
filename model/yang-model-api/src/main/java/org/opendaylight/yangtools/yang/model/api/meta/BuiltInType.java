/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;

/**
 * Well-known YANG built-in types. Exposes the type name as bound to {@link YangConstants#RFC6020_YANG_MODULE}.
 *
 * @param <V> value representation
 * @since 15.0.0
 */
@Beta
@NonNullByDefault
public sealed interface BuiltInType permits DefaultBuiltInType {
    /**
     * Well-known {@code binary} built-in type.
     */
    BuiltInType BINARY = new DefaultBuiltInType("binary");
    /**
     * Well-known {@code bits} built-in type.
     */
    BuiltInType BITS = new DefaultBuiltInType("bits");
    /**
     * Well-known {@code boolean} built-in type.
     */
    BuiltInType BOOLEAN = new DefaultBuiltInType("boolean");
    /**
     * Well-known {@code decimal64} built-in type.
     */
    BuiltInType DECIMAL64 = new DefaultBuiltInType("decimal64");
    /**
     * Well-known {@code empty} built-in type.
     */
    BuiltInType EMPTY = new DefaultBuiltInType("empty");
    /**
     * Well-known {@code enumeration} built-in type.
     */
    BuiltInType ENUMERATION = new DefaultBuiltInType("enumeration");
    /**
     * Well-known {@code identityref} built-in type.
     */
    BuiltInType IDENTITYREF = new DefaultBuiltInType("identityref");
    /**
     * Well-known {@code int8} built-in type.
     */
    BuiltInType INT8 = new DefaultBuiltInType("int8");
    /**
     * Well-known {@code int16} built-in type.
     */
    BuiltInType INT16 = new DefaultBuiltInType("int16");
    /**
     * Well-known {@code int32} built-in type.
     */
    BuiltInType INT32 = new DefaultBuiltInType("int32");
    /**
     * Well-known {@code int64} built-in type.
     */
    BuiltInType INT64 = new DefaultBuiltInType("int64");
    /**
     * Well-known {@code string} built-in type.
     */
    BuiltInType STRING = new DefaultBuiltInType("string");
    /**
     * Well-known {@code union} built-in type.
     */
    BuiltInType UNION = new DefaultBuiltInType("union");
    /**
     * Well-known {@code leafref} built-in type.
     */
    BuiltInType LEAFREF = new DefaultBuiltInType("leafref");
    /**
     * Well-known {@code instance-identifier} built-in type.
     */
    BuiltInType INSTANCE_IDENTIFIER = new DefaultBuiltInType("instance-identifier");
    /**
     * Well-known {@code uint8} built-in type.
     */
    BuiltInType UINT8 = new DefaultBuiltInType("uint8");
    /**
     * Well-known {@code uint16} built-in type.
     */
    BuiltInType UINT16 = new DefaultBuiltInType("uint16");
    /**
     * Well-known {@code uint32} built-in type.
     */
    BuiltInType UINT32 = new DefaultBuiltInType("uint32");
    /**
     * Well-known {@code uint64} built-in type.
     */
    BuiltInType UINT64 = new DefaultBuiltInType("uint64");

    /**
     * {@return the type name bound to {@link YangConstants#RFC6020_YANG_MODULE}}
     */
    default QName typeName() {
        return asTypeArgument().argumentName();
    }

    /**
     * {@return a plain type name}
     */
    default String simpleName() {
        return asTypeArgument().simpleName();
    }

    /**
     * {@return this built-in type as an argument to {@link TypeStatement}}
     */
    ArgumentDefinition asTypeArgument();

    /**
     * {@return the {@link BuiltInType} for specified type name, or {@code null}}
     * @param typeName the type name
     */
    static @Nullable BuiltInType forTypeName(final String typeName) {
        return switch (typeName) {
            case "binary" -> BINARY;
            case "bits" -> BITS;
            case "boolean" -> BOOLEAN;
            case "decimal64" -> DECIMAL64;
            case "empty" -> EMPTY;
            case "enumeration" -> ENUMERATION;
            case "identityref" -> IDENTITYREF;
            case "int8" -> INT8;
            case "int16" -> INT16;
            case "int32" -> INT32;
            case "int64" -> INT64;
            case "string" -> STRING;
            case "union" -> UNION;
            case "leafref" -> LEAFREF;
            case "instance-identifier" -> INSTANCE_IDENTIFIER;
            case "uint8" -> UINT8;
            case "uint16" -> UINT16;
            case "uint32" -> UINT32;
            case "uint64" -> UINT64;
            default -> null;
        };
    }
}
