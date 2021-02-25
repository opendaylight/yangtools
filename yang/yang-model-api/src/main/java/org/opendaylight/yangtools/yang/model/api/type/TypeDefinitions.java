/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.type;

import static org.opendaylight.yangtools.yang.common.YangConstants.RFC6020_YANG_MODULE;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

@Beta
public final class TypeDefinitions {
    /**
     * Well-known QName of the {@code binary} built-in type.
     */
    public static final @NonNull QName BINARY = QName.create(RFC6020_YANG_MODULE, "binary").intern();

    /**
     * Well-known QName of the {@code bits} built-in type.
     */
    public static final @NonNull QName BITS = QName.create(RFC6020_YANG_MODULE, "bits").intern();

    /**
     * Well-known QName of the {@code boolean} built-in type.
     */
    public static final @NonNull QName BOOLEAN = QName.create(RFC6020_YANG_MODULE, "boolean").intern();

    /**
     * Well-known QName of the {@code decimal64} built-in type.
     */
    public static final @NonNull QName DECIMAL64 = QName.create(RFC6020_YANG_MODULE, "decimal64").intern();

    /**
     * Well-known QName of the {@code empty} built-in type.
     */
    public static final @NonNull QName EMPTY = QName.create(RFC6020_YANG_MODULE, "empty").intern();

    /**
     * Well-known QName of the {@code enumeration} built-in type.
     */
    public static final @NonNull QName ENUMERATION = QName.create(RFC6020_YANG_MODULE, "enumeration").intern();

    /**
     * Well-known QName of the {@code identityref} built-in type.
     */
    public static final @NonNull QName IDENTITYREF = QName.create(RFC6020_YANG_MODULE, "identityref").intern();

    /**
     * Well-known QName of the {@code int8} built-in type.
     */
    public static final @NonNull QName INT8 = QName.create(RFC6020_YANG_MODULE, "int8").intern();

    /**
     * Well-known QName of the {@code int16} built-in type.
     */
    public static final @NonNull QName INT16 = QName.create(RFC6020_YANG_MODULE, "int16").intern();

    /**
     * Well-known QName of the {@code int32} built-in type.
     */
    public static final @NonNull QName INT32 = QName.create(RFC6020_YANG_MODULE, "int32").intern();

    /**
     * Well-known QName of the {@code int64} built-in type.
     */
    public static final @NonNull QName INT64 = QName.create(RFC6020_YANG_MODULE, "int64").intern();

    /**
     * Well-known QName of the {@code string} built-in type.
     */
    public static final @NonNull QName STRING = QName.create(RFC6020_YANG_MODULE, "string").intern();

    /**
     * Well-known QName of the {@code union} built-in type.
     */
    public static final @NonNull QName UNION = QName.create(RFC6020_YANG_MODULE, "union").intern();

    /**
     * Well-known QName of the {@code leafref} built-in type.
     */
    public static final @NonNull QName LEAFREF = QName.create(RFC6020_YANG_MODULE, "leafref").intern();

    /**
     * Well-known QName of the {@code instance-identifier} built-in type.
     */
    public static final @NonNull QName INSTANCE_IDENTIFIER = QName.create(RFC6020_YANG_MODULE, "instance-identifier")
        .intern();

    /**
     * Well-known QName of the {@code uint8} built-in type.
     */
    public static final @NonNull QName UINT8 = QName.create(RFC6020_YANG_MODULE, "uint8").intern();

    /**
     * Well-known QName of the {@code uint16} built-in type.
     */
    public static final @NonNull QName UINT16 = QName.create(RFC6020_YANG_MODULE, "uint16").intern();

    /**
     * Well-known QName of the {@code uint32} built-in type.
     */
    public static final @NonNull QName UINT32 = QName.create(RFC6020_YANG_MODULE, "uint32").intern();

    /**
     * Well-known QName of the {@code uint64} built-in type.
     */
    public static final @NonNull QName UINT64 = QName.create(RFC6020_YANG_MODULE, "uint64").intern();

    private TypeDefinitions() {
        // Hidden on purpose
    }

    static int basicHashCode(final @NonNull TypeDefinition<?> type) {
        return Objects.hash(type.getQName(), type.getUnknownSchemaNodes(), type.getBaseType(),
            type.getUnits().orElse(null), type.getDefaultValue().orElse(null));
    }

    static int hashCode(final @NonNull RangeRestrictedTypeDefinition<?, ?> type) {
        return Objects.hash(type.getQName(), type.getUnknownSchemaNodes(), type.getBaseType(),
            type.getUnits().orElse(null), type.getDefaultValue().orElse(null), type.getRangeConstraint().orElse(null));
    }

    static <T extends RangeRestrictedTypeDefinition<T, ?>> boolean equals(final @NonNull Class<T> clazz,
            final @NonNull T type, final @Nullable Object obj) {
        if (type == obj) {
            return true;
        }

        final @Nullable T other = castIfEquals(clazz, type, obj);
        return other != null && type.getRangeConstraint().equals(other.getRangeConstraint());
    }

    static @NonNull String toString(final @NonNull RangeRestrictedTypeDefinition<?, ?> type) {
        return toStringHelper(type).toString();
    }

    static <T extends TypeDefinition<T>> @Nullable T castIfEquals(final @NonNull Class<T> clazz, final @NonNull T type,
            final @Nullable Object obj) {
        if (!clazz.isInstance(obj)) {
            return null;
        }

        final @NonNull T other = clazz.cast(obj);
        return Objects.equals(type.getQName(), other.getQName())
                && Objects.equals(type.getBaseType(), other.getBaseType())
                && Objects.equals(type.getDefaultValue(), other.getDefaultValue())
                && Objects.equals(type.getUnknownSchemaNodes(), other.getUnknownSchemaNodes())
                && Objects.equals(type.getUnits(), other.getUnits()) ? other : null;
    }

    static @NonNull ToStringHelper toStringHelper(final @NonNull TypeDefinition<?> type) {
        return MoreObjects.toStringHelper(type).omitNullValues()
                .add("name", type.getQName())
                .add("baseType", type.getBaseType())
                .add("default", type.getDefaultValue().orElse(null))
                .add("description", type.getDescription().orElse(null))
                .add("reference", type.getReference().orElse(null))
                .add("status", type.getStatus())
                .add("units", type.getUnits().orElse(null));
    }

    static @NonNull ToStringHelper toStringHelper(final @NonNull RangeRestrictedTypeDefinition<?, ?> type) {
        return toStringHelper((TypeDefinition<?>) type).add("range", type.getRangeConstraint().orElse(null));
    }
}
