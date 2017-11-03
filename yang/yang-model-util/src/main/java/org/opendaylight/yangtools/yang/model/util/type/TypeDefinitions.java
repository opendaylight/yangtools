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
import org.opendaylight.yangtools.yang.model.api.type.Int16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int8TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.RangeRestrictedTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint8TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;

// TODO: this should be in the API package, as it defines equality for TypeDefinitions
final class TypeDefinitions {
    private TypeDefinitions() {
        throw new UnsupportedOperationException();
    }

    private static int basicHashCode(final TypeDefinition<?> type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(),
            type.getUnits().orElse(null), type.getDefaultValue().orElse(null));
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
                .add("default", type.getDefaultValue().orElse(null))
                .add("description", type.getDescription().orElse(null))
                .add("path", type.getPath())
                .add("reference", type.getReference().orElse(null))
                .add("status", type.getStatus())
                .add("units", type.getUnits().orElse(null));
    }

    static int hashCode(final BinaryTypeDefinition type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(),
            type.getUnits().orElse(null), type.getDefaultValue().orElse(null), type.getLengthConstraint().orElse(null));
    }

    static int hashCode(final BitsTypeDefinition type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(),
            type.getUnits().orElse(null), type.getDefaultValue().orElse(null), type.getBits());
    }


    static int hashCode(final BooleanTypeDefinition type) {
        return basicHashCode(type);
    }

    static int hashCode(final DecimalTypeDefinition type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(),
            type.getUnits().orElse(null), type.getDefaultValue().orElse(null), type.getFractionDigits(),
            type.getRangeConstraint().orElse(null));
    }

    static int hashCode(final EmptyTypeDefinition type) {
        return basicHashCode(type);
    }

    static int hashCode(final EnumTypeDefinition type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(),
            type.getUnits().orElse(null),
            type.getDefaultValue(), type.getValues());
    }

    static int hashCode(final IdentityrefTypeDefinition type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(),
            type.getUnits().orElse(null), type.getDefaultValue().orElse(null), type.getIdentities());
    }

    static int hashCode(final InstanceIdentifierTypeDefinition type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(),
            type.getUnits().orElse(null), type.getDefaultValue().orElse(null), type.requireInstance());
    }

    private static int hashCode(final RangeRestrictedTypeDefinition<?, ?> type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(),
            type.getUnits().orElse(null), type.getDefaultValue().orElse(null), type.getRangeConstraint().orElse(null));
    }

    static int hashCode(final Int8TypeDefinition type) {
        return hashCode((RangeRestrictedTypeDefinition<?, ?>) type);
    }

    static int hashCode(final Int16TypeDefinition type) {
        return hashCode((RangeRestrictedTypeDefinition<?, ?>) type);
    }

    static int hashCode(final Int32TypeDefinition type) {
        return hashCode((RangeRestrictedTypeDefinition<?, ?>) type);
    }

    static int hashCode(final Int64TypeDefinition type) {
        return hashCode((RangeRestrictedTypeDefinition<?, ?>) type);
    }

    static int hashCode(final LeafrefTypeDefinition type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(),
            type.getUnits().orElse(null), type.getDefaultValue().orElse(null), type.getPathStatement());
    }

    static int hashCode(final StringTypeDefinition type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(),
            type.getUnits().orElse(null), type.getDefaultValue().orElse(null), type.getLengthConstraint().orElse(null),
            type.getPatternConstraints());
    }

    static int hashCode(final UnionTypeDefinition type) {
        return Objects.hash(type.getPath(), type.getUnknownSchemaNodes(), type.getBaseType(),
            type.getUnits().orElse(null), type.getDefaultValue().orElse(null), type.getTypes());
    }

    static int hashCode(final Uint8TypeDefinition type) {
        return hashCode((RangeRestrictedTypeDefinition<?, ?>) type);
    }

    static int hashCode(final Uint16TypeDefinition type) {
        return hashCode((RangeRestrictedTypeDefinition<?, ?>) type);
    }

    static int hashCode(final Uint32TypeDefinition type) {
        return hashCode((RangeRestrictedTypeDefinition<?, ?>) type);
    }

    static int hashCode(final Uint64TypeDefinition type) {
        return hashCode((RangeRestrictedTypeDefinition<?, ?>) type);
    }

    static boolean equals(final BinaryTypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }

        final BinaryTypeDefinition other = castIfEquals(BinaryTypeDefinition.class, type, obj);
        return other != null && type.getLengthConstraint().equals(other.getLengthConstraint());
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
        return other != null && type.getFractionDigits() == other.getFractionDigits()
                && type.getRangeConstraint().equals(other.getRangeConstraint());
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
        return other != null && type.getIdentities().equals(other.getIdentities());
    }

    static boolean equals(final InstanceIdentifierTypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }

        final InstanceIdentifierTypeDefinition other = castIfEquals(InstanceIdentifierTypeDefinition.class, type, obj);
        return other != null && type.requireInstance() == other.requireInstance();
    }

    static boolean equals(final Int8TypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }

        final Int8TypeDefinition other = castIfEquals(Int8TypeDefinition.class, type, obj);
        return other != null && type.getRangeConstraint().equals(other.getRangeConstraint());
    }

    static boolean equals(final Int16TypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }

        final Int16TypeDefinition other = castIfEquals(Int16TypeDefinition.class, type, obj);
        return other != null && type.getRangeConstraint().equals(other.getRangeConstraint());
    }

    static boolean equals(final Int32TypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }

        final Int32TypeDefinition other = castIfEquals(Int32TypeDefinition.class, type, obj);
        return other != null && type.getRangeConstraint().equals(other.getRangeConstraint());
    }

    static boolean equals(final Int64TypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }

        final Int64TypeDefinition other = castIfEquals(Int64TypeDefinition.class, type, obj);
        return other != null && type.getRangeConstraint().equals(other.getRangeConstraint());
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
        return other != null && type.getLengthConstraint().equals(other.getLengthConstraint())
                && type.getPatternConstraints().equals(other.getPatternConstraints());
    }

    static boolean equals(final UnionTypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }

        final UnionTypeDefinition other = castIfEquals(UnionTypeDefinition.class, type, obj);
        return other != null && type.getTypes().equals(other.getTypes());
    }

    static boolean equals(final Uint8TypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }

        final Uint8TypeDefinition other = castIfEquals(Uint8TypeDefinition.class, type, obj);
        return other != null && type.getRangeConstraint().equals(other.getRangeConstraint());
    }

    static boolean equals(final Uint16TypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }

        final Uint16TypeDefinition other = castIfEquals(Uint16TypeDefinition.class, type, obj);
        return other != null && type.getRangeConstraint().equals(other.getRangeConstraint());
    }

    static boolean equals(final Uint32TypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }

        final Uint32TypeDefinition other = castIfEquals(Uint32TypeDefinition.class, type, obj);
        return other != null && type.getRangeConstraint().equals(other.getRangeConstraint());
    }

    static boolean equals(final Uint64TypeDefinition type, final Object obj) {
        if (type == obj) {
            return true;
        }

        final Uint64TypeDefinition other = castIfEquals(Uint64TypeDefinition.class, type, obj);
        return other != null && type.getRangeConstraint().equals(other.getRangeConstraint());
    }

    static String toString(final BinaryTypeDefinition type) {
        return toStringHelper(type).add("length", type.getLengthConstraint().orElse(null)).toString();
    }

    static String toString(final BitsTypeDefinition type) {
        return toStringHelper(type).add("bits", type.getBits()).toString();
    }

    static String toString(final BooleanTypeDefinition type) {
        return toStringHelper(type).toString();
    }

    static String toString(final DecimalTypeDefinition type) {
        return toStringHelper(type).add("fractionDigits", type.getFractionDigits())
                .add("range", type.getRangeConstraint().orElse(null)).toString();
    }

    static String toString(final EmptyTypeDefinition type) {
        return toStringHelper(type).toString();
    }

    static String toString(final EnumTypeDefinition type) {
        return toStringHelper(type).add("values", type.getValues()).toString();
    }

    static String toString(final IdentityrefTypeDefinition type) {
        return toStringHelper(type).add("identities", type.getIdentities()).toString();
    }

    static String toString(final InstanceIdentifierTypeDefinition type) {
        return toStringHelper(type).add("requireInstance", type.requireInstance()).toString();
    }

    static String toString(final Int8TypeDefinition type) {
        return toStringHelper(type).add("range", type.getRangeConstraint().orElse(null)).toString();
    }

    static String toString(final Int16TypeDefinition type) {
        return toStringHelper(type).add("range", type.getRangeConstraint().orElse(null)).toString();
    }

    static String toString(final Int32TypeDefinition type) {
        return toStringHelper(type).add("range", type.getRangeConstraint().orElse(null)).toString();
    }

    static String toString(final Int64TypeDefinition type) {
        return toStringHelper(type).add("range", type.getRangeConstraint().orElse(null)).toString();
    }

    static String toString(final LeafrefTypeDefinition type) {
        return toStringHelper(type).add("pathStatement", type.getPathStatement()).toString();
    }

    static String toString(final StringTypeDefinition type) {
        return toStringHelper(type).add("length", type.getLengthConstraint().orElse(null))
                .add("patterns", type.getPatternConstraints()).toString();
    }

    static String toString(final UnionTypeDefinition type) {
        return toStringHelper(type).add("types", type.getTypes()).toString();
    }

    static String toString(final Uint8TypeDefinition type) {
        return toStringHelper(type).add("range", type.getRangeConstraint().orElse(null)).toString();
    }

    static String toString(final Uint16TypeDefinition type) {
        return toStringHelper(type).add("range", type.getRangeConstraint().orElse(null)).toString();
    }

    static String toString(final Uint32TypeDefinition type) {
        return toStringHelper(type).add("range", type.getRangeConstraint().orElse(null)).toString();
    }

    static String toString(final Uint64TypeDefinition type) {
        return toStringHelper(type).add("range", type.getRangeConstraint().orElse(null)).toString();
    }

}
