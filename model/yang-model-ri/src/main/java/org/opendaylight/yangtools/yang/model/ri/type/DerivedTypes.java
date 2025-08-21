/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.type;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
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
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint8TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;

/**
 * YANG defines 'derived type' as a type created through a 'typedef' statement. These types are exposed in the
 * hierarchical namespace and can be looked up.
 *
 * <p>A derived type can redefine the default value, description, status and reference of a particular type definition.
 * It can only refine the units attribute, as that attribute is tied to the semantics of the value. The default value,
 * and units attributes are inherited from the super (base or restricted) type, others are left undefined if not
 * explicitly set. Status defaults to current.
 */
@Beta
public final class DerivedTypes {
    private DerivedTypes() {
        // Hidden on purpose
    }

    public static @NonNull DerivedTypeBuilder<?> derivedTypeBuilder(final @NonNull TypeDefinition<?> baseType,
            final @NonNull QName qname) {
        return typeBuilderOf(baseType, qname);
    }

    private static @NonNull DerivedTypeBuilder<?> typeBuilderOf(final @NonNull TypeDefinition<?> baseType,
            final @NonNull QName qname) {
        return switch (baseType) {
            case BinaryTypeDefinition def -> derivedBinaryBuilder(def, qname);
            case BitsTypeDefinition def -> derivedBitsBuilder(def, qname);
            case BooleanTypeDefinition def -> derivedBooleanBuilder(def, qname);
            case DecimalTypeDefinition def -> derivedDecimalBuilder(def, qname);
            case EmptyTypeDefinition def -> derivedEmptyBuilder(def, qname);
            case EnumTypeDefinition def -> derivedEnumerationBuilder(def, qname);
            case IdentityrefTypeDefinition def -> derivedIdentityrefBuilder(def, qname);
            case InstanceIdentifierTypeDefinition def -> derivedInstanceIdentifierBuilder(def, qname);
            case Int8TypeDefinition def -> derivedInt8Builder(def, qname);
            case Int16TypeDefinition def -> derivedInt16Builder(def, qname);
            case Int32TypeDefinition def -> derivedInt32Builder(def, qname);
            case Int64TypeDefinition def -> derivedInt64Builder(def, qname);
            case LeafrefTypeDefinition def -> derivedLeafrefBuilder(def, qname);
            case StringTypeDefinition def -> derivedStringBuilder(def, qname);
            case UnionTypeDefinition def -> derivedUnionBuilder(def, qname);
            case Uint8TypeDefinition def -> derivedUint8Builder(def, qname);
            case Uint16TypeDefinition def -> derivedUint16Builder(def, qname);
            case Uint32TypeDefinition def -> derivedUint32Builder(def, qname);
            case Uint64TypeDefinition def -> derivedUint64Builder(def, qname);
            default -> throw new IllegalArgumentException("Unhandled type definition class " + baseType.getClass());
        };
    }

    public static @NonNull DerivedTypeBuilder<BinaryTypeDefinition> derivedBinaryBuilder(
            final @NonNull BinaryTypeDefinition baseType, final @NonNull QName qname) {
        return new DerivedTypeBuilder<>(baseType, qname) {
            @Override
            public BinaryTypeDefinition build() {
                return new DerivedBinaryType(getBaseType(), getQName(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static @NonNull DerivedTypeBuilder<BitsTypeDefinition> derivedBitsBuilder(final BitsTypeDefinition baseType,
            final QName qname) {
        return new DerivedTypeBuilder<>(baseType, qname) {
            @Override
            public BitsTypeDefinition build() {
                return new DerivedBitsType(getBaseType(), getQName(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static @NonNull DerivedTypeBuilder<BooleanTypeDefinition> derivedBooleanBuilder(
            final @NonNull BooleanTypeDefinition baseType, final @NonNull QName qname) {
        return new DerivedTypeBuilder<>(baseType, qname) {
            @Override
            public BooleanTypeDefinition build() {
                return new DerivedBooleanType(getBaseType(), getQName(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static @NonNull DerivedTypeBuilder<DecimalTypeDefinition> derivedDecimalBuilder(
            final DecimalTypeDefinition baseType, final QName qname) {
        return new DerivedTypeBuilder<>(baseType, qname) {
            @Override
            public DecimalTypeDefinition build() {
                return new DerivedDecimalType(getBaseType(), getQName(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static @NonNull DerivedTypeBuilder<EmptyTypeDefinition> derivedEmptyBuilder(
            final EmptyTypeDefinition baseType, final QName qname) {
        return new DerivedTypeBuilder<>(baseType, qname) {
            @Override
            public EmptyTypeDefinition build() {
                return new DerivedEmptyType(getBaseType(), getQName(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static @NonNull DerivedTypeBuilder<EnumTypeDefinition> derivedEnumerationBuilder(
            final EnumTypeDefinition baseType, final QName qname) {
        return new DerivedTypeBuilder<>(baseType, qname) {
            @Override
            public EnumTypeDefinition build() {
                return new DerivedEnumerationType(getBaseType(), getQName(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static @NonNull DerivedTypeBuilder<IdentityrefTypeDefinition> derivedIdentityrefBuilder(
            final IdentityrefTypeDefinition baseType, final QName qname) {
        return new DerivedTypeBuilder<>(baseType, qname) {
            @Override
            public IdentityrefTypeDefinition build() {
                return new DerivedIdentityrefType(getBaseType(), getQName(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static @NonNull DerivedTypeBuilder<InstanceIdentifierTypeDefinition> derivedInstanceIdentifierBuilder(
            final InstanceIdentifierTypeDefinition baseType, final QName qname) {
        return new DerivedTypeBuilder<>(baseType, qname) {
            @Override
            public InstanceIdentifierTypeDefinition build() {
                return new DerivedInstanceIdentifierType(getBaseType(), getQName(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes(), baseType.requireInstance());
            }
        };
    }

    private static @NonNull DerivedTypeBuilder<Int8TypeDefinition> derivedInt8Builder(final Int8TypeDefinition baseType,
            final QName qname) {
        return new DerivedTypeBuilder<>(baseType, qname) {
            @Override
            public Int8TypeDefinition build() {
                return new DerivedInt8Type(getBaseType(), getQName(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static @NonNull DerivedTypeBuilder<Int16TypeDefinition> derivedInt16Builder(
            final Int16TypeDefinition baseType, final QName qname) {
        return new DerivedTypeBuilder<>(baseType, qname) {
            @Override
            public Int16TypeDefinition build() {
                return new DerivedInt16Type(getBaseType(), getQName(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static @NonNull DerivedTypeBuilder<Int32TypeDefinition> derivedInt32Builder(
            final Int32TypeDefinition baseType, final QName qname) {
        return new DerivedTypeBuilder<>(baseType, qname) {
            @Override
            public Int32TypeDefinition build() {
                return new DerivedInt32Type(getBaseType(), getQName(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static @NonNull DerivedTypeBuilder<Int64TypeDefinition> derivedInt64Builder(
            final Int64TypeDefinition baseType, final QName qname) {
        return new DerivedTypeBuilder<>(baseType, qname) {
            @Override
            public Int64TypeDefinition build() {
                return new DerivedInt64Type(getBaseType(), getQName(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static @NonNull DerivedTypeBuilder<LeafrefTypeDefinition> derivedLeafrefBuilder(
            final LeafrefTypeDefinition baseType, final QName qname) {
        return new DerivedTypeBuilder<>(baseType, qname) {
            @Override
            public LeafrefTypeDefinition build() {
                return new DerivedLeafrefType(getBaseType(), getQName(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static @NonNull DerivedTypeBuilder<StringTypeDefinition> derivedStringBuilder(
            final StringTypeDefinition baseType, final QName qname) {
        return new DerivedTypeBuilder<>(baseType, qname) {
            @Override
            public StringTypeDefinition build() {
                return new DerivedStringType(getBaseType(), getQName(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static @NonNull DerivedTypeBuilder<UnionTypeDefinition> derivedUnionBuilder(
            final UnionTypeDefinition baseType, final QName qname) {
        return new DerivedTypeBuilder<>(baseType, qname) {
            @Override
            public DerivedUnionType build() {
                return new DerivedUnionType(getBaseType(), getQName(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static @NonNull DerivedTypeBuilder<Uint8TypeDefinition> derivedUint8Builder(
            final Uint8TypeDefinition baseType, final QName qname) {
        return new DerivedTypeBuilder<>(baseType, qname) {
            @Override
            public Uint8TypeDefinition build() {
                return new DerivedUint8Type(getBaseType(), getQName(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static @NonNull DerivedTypeBuilder<Uint16TypeDefinition> derivedUint16Builder(
            final Uint16TypeDefinition baseType, final QName qname) {
        return new DerivedTypeBuilder<>(baseType, qname) {
            @Override
            public Uint16TypeDefinition build() {
                return new DerivedUint16Type(getBaseType(), getQName(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static @NonNull DerivedTypeBuilder<Uint32TypeDefinition> derivedUint32Builder(
            final Uint32TypeDefinition baseType, final QName qname) {
        return new DerivedTypeBuilder<>(baseType, qname) {
            @Override
            public Uint32TypeDefinition build() {
                return new DerivedUint32Type(getBaseType(), getQName(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static @NonNull DerivedTypeBuilder<Uint64TypeDefinition> derivedUint64Builder(
            final Uint64TypeDefinition baseType, final QName qname) {
        return new DerivedTypeBuilder<>(baseType, qname) {
            @Override
            public Uint64TypeDefinition build() {
                return new DerivedUint64Type(getBaseType(), getQName(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }
}
