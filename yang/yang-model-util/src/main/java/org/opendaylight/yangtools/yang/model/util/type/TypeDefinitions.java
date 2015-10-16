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

final class TypeDefinitions {
    private TypeDefinitions() {
        throw new UnsupportedOperationException();
    }

    private static int basicHashCode(final TypeDefinition<?> type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(), type.getUnits(),
            type.getDefaultValue());
    }

    private static boolean basicEquals(final TypeDefinition<?> type, final TypeDefinition<?> other) {
        return Objects.equals(type.getPath(), other.getPath())
                && Objects.equals(type.getBaseType(), other.getBaseType())
                && Objects.equals(type.getDefaultValue(), other.getDefaultValue())
                && Objects.equals(type.getUnknownSchemaNodes(), other.getUnknownSchemaNodes())
                && Objects.equals(type.getUnits(), other.getUnits());
    }

    private static ToStringHelper basicToString(final TypeDefinition<?> type) {
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
        if (!(obj instanceof BinaryTypeDefinition)) {
            return false;
        }

        final BinaryTypeDefinition other = (BinaryTypeDefinition) obj;
        return basicEquals(type, other) && type.getLengthConstraints().equals(other.getLengthConstraints());
    }

    static String toString(final BinaryTypeDefinition type) {
        return basicToString(type).add("length", type.getLengthConstraints()).toString();
    }

    static int hashCode(final BitsTypeDefinition type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(), type.getUnits(),
            type.getDefaultValue(), type.getBits());
    }

    static boolean equals(final BitsTypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }
        if (!(obj instanceof BitsTypeDefinition)) {
            return false;
        }

        final BitsTypeDefinition other = (BitsTypeDefinition) obj;
        return basicEquals(type, other) && type.getBits().equals(other.getBits());
    }

    static String toString(final BitsTypeDefinition type) {
        return basicToString(type).add("bits", type.getBits()).toString();
    }

    static int hashCode(final BooleanTypeDefinition type) {
        return basicHashCode(type);
    }

    static boolean equals(final BooleanTypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }
        if (!(obj instanceof BooleanTypeDefinition)) {
            return false;
        }

        final BooleanTypeDefinition other = (BooleanTypeDefinition) obj;
        return basicEquals(type, other);
    }

    static String toString(final BooleanTypeDefinition type) {
        return basicToString(type).toString();
    }

    static int hashCode(final DecimalTypeDefinition type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(), type.getUnits(),
            type.getDefaultValue(), type.getFractionDigits(), type.getRangeConstraints());
    }

    static boolean equals(final DecimalTypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }
        if (!(obj instanceof DecimalTypeDefinition)) {
            return false;
        }

        final DecimalTypeDefinition other = (DecimalTypeDefinition) obj;
        return basicEquals(type, other) && type.getFractionDigits().equals(other.getFractionDigits())
                && type.getRangeConstraints().equals(other.getRangeConstraints());
    }

    static String toString(final DecimalTypeDefinition type) {
        return basicToString(type).add("fractionDigits", type.getFractionDigits())
                .add("range", type.getRangeConstraints()).toString();
    }

    static int hashCode(final EmptyTypeDefinition type) {
        return basicHashCode(type);
    }

    static boolean equals(final EmptyTypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }
        if (!(obj instanceof EmptyTypeDefinition)) {
            return false;
        }

        final EmptyTypeDefinition other = (EmptyTypeDefinition) obj;
        return basicEquals(type, other);
    }

    static String toString(final EmptyTypeDefinition type) {
        return basicToString(type).toString();
    }

    static int hashCode(final EnumTypeDefinition type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(), type.getUnits(),
            type.getDefaultValue(), type.getValues());
    }

    static boolean equals(final EnumTypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }
        if (!(obj instanceof EnumTypeDefinition)) {
            return false;
        }

        final EnumTypeDefinition other = (EnumTypeDefinition) obj;
        return basicEquals(type, other) && type.getValues().equals(other.getValues());
    }

    static String toString(final EnumTypeDefinition type) {
        return basicToString(type).add("values", type.getValues()).toString();
    }

    static int hashCode(final IdentityrefTypeDefinition type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(), type.getUnits(),
            type.getDefaultValue(), type.getIdentity());
    }

    static boolean equals(final IdentityrefTypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }
        if (!(obj instanceof IdentityrefTypeDefinition)) {
            return false;
        }

        final IdentityrefTypeDefinition other = (IdentityrefTypeDefinition) obj;
        return basicEquals(type, other) && type.getIdentity().equals(other.getIdentity());
    }

    static String toString(final IdentityrefTypeDefinition type) {
        return basicToString(type).add("identity", type.getIdentity()).toString();
    }

    static int hashCode(final InstanceIdentifierTypeDefinition type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(), type.getUnits(),
            type.getDefaultValue(), type.requireInstance());
    }

    static boolean equals(final InstanceIdentifierTypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }
        if (!(obj instanceof InstanceIdentifierTypeDefinition)) {
            return false;
        }

        final InstanceIdentifierTypeDefinition other = (InstanceIdentifierTypeDefinition) obj;
        return basicEquals(type, other) && type.requireInstance() == other.requireInstance();
    }

    static String toString(final InstanceIdentifierTypeDefinition type) {
        return basicToString(type).add("requireInstance", type.requireInstance()).toString();
    }

    static int hashCode(final IntegerTypeDefinition type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(), type.getUnits(),
            type.getDefaultValue(), type.getRangeConstraints());
    }

    static boolean equals(final IntegerTypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }
        if (!(obj instanceof IntegerTypeDefinition)) {
            return false;
        }

        final IntegerTypeDefinition other = (IntegerTypeDefinition) obj;
        return basicEquals(type, other) && type.getRangeConstraints().equals(other.getRangeConstraints());
    }

    static String toString(final IntegerTypeDefinition type) {
        return basicToString(type).add("range", type.getRangeConstraints()).toString();
    }

    static int hashCode(final LeafrefTypeDefinition type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(), type.getUnits(),
            type.getDefaultValue(), type.getPathStatement());
    }

    static boolean equals(final LeafrefTypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }
        if (!(obj instanceof LeafrefTypeDefinition)) {
            return false;
        }

        final LeafrefTypeDefinition other = (LeafrefTypeDefinition) obj;
        return basicEquals(type, other) && type.getPathStatement().equals(other.getPathStatement());
    }

    static String toString(final LeafrefTypeDefinition type) {
        return basicToString(type).add("pathStatement", type.getPathStatement()).toString();
    }

    static int hashCode(final StringTypeDefinition type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(), type.getUnits(),
            type.getDefaultValue(), type.getLengthConstraints(), type.getPatternConstraints());
    }

    static boolean equals(final StringTypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }
        if (!(obj instanceof StringTypeDefinition)) {
            return false;
        }

        final StringTypeDefinition other = (StringTypeDefinition) obj;
        return basicEquals(type, other) && type.getLengthConstraints().equals(other.getLengthConstraints())
                && type.getPatternConstraints().equals(other.getPatternConstraints());
    }

    static String toString(final StringTypeDefinition type) {
        return basicToString(type).add("length", type.getLengthConstraints())
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
        if (!(obj instanceof UnionTypeDefinition)) {
            return false;
        }

        final UnionTypeDefinition other = (UnionTypeDefinition) obj;
        return basicEquals(type, other) && type.getTypes().equals(other.getTypes());
    }

    static String toString(final UnionTypeDefinition type) {
        return basicToString(type).add("types", type.getTypes()).toString();
    }

    static int hashCode(final UnsignedIntegerTypeDefinition type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(), type.getUnits(),
            type.getDefaultValue(), type.getRangeConstraints());
    }

    static boolean equals(final UnsignedIntegerTypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }
        if (!(obj instanceof UnsignedIntegerTypeDefinition)) {
            return false;
        }

        final UnsignedIntegerTypeDefinition other = (UnsignedIntegerTypeDefinition) obj;
        return basicEquals(type, other) && type.getRangeConstraints().equals(other.getRangeConstraints());
    }

    static String toString(final UnsignedIntegerTypeDefinition type) {
        return basicToString(type).add("range", type.getRangeConstraints()).toString();
    }
}
