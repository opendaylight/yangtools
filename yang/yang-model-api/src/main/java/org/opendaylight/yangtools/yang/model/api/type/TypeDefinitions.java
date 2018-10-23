/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.type;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

final class TypeDefinitions {
    private TypeDefinitions() {
        throw new UnsupportedOperationException();
    }

    static int basicHashCode(final @NonNull TypeDefinition<?> type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(),
            type.getUnits().orElse(null), type.getDefaultValue().orElse(null));
    }

    static int hashCode(final @NonNull RangeRestrictedTypeDefinition<?, ?> type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(),
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
        return Objects.equals(type.getPath(), other.getPath())
                && Objects.equals(type.getBaseType(), other.getBaseType())
                && Objects.equals(type.getDefaultValue(), other.getDefaultValue())
                && Objects.equals(type.getUnknownSchemaNodes(), other.getUnknownSchemaNodes())
                && Objects.equals(type.getUnits(), other.getUnits()) ? other : null;
    }

    static @NonNull ToStringHelper toStringHelper(final @NonNull TypeDefinition<?> type) {
        return MoreObjects.toStringHelper(type).omitNullValues()
                .add("path", type.getPath())
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
