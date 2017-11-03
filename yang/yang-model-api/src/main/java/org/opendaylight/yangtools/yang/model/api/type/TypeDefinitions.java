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
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

final class TypeDefinitions {
    private TypeDefinitions() {
        throw new UnsupportedOperationException();
    }

    static int basicHashCode(final TypeDefinition<?> type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(),
            type.getUnits().orElse(null), type.getDefaultValue().orElse(null));
    }

    static int hashCode(final RangeRestrictedTypeDefinition<?, ?> type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(),
            type.getUnits().orElse(null), type.getDefaultValue().orElse(null), type.getRangeConstraint().orElse(null));
    }

    static <T extends RangeRestrictedTypeDefinition<T, ?>> boolean equals(final Class<T> clazz,
            final T type, final Object obj) {
        if (type == obj) {
            return true;
        }

        final T other = castIfEquals(clazz, type, obj);
        return other != null && type.getRangeConstraint().equals(other.getRangeConstraint());
    }

    static String toString(final RangeRestrictedTypeDefinition<?, ?> type) {
        return toStringHelper(type).toString();
    }

    @Nullable
    static <T extends TypeDefinition<T>> T castIfEquals(final Class<T> clazz, final T type, final Object obj) {
        if (!clazz.isInstance(obj)) {
            return null;
        }

        final T other = clazz.cast(obj);
        return Objects.equals(type.getPath(), other.getPath())
                && Objects.equals(type.getBaseType(), other.getBaseType())
                && Objects.equals(type.getDefaultValue(), other.getDefaultValue())
                && Objects.equals(type.getUnknownSchemaNodes(), other.getUnknownSchemaNodes())
                && Objects.equals(type.getUnits(), other.getUnits()) ? other : null;
    }

    static ToStringHelper toStringHelper(final TypeDefinition<?> type) {
        return MoreObjects.toStringHelper(type).omitNullValues()
                .add("path", type.getPath())
                .add("baseType", type.getBaseType())
                .add("default", type.getDefaultValue().orElse(null))
                .add("description", type.getDescription().orElse(null))
                .add("reference", type.getReference().orElse(null))
                .add("status", type.getStatus())
                .add("units", type.getUnits().orElse(null));
    }

    static ToStringHelper toStringHelper(final RangeRestrictedTypeDefinition<?, ?> type) {
        return toStringHelper((TypeDefinition<?>) type).add("range", type.getRangeConstraint().orElse(null));
    }
}
