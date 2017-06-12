/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.annotations.Beta;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
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

/**
 * YANG defines 'derived type' as a type created through a 'typedef' statement. These types are exposed in the
 * hierarchical namespace and can be looked up.
 *
 * <p>
 * A derived type can redefine the default value, description, status and reference of a particular type definition.
 * It can only refine the units attribute, as that attribute is tied to the semantics of the value. The default value,
 * and units attributes are inherited from the super (base or restricted) type, others are left undefined if not
 * explicitly set. Status defaults to current.
 */
@Beta
public final class DerivedTypes {
    private DerivedTypes() {
        throw new UnsupportedOperationException();
    }

    public static DerivedTypeBuilder<?> derivedTypeBuilder(@Nonnull final TypeDefinition<?> baseType,
            @Nonnull final SchemaPath path) {
        if (baseType instanceof BinaryTypeDefinition) {
            return derivedBinaryBuilder((BinaryTypeDefinition) baseType, path);
        } else if (baseType instanceof BitsTypeDefinition) {
            return derivedBitsBuilder((BitsTypeDefinition) baseType, path);
        } else if (baseType instanceof BooleanTypeDefinition) {
            return derivedBooleanBuilder((BooleanTypeDefinition) baseType, path);
        } else if (baseType instanceof DecimalTypeDefinition) {
            return derivedDecimalBuilder((DecimalTypeDefinition) baseType, path);
        } else if (baseType instanceof EmptyTypeDefinition) {
            return derivedEmptyBuilder((EmptyTypeDefinition) baseType, path);
        } else if (baseType instanceof EnumTypeDefinition) {
            return derivedEnumerationBuilder((EnumTypeDefinition) baseType, path);
        } else if (baseType instanceof IdentityrefTypeDefinition) {
            return derivedIdentityrefBuilder((IdentityrefTypeDefinition) baseType, path);
        } else if (baseType instanceof InstanceIdentifierTypeDefinition) {
            return derivedInstanceIdentifierBuilder((InstanceIdentifierTypeDefinition) baseType, path);
        } else if (baseType instanceof IntegerTypeDefinition) {
            return derivedIntegerBuilder((IntegerTypeDefinition) baseType, path);
        } else if (baseType instanceof LeafrefTypeDefinition) {
            return derivedLeafrefBuilder((LeafrefTypeDefinition) baseType, path);
        } else if (baseType instanceof StringTypeDefinition) {
            return derivedStringBuilder((StringTypeDefinition) baseType, path);
        } else if (baseType instanceof UnionTypeDefinition) {
            return derivedUnionBuilder((UnionTypeDefinition) baseType, path);
        } else if (baseType instanceof UnsignedIntegerTypeDefinition) {
            return derivedUnsignedBuilder((UnsignedIntegerTypeDefinition) baseType, path);
        } else {
            throw new IllegalArgumentException("Unhandled type definition class " + baseType.getClass());
        }
    }

    /**
     * Check if a particular type is corresponds to int8. Unlike {@link BaseTypes#isInt8(TypeDefinition)}, this
     * method performs recursive lookup to find the base type.
     *
     * @param type The type to check
     * @return If the type belongs to the int8 type family.
     * @throws NullPointerException if type is null
     */
    public static boolean isInt8(@Nonnull final TypeDefinition<?> type) {
        return BaseTypes.isInt8(BaseTypes.baseTypeOf(type));
    }

    /**
     * Check if a particular type is corresponds to int16. Unlike {@link BaseTypes#isInt16(TypeDefinition)}, this
     * method performs recursive lookup to find the base type.
     *
     * @param type The type to check
     * @return If the type belongs to the int16 type family.
     * @throws NullPointerException if type is null
     */
    public static boolean isInt16(@Nonnull final TypeDefinition<?> type) {
        return BaseTypes.isInt16(BaseTypes.baseTypeOf(type));
    }

    /**
     * Check if a particular type is corresponds to int32. Unlike {@link BaseTypes#isInt32(TypeDefinition)}, this
     * method performs recursive lookup to find the base type.
     *
     * @param type The type to check
     * @return If the type belongs to the int32 type family.
     * @throws NullPointerException if type is null
     */
    public static boolean isInt32(@Nonnull final TypeDefinition<?> type) {
        return BaseTypes.isInt32(BaseTypes.baseTypeOf(type));
    }

    /**
     * Check if a particular type is corresponds to int64. Unlike {@link BaseTypes#isInt64(TypeDefinition)}, this
     * method performs recursive lookup to find the base type.
     *
     * @param type The type to check
     * @return If the type belongs to the int64 type family.
     * @throws NullPointerException if type is null
     */
    public static boolean isInt64(@Nonnull final TypeDefinition<?> type) {
        return BaseTypes.isInt64(BaseTypes.baseTypeOf(type));
    }

    /**
     * Check if a particular type is corresponds to uint8. Unlike {@link BaseTypes#isUint8(TypeDefinition)}, this
     * method performs recursive lookup to find the base type.
     *
     * @param type The type to check
     * @return If the type belongs to the uint8 type family.
     * @throws NullPointerException if type is null
     */
    public static boolean isUint8(@Nonnull final TypeDefinition<?> type) {
        return BaseTypes.isUint8(BaseTypes.baseTypeOf(type));
    }

    /**
     * Check if a particular type is corresponds to uint16. Unlike {@link BaseTypes#isUint16(TypeDefinition)}, this
     * method performs recursive lookup to find the base type.
     *
     * @param type The type to check
     * @return If the type belongs to the uint16 type family.
     * @throws NullPointerException if type is null
     */
    public static boolean isUint16(@Nonnull final TypeDefinition<?> type) {
        return BaseTypes.isUint16(BaseTypes.baseTypeOf(type));
    }

    /**
     * Check if a particular type is corresponds to uint32. Unlike {@link BaseTypes#isUint32(TypeDefinition)}, this
     * method performs recursive lookup to find the base type.
     *
     * @param type The type to check
     * @return If the type belongs to the uint32 type family.
     * @throws NullPointerException if type is null
     */
    public static boolean isUint32(@Nonnull final TypeDefinition<?> type) {
        return BaseTypes.isUint32(BaseTypes.baseTypeOf(type));
    }

    /**
     * Check if a particular type is corresponds to uint64. Unlike {@link BaseTypes#isUint64(TypeDefinition)}, this
     * method performs recursive lookup to find the base type.
     *
     * @param type The type to check
     * @return If the type belongs to the uint64 type family.
     * @throws NullPointerException if type is null
     */
    public static boolean isUint64(@Nonnull final TypeDefinition<?> type) {
        return BaseTypes.isUint64(BaseTypes.baseTypeOf(type));
    }

    private static DerivedTypeBuilder<BinaryTypeDefinition> derivedBinaryBuilder(
            @Nonnull final BinaryTypeDefinition baseType, @Nonnull final SchemaPath path) {
        return new DerivedTypeBuilder<BinaryTypeDefinition>(baseType, path) {
            @Override
            public BinaryTypeDefinition build() {
                return new DerivedBinaryType(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static DerivedTypeBuilder<BitsTypeDefinition> derivedBitsBuilder(final BitsTypeDefinition baseType,
            final SchemaPath path) {
        return new DerivedTypeBuilder<BitsTypeDefinition>(baseType, path) {
            @Override
            public BitsTypeDefinition build() {
                return new DerivedBitsType(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static DerivedTypeBuilder<BooleanTypeDefinition> derivedBooleanBuilder(
            @Nonnull final BooleanTypeDefinition baseType, @Nonnull final SchemaPath path) {
        return new DerivedTypeBuilder<BooleanTypeDefinition>(baseType, path) {
            @Override
            public BooleanTypeDefinition build() {
                return new DerivedBooleanType(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static DerivedTypeBuilder<DecimalTypeDefinition> derivedDecimalBuilder(final DecimalTypeDefinition baseType,
            final SchemaPath path) {
        return new DerivedTypeBuilder<DecimalTypeDefinition>(baseType, path) {
            @Override
            public DecimalTypeDefinition build() {
                return new DerivedDecimalType(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static DerivedTypeBuilder<EmptyTypeDefinition> derivedEmptyBuilder(final EmptyTypeDefinition baseType,
            final SchemaPath path) {
        return new DerivedTypeBuilder<EmptyTypeDefinition>(baseType, path) {
            @Override
            public EmptyTypeDefinition build() {
                return new DerivedEmptyType(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static DerivedTypeBuilder<EnumTypeDefinition> derivedEnumerationBuilder(final EnumTypeDefinition baseType,
            final SchemaPath path) {
        return new DerivedTypeBuilder<EnumTypeDefinition>(baseType, path) {
            @Override
            public EnumTypeDefinition build() {
                return new DerivedEnumerationType(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static DerivedTypeBuilder<IdentityrefTypeDefinition> derivedIdentityrefBuilder(
            final IdentityrefTypeDefinition baseType, final SchemaPath path) {
        return new DerivedTypeBuilder<IdentityrefTypeDefinition>(baseType, path) {
            @Override
            public IdentityrefTypeDefinition build() {
                return new DerivedIdentityrefType(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static DerivedTypeBuilder<InstanceIdentifierTypeDefinition> derivedInstanceIdentifierBuilder(
            final InstanceIdentifierTypeDefinition baseType, final SchemaPath path) {
        return new DerivedTypeBuilder<InstanceIdentifierTypeDefinition>(baseType, path) {
            @Override
            public InstanceIdentifierTypeDefinition build() {
                return new DerivedInstanceIdentifierType(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes(), baseType.requireInstance());
            }
        };
    }

    private static DerivedTypeBuilder<IntegerTypeDefinition> derivedIntegerBuilder(final IntegerTypeDefinition baseType,
            final SchemaPath path) {
        return new DerivedTypeBuilder<IntegerTypeDefinition>(baseType, path) {
            @Override
            public IntegerTypeDefinition build() {
                return new DerivedIntegerType(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static DerivedTypeBuilder<LeafrefTypeDefinition> derivedLeafrefBuilder(final LeafrefTypeDefinition baseType,
            final SchemaPath path) {
        return new DerivedTypeBuilder<LeafrefTypeDefinition>(baseType, path) {
            @Override
            public LeafrefTypeDefinition build() {
                return new DerivedLeafrefType(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static DerivedTypeBuilder<StringTypeDefinition> derivedStringBuilder(final StringTypeDefinition baseType,
            final SchemaPath path) {
        return new DerivedTypeBuilder<StringTypeDefinition>(baseType, path) {
            @Override
            public StringTypeDefinition build() {
                return new DerivedStringType(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static DerivedTypeBuilder<UnionTypeDefinition> derivedUnionBuilder(final UnionTypeDefinition baseType,
            final SchemaPath path) {
        return new DerivedTypeBuilder<UnionTypeDefinition>(baseType, path) {
            @Override
            public DerivedUnionType build() {
                return new DerivedUnionType(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static DerivedTypeBuilder<UnsignedIntegerTypeDefinition> derivedUnsignedBuilder(
            final UnsignedIntegerTypeDefinition baseType, final SchemaPath path) {
        return new DerivedTypeBuilder<UnsignedIntegerTypeDefinition>(baseType, path) {
            @Override
            public UnsignedIntegerTypeDefinition build() {
                return new DerivedUnsignedType(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }
}
