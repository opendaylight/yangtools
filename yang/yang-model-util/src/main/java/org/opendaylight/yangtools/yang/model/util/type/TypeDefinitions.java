/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Objects;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;

// TODO: this should be in the API package, as it defines equality for TypeDefinitions
final class TypeDefinitions {
    private TypeDefinitions() {
        throw new UnsupportedOperationException();
    }

    private static int basicHashCode(final TypeDefinition<?> type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(), type.getUnits(),
            type.getDefaultValue());
    }

    private static <T extends TypeDefinition<T>> T castIfEquals(final Class<T> clazz, final T type, final Object obj) {
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

    private static ToStringHelper toStringHelper(final TypeDefinition<?> type) {
        return MoreObjects.toStringHelper(type).omitNullValues()
                .add("baseType", type.getBaseType())
                .add("default", type.getDefaultValue())
                .add("description", type.getDescription())
                .add("path", type.getPath())
                .add("reference", type.getReference())
                .add("status", type.getStatus())
                .add("units", type.getUnits());
    }

    static int hashCode(final BinaryTypeDefinition type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(), type.getUnits(),
            type.getDefaultValue(), type.getLengthConstraints());
    }

    static int hashCode(final BitsTypeDefinition type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(), type.getUnits(),
            type.getDefaultValue(), type.getBits());
    }


    static int hashCode(final BooleanTypeDefinition type) {
        return basicHashCode(type);
    }

    static int hashCode(final DecimalTypeDefinition type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(), type.getUnits(),
            type.getDefaultValue(), type.getFractionDigits(), type.getRangeConstraints());
    }

    static int hashCode(final EmptyTypeDefinition type) {
        return basicHashCode(type);
    }

    static int hashCode(final EnumTypeDefinition type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(), type.getUnits(),
            type.getDefaultValue(), type.getValues());
    }

    static int hashCode(final IdentityrefTypeDefinition type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(), type.getUnits(),
            type.getDefaultValue(), type.getIdentity());
    }

    static int hashCode(final InstanceIdentifierTypeDefinition type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(), type.getUnits(),
            type.getDefaultValue(), type.requireInstance());
    }

    static int hashCode(final IntegerTypeDefinition type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(), type.getUnits(),
            type.getDefaultValue(), type.getRangeConstraints());
    }

    static int hashCode(final LeafrefTypeDefinition type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(), type.getUnits(),
            type.getDefaultValue(), type.getPathStatement());
    }

    static int hashCode(final StringTypeDefinition type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(), type.getUnits(),
            type.getDefaultValue(), type.getLengthConstraints(), type.getPatternConstraints());
    }

    static int hashCode(final UnionTypeDefinition type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(), type.getUnits(),
            type.getDefaultValue(), type.getTypes());
    }

    static int hashCode(final UnsignedIntegerTypeDefinition type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(), type.getUnits(),
            type.getDefaultValue(), type.getRangeConstraints());
    }

    static boolean equals(final BinaryTypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }

        final BinaryTypeDefinition other = castIfEquals(BinaryTypeDefinition.class, type, obj);
        return other != null && type.getLengthConstraints().equals(other.getLengthConstraints());
    }

    static boolean equals(final BitsTypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }

        final BitsTypeDefinition other = castIfEquals(BitsTypeDefinition.class, type, obj);
        return other != null && type.getBits().equals(other.getBits());
    }

    static boolean equals(final BooleanTypeDefinition type, final Object obj) {
        return type == obj || castIfEquals(BooleanTypeDefinition.class, type, obj) != null;
    }

    static boolean equals(final DecimalTypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }

        final DecimalTypeDefinition other = castIfEquals(DecimalTypeDefinition.class, type, obj);
        return other != null && type.getFractionDigits().equals(other.getFractionDigits())
                && type.getRangeConstraints().equals(other.getRangeConstraints());
    }

    static boolean equals(final EmptyTypeDefinition type, final Object obj) {
        return type == obj || castIfEquals(EmptyTypeDefinition.class, type, obj) != null;
    }

    static boolean equals(final EnumTypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }

        final EnumTypeDefinition other = castIfEquals(EnumTypeDefinition.class, type, obj);
        return other != null && type.getValues().equals(other.getValues());
    }

    static boolean equals(final IdentityrefTypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }

        final IdentityrefTypeDefinition other = castIfEquals(IdentityrefTypeDefinition.class, type, obj);
        return other != null && type.getIdentity().equals(other.getIdentity());
    }

    static boolean equals(final InstanceIdentifierTypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }

        final InstanceIdentifierTypeDefinition other = castIfEquals(InstanceIdentifierTypeDefinition.class, type, obj);
        return other != null && type.requireInstance() == other.requireInstance();
    }

    static boolean equals(final IntegerTypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }

        final IntegerTypeDefinition other = castIfEquals(IntegerTypeDefinition.class, type, obj);
        return other != null && type.getRangeConstraints().equals(other.getRangeConstraints());
    }

    static boolean equals(final LeafrefTypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }

        final LeafrefTypeDefinition other = castIfEquals(LeafrefTypeDefinition.class, type, obj);
        return other != null && type.getPathStatement().equals(other.getPathStatement());
    }

    static boolean equals(final StringTypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }

        final StringTypeDefinition other = castIfEquals(StringTypeDefinition.class, type, obj);
        return other != null && type.getLengthConstraints().equals(other.getLengthConstraints())
                && type.getPatternConstraints().equals(other.getPatternConstraints());
    }

    static boolean equals(final UnionTypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }

        final UnionTypeDefinition other = castIfEquals(UnionTypeDefinition.class, type, obj);
        return other != null && type.getTypes().equals(other.getTypes());
    }

    static boolean equals(final UnsignedIntegerTypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }

        final UnsignedIntegerTypeDefinition other = castIfEquals(UnsignedIntegerTypeDefinition.class, type, obj);
        return other != null && type.getRangeConstraints().equals(other.getRangeConstraints());
    }

    static String toString(final BinaryTypeDefinition type) {
        return toStringHelper(type).add("length", type.getLengthConstraints()).toString();
    }

    static String toString(final BitsTypeDefinition type) {
        return toStringHelper(type).add("bits", type.getBits()).toString();
    }

    static String toString(final BooleanTypeDefinition type) {
        return toStringHelper(type).toString();
    }

    static String toString(final DecimalTypeDefinition type) {
        return toStringHelper(type).add("fractionDigits", type.getFractionDigits())
                .add("range", type.getRangeConstraints()).toString();
    }

    static String toString(final EmptyTypeDefinition type) {
        return toStringHelper(type).toString();
    }

    static String toString(final EnumTypeDefinition type) {
        return toStringHelper(type).add("values", type.getValues()).toString();
    }

    static String toString(final IdentityrefTypeDefinition type) {
        return toStringHelper(type).add("identity", type.getIdentity()).toString();
    }

    static String toString(final InstanceIdentifierTypeDefinition type) {
        return toStringHelper(type).add("requireInstance", type.requireInstance()).toString();
    }

    static String toString(final IntegerTypeDefinition type) {
        return toStringHelper(type).add("range", type.getRangeConstraints()).toString();
    }

    static String toString(final LeafrefTypeDefinition type) {
        return toStringHelper(type).add("pathStatement", type.getPathStatement()).toString();
    }

    static String toString(final StringTypeDefinition type) {
        return toStringHelper(type).add("length", type.getLengthConstraints())
                .add("patterns", type.getPatternConstraints()).toString();
    }

    static String toString(final UnionTypeDefinition type) {
        return toStringHelper(type).add("types", type.getTypes()).toString();
    }

    static String toString(final UnsignedIntegerTypeDefinition type) {
        return toStringHelper(type).add("range", type.getRangeConstraints()).toString();
    }
}
