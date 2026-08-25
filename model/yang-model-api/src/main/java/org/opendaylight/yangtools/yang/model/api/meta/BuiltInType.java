/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;

/**
 * Well-known YANG built-in types. Exposes the type name as bound to {@link YangConstants#RFC6020_YANG_MODULE}.
 *
 * @since 15.0.0
 */
@NonNullByDefault
public enum BuiltInType {
    /**
     * Well-known {@code binary} built-in type.
     */
    BINARY("binary"),
    /**
     * Well-known {@code bits} built-in type.
     */
    BITS("bits"),
    /**
     * Well-known {@code boolean} built-in type.
     */
    BOOLEAN("boolean"),
    /**
     * Well-known {@code decimal64} built-in type.
     */
    DECIMAL64("decimal64"),
    /**
     * Well-known {@code empty} built-in type.
     */
    EMPTY("empty"),
    /**
     * Well-known {@code enumeration} built-in type.
     */
    ENUMERATION("enumeration"),
    /**
     * Well-known {@code identityref} built-in type.
     */
    IDENTITYREF("identityref"),
    /**
     * Well-known {@code int8} built-in type.
     */
    INT8("int8"),
    /**
     * Well-known {@code int16} built-in type.
     */
    INT16("int16"),
    /**
     * Well-known {@code int32} built-in type.
     */
    INT32("int32"),
    /**
     * Well-known {@code int64} built-in type.
     */
    INT64("int64"),
    /**
     * Well-known {@code string} built-in type.
     */
    STRING("string"),
    /**
     * Well-known {@code union} built-in type.
     */
    UNION("union"),
    /**
     * Well-known {@code leafref} built-in type.
     */
    LEAFREF("leafref"),
    /**
     * Well-known {@code instance-identifier} built-in type.
     */
    INSTANCE_IDENTIFIER("instance-identifier"),
    /**
     * Well-known {@code uint8} built-in type.
     */
    UINT8("uint8"),
    /**
     * Well-known {@code uint16} built-in type.
     */
    UINT16("uint16"),
    /**
     * Well-known {@code uint32} built-in type.
     */
    UINT32("uint32"),
    /**
     * Well-known {@code uint64} built-in type.
     */
    UINT64("uint64");

    private final ArgumentDefinition<QName> asTypeArgument;

    BuiltInType(final String typeName) {
        asTypeArgument = ArgumentDefinition.of(QName.class, YangConstants.RFC6020_YANG_MODULE, typeName);
    }

    /**
     * {@return the type name bound to {@link YangConstants#RFC6020_YANG_MODULE}}
     */
    public QName typeName() {
        return asTypeArgument.argumentName();
    }

    /**
     * {@return a plain type name}
     */

    public String simpleName() {
        return asTypeArgument.simpleName();
    }

    /**
     * {@return the {@link ArgumentDefinition} corresponding to a reference to this type}
     */
    public ArgumentDefinition<QName> asTypeArgument() {
        return asTypeArgument;
    }

    /**
     * {@return the {@link BuiltInType} for specified type name, or {@code null}}
     *
     * @param simpleName the type name
     */
    public static @Nullable BuiltInType forSimpleName(final String simpleName) {
        return switch (simpleName) {
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

    @Override
    public String toString() {
        return "BuiltInType{name=" + simpleName() + "}";
    }
}
