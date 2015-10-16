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

    private static <T extends TypeDefinition<T>> boolean equals(final Class<T> klass, final T type, final Object obj) {
        if (!klass.isInstance(obj)) {
            return false;
        }

        final TypeDefinition<?> other = (TypeDefinition<?>) obj;
        return Objects.equals(type.getPath(), other.getPath())
                && Objects.equals(type.getBaseType(), other.getBaseType())
                && Objects.equals(type.getDefaultValue(), other.getDefaultValue())
                && Objects.equals(type.getUnknownSchemaNodes(), other.getUnknownSchemaNodes())
                && Objects.equals(type.getUnits(), other.getUnits());
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

    static boolean equals(final BinaryTypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }

        return equals(BinaryTypeDefinition.class, type, obj)
                && type.getLengthConstraints().equals(((BinaryTypeDefinition) obj).getLengthConstraints());
    }

    static String toString(final BinaryTypeDefinition type) {
        return toStringHelper(type).add("length", type.getLengthConstraints()).toString();
    }

    static int hashCode(final BitsTypeDefinition type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(), type.getUnits(),
            type.getDefaultValue(), type.getBits());
    }

    static boolean equals(final BitsTypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }

        return equals(BitsTypeDefinition.class, type, obj)
                && type.getBits().equals(((BitsTypeDefinition) obj).getBits());
    }

    static String toString(final BitsTypeDefinition type) {
        return toStringHelper(type).add("bits", type.getBits()).toString();
    }

    static int hashCode(final BooleanTypeDefinition type) {
        return basicHashCode(type);
    }

    static boolean equals(final BooleanTypeDefinition type, final Object obj) {
        return type == obj || equals(BooleanTypeDefinition.class, type, obj);
    }

    static String toString(final BooleanTypeDefinition type) {
        return toStringHelper(type).toString();
    }

    static int hashCode(final DecimalTypeDefinition type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(), type.getUnits(),
            type.getDefaultValue(), type.getFractionDigits(), type.getRangeConstraints());
    }

    static boolean equals(final DecimalTypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }
        if (!equals(DecimalTypeDefinition.class, type, obj)) {
            return false;
        }

        final DecimalTypeDefinition other = (DecimalTypeDefinition) obj;
        return type.getFractionDigits().equals(other.getFractionDigits())
                && type.getRangeConstraints().equals(other.getRangeConstraints());
    }

    static String toString(final DecimalTypeDefinition type) {
        return toStringHelper(type).add("fractionDigits", type.getFractionDigits())
                .add("range", type.getRangeConstraints()).toString();
    }

    static int hashCode(final EmptyTypeDefinition type) {
        return basicHashCode(type);
    }

    static boolean equals(final EmptyTypeDefinition type, final Object obj) {
        return type == obj || equals(EmptyTypeDefinition.class, type, obj);
    }

    static String toString(final EmptyTypeDefinition type) {
        return toStringHelper(type).toString();
    }

    static int hashCode(final EnumTypeDefinition type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(), type.getUnits(),
            type.getDefaultValue(), type.getValues());
    }

    static boolean equals(final EnumTypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }

        return equals(EnumTypeDefinition.class, type, obj)
                && type.getValues().equals(((EnumTypeDefinition) obj).getValues());
    }

    static String toString(final EnumTypeDefinition type) {
        return toStringHelper(type).add("values", type.getValues()).toString();
    }

    static int hashCode(final IdentityrefTypeDefinition type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(), type.getUnits(),
            type.getDefaultValue(), type.getIdentity());
    }

    static boolean equals(final IdentityrefTypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }

        return equals(IdentityrefTypeDefinition.class, type, obj)
                && type.getIdentity().equals(((IdentityrefTypeDefinition) obj).getIdentity());
    }

    static String toString(final IdentityrefTypeDefinition type) {
        return toStringHelper(type).add("identity", type.getIdentity()).toString();
    }

    static int hashCode(final InstanceIdentifierTypeDefinition type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(), type.getUnits(),
            type.getDefaultValue(), type.requireInstance());
    }

    static boolean equals(final InstanceIdentifierTypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }

        return equals(InstanceIdentifierTypeDefinition.class, type, obj)
                && type.requireInstance() == ((InstanceIdentifierTypeDefinition) obj).requireInstance();
    }

    static String toString(final InstanceIdentifierTypeDefinition type) {
        return toStringHelper(type).add("requireInstance", type.requireInstance()).toString();
    }

    static int hashCode(final IntegerTypeDefinition type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(), type.getUnits(),
            type.getDefaultValue(), type.getRangeConstraints());
    }

    static boolean equals(final IntegerTypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }

        return equals(IntegerTypeDefinition.class, type, obj)
                && type.getRangeConstraints().equals(((IntegerTypeDefinition) obj).getRangeConstraints());
    }

    static String toString(final IntegerTypeDefinition type) {
        return toStringHelper(type).add("range", type.getRangeConstraints()).toString();
    }

    static int hashCode(final LeafrefTypeDefinition type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(), type.getUnits(),
            type.getDefaultValue(), type.getPathStatement());
    }

    static boolean equals(final LeafrefTypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }

        return equals(LeafrefTypeDefinition.class, type, obj)
                && type.getPathStatement().equals(((LeafrefTypeDefinition) obj).getPathStatement());
    }

    static String toString(final LeafrefTypeDefinition type) {
        return toStringHelper(type).add("pathStatement", type.getPathStatement()).toString();
    }

    static int hashCode(final StringTypeDefinition type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(), type.getUnits(),
            type.getDefaultValue(), type.getLengthConstraints(), type.getPatternConstraints());
    }

    static boolean equals(final StringTypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }
        if (!equals(StringTypeDefinition.class, type, obj)) {
            return false;
        }

        final StringTypeDefinition other = (StringTypeDefinition) obj;
        return type.getLengthConstraints().equals(other.getLengthConstraints())
                && type.getPatternConstraints().equals(other.getPatternConstraints());
    }

    static String toString(final StringTypeDefinition type) {
        return toStringHelper(type).add("length", type.getLengthConstraints())
                .add("patterns", type.getPatternConstraints()).toString();
    }

    static int hashCode(final UnionTypeDefinition type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(), type.getUnits(),
            type.getDefaultValue(), type.getTypes());
    }

    static boolean equals(final UnionTypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }

        return equals(UnionTypeDefinition.class, type, obj)
                && type.getTypes().equals(((UnionTypeDefinition) obj).getTypes());
    }

    static String toString(final UnionTypeDefinition type) {
        return toStringHelper(type).add("types", type.getTypes()).toString();
    }

    static int hashCode(final UnsignedIntegerTypeDefinition type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(), type.getUnits(),
            type.getDefaultValue(), type.getRangeConstraints());
    }

    static boolean equals(final UnsignedIntegerTypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }

        return equals(UnsignedIntegerTypeDefinition.class, type, obj)
                && type.getRangeConstraints().equals(((UnsignedIntegerTypeDefinition) obj).getRangeConstraints());
    }

    static String toString(final UnsignedIntegerTypeDefinition type) {
        return toStringHelper(type).add("range", type.getRangeConstraints()).toString();
    }
}
