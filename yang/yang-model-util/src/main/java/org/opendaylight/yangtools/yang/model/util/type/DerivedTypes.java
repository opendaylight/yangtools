/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.math.BigDecimal;
import java.math.BigInteger;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
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

    public static DerivedTypeBuilder<?, ?> derivedTypeBuilder(@Nonnull final TypeDefinition<?, ?> baseType,
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
        } else if (baseType instanceof Int8TypeDefinition) {
            return derivedInt8Builder((Int8TypeDefinition) baseType, path);
        } else if (baseType instanceof Int16TypeDefinition) {
            return derivedInt16Builder((Int16TypeDefinition) baseType, path);
        } else if (baseType instanceof Int32TypeDefinition) {
            return derivedInt32Builder((Int32TypeDefinition) baseType, path);
        } else if (baseType instanceof Int64TypeDefinition) {
            return derivedInt64Builder((Int64TypeDefinition) baseType, path);
        } else if (baseType instanceof LeafrefTypeDefinition) {
            return derivedLeafrefBuilder((LeafrefTypeDefinition) baseType, path);
        } else if (baseType instanceof StringTypeDefinition) {
            return derivedStringBuilder((StringTypeDefinition) baseType, path);
        } else if (baseType instanceof UnionTypeDefinition) {
            return derivedUnionBuilder((UnionTypeDefinition) baseType, path);
        } else if (baseType instanceof Uint8TypeDefinition) {
            return derivedUint8Builder((Uint8TypeDefinition) baseType, path);
        } else if (baseType instanceof Uint16TypeDefinition) {
            return derivedUint16Builder((Uint16TypeDefinition) baseType, path);
        } else if (baseType instanceof Uint32TypeDefinition) {
            return derivedUint32Builder((Uint32TypeDefinition) baseType, path);
        } else if (baseType instanceof Uint64TypeDefinition) {
            return derivedUint64Builder((Uint64TypeDefinition) baseType, path);
        } else {
            throw new IllegalArgumentException("Unhandled type definition class " + baseType.getClass());
        }
    }

    /**
     * Check if a particular type is itself, or is derived from, int8.
     *
     * @param type The type to check
     * @return If the type belongs to the int8 type family.
     * @throws NullPointerException if type is null
     *
     * @deprecated Use @{code type instanceof Int8TypeDefinition} instead.
     */
    @Deprecated
    public static boolean isInt8(@Nonnull final TypeDefinition<?, ?> type) {
        return requireNonNull(type) instanceof Int8TypeDefinition;
    }

    /**
     * Check if a particular type is itself, or is derived from, int16.
     *
     * @param type The type to check
     * @return If the type belongs to the int16 type family.
     * @throws NullPointerException if type is null
     *
     * @deprecated Use @{code type instanceof Int16TypeDefinition} instead.
     */
    @Deprecated
    public static boolean isInt16(@Nonnull final TypeDefinition<?, ?> type) {
        return requireNonNull(type) instanceof Int16TypeDefinition;
    }

    /**
     * Check if a particular type is itself, or is derived from, int32.
     *
     * @param type The type to check
     * @return If the type belongs to the int32 type family.
     * @throws NullPointerException if type is null
     *
     * @deprecated Use @{code type instanceof Int32TypeDefinition} instead.
     */
    @Deprecated
    public static boolean isInt32(@Nonnull final TypeDefinition<?, ?> type) {
        return requireNonNull(type) instanceof Int32TypeDefinition;
    }

    /**
     * Check if a particular type is itself, or is derived from, int64.
     *
     * @param type The type to check
     * @return If the type belongs to the int64 type family.
     * @throws NullPointerException if type is null
     *
     * @deprecated Use @{code type instanceof Int64TypeDefinition} instead.
     */
    @Deprecated
    public static boolean isInt64(@Nonnull final TypeDefinition<?, ?> type) {
        return requireNonNull(type) instanceof Int64TypeDefinition;
    }

    /**
     * Check if a particular type is itself, or is derived from, uint8.
     *
     * @param type The type to check
     * @return If the type belongs to the uint8 type family.
     * @throws NullPointerException if type is null
     *
     * @deprecated Use @{code type instanceof Uint8TypeDefinition} instead.
     */
    @Deprecated
    public static boolean isUint8(@Nonnull final TypeDefinition<?, ?> type) {
        return requireNonNull(type) instanceof Uint8TypeDefinition;
    }

    /**
     * Check if a particular type is itself, or is derived from, uint16.
     *
     * @param type The type to check
     * @return If the type belongs to the uint16 type family.
     * @throws NullPointerException if type is null
     *
     * @deprecated Use @{code type instanceof Uint16TypeDefinition} instead.
     */
    @Deprecated
    public static boolean isUint16(@Nonnull final TypeDefinition<?, ?> type) {
        return requireNonNull(type) instanceof Uint16TypeDefinition;
    }

    /**
     * Check if a particular type is itself, or is derived from, uint32.
     *
     * @param type The type to check
     * @return If the type belongs to the uint32 type family.
     * @throws NullPointerException if type is null
     *
     * @deprecated Use @{code type instanceof Uint32TypeDefinition} instead.
     */
    @Deprecated
    public static boolean isUint32(@Nonnull final TypeDefinition<?, ?> type) {
        return requireNonNull(type) instanceof Uint32TypeDefinition;
    }

    /**
     * Check if a particular type is itself, or is derived from, uint64.
     *
     * @param type The type to check
     * @return If the type belongs to the uint64 type family.
     * @throws NullPointerException if type is null
     *
     * @deprecated Use @{code type instanceof Uint64TypeDefinition} instead.
     */
    @Deprecated
    public static boolean isUint64(@Nonnull final TypeDefinition<?, ?> type) {
        return requireNonNull(type) instanceof Uint64TypeDefinition;
    }

    private static DerivedTypeBuilder<BinaryTypeDefinition, byte[]> derivedBinaryBuilder(
            @Nonnull final BinaryTypeDefinition baseType, @Nonnull final SchemaPath path) {
        return new DerivedTypeBuilder<BinaryTypeDefinition, byte[]>(baseType, path) {
            @Override
            public BinaryTypeDefinition build() {
                return new DerivedBinaryType(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static DerivedTypeBuilder<BitsTypeDefinition, Object> derivedBitsBuilder(final BitsTypeDefinition baseType,
            final SchemaPath path) {
        return new DerivedTypeBuilder<BitsTypeDefinition, Object>(baseType, path) {
            @Override
            public BitsTypeDefinition build() {
                return new DerivedBitsType(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static DerivedTypeBuilder<BooleanTypeDefinition, Boolean> derivedBooleanBuilder(
            @Nonnull final BooleanTypeDefinition baseType, @Nonnull final SchemaPath path) {
        return new DerivedTypeBuilder<BooleanTypeDefinition, Boolean>(baseType, path) {
            @Override
            public BooleanTypeDefinition build() {
                return new DerivedBooleanType(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static DerivedTypeBuilder<DecimalTypeDefinition, BigDecimal> derivedDecimalBuilder(
            final DecimalTypeDefinition baseType, final SchemaPath path) {
        return new DerivedTypeBuilder<DecimalTypeDefinition, BigDecimal>(baseType, path) {
            @Override
            public DecimalTypeDefinition build() {
                return new DerivedDecimalType(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static DerivedTypeBuilder<EmptyTypeDefinition, Empty> derivedEmptyBuilder(
            final EmptyTypeDefinition baseType, final SchemaPath path) {
        return new DerivedTypeBuilder<EmptyTypeDefinition, Empty>(baseType, path) {
            @Override
            public EmptyTypeDefinition build() {
                return new DerivedEmptyType(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static DerivedTypeBuilder<EnumTypeDefinition, Object> derivedEnumerationBuilder(
            final EnumTypeDefinition baseType, final SchemaPath path) {
        return new DerivedTypeBuilder<EnumTypeDefinition, Object>(baseType, path) {
            @Override
            public EnumTypeDefinition build() {
                return new DerivedEnumerationType(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static DerivedTypeBuilder<IdentityrefTypeDefinition, QName> derivedIdentityrefBuilder(
            final IdentityrefTypeDefinition baseType, final SchemaPath path) {
        return new DerivedTypeBuilder<IdentityrefTypeDefinition, QName>(baseType, path) {
            @Override
            public IdentityrefTypeDefinition build() {
                return new DerivedIdentityrefType(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static DerivedTypeBuilder<InstanceIdentifierTypeDefinition, Object> derivedInstanceIdentifierBuilder(
            final InstanceIdentifierTypeDefinition baseType, final SchemaPath path) {
        return new DerivedTypeBuilder<InstanceIdentifierTypeDefinition, Object>(baseType, path) {
            @Override
            public InstanceIdentifierTypeDefinition build() {
                return new DerivedInstanceIdentifierType(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes(), baseType.requireInstance());
            }
        };
    }

    private static DerivedTypeBuilder<Int8TypeDefinition, Byte> derivedInt8Builder(final Int8TypeDefinition baseType,
            final SchemaPath path) {
        return new DerivedTypeBuilder<Int8TypeDefinition, Byte>(baseType, path) {
            @Override
            public Int8TypeDefinition build() {
                return new DerivedInt8Type(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static DerivedTypeBuilder<Int16TypeDefinition, Short> derivedInt16Builder(
            final Int16TypeDefinition baseType, final SchemaPath path) {
        return new DerivedTypeBuilder<Int16TypeDefinition, Short>(baseType, path) {
            @Override
            public Int16TypeDefinition build() {
                return new DerivedInt16Type(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static DerivedTypeBuilder<Int32TypeDefinition, Integer> derivedInt32Builder(
            final Int32TypeDefinition baseType, final SchemaPath path) {
        return new DerivedTypeBuilder<Int32TypeDefinition, Integer>(baseType, path) {
            @Override
            public Int32TypeDefinition build() {
                return new DerivedInt32Type(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static DerivedTypeBuilder<Int64TypeDefinition, Long> derivedInt64Builder(final Int64TypeDefinition baseType,
            final SchemaPath path) {
        return new DerivedTypeBuilder<Int64TypeDefinition, Long>(baseType, path) {
            @Override
            public Int64TypeDefinition build() {
                return new DerivedInt64Type(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static DerivedTypeBuilder<LeafrefTypeDefinition, Object> derivedLeafrefBuilder(
            final LeafrefTypeDefinition baseType, final SchemaPath path) {
        return new DerivedTypeBuilder<LeafrefTypeDefinition, Object>(baseType, path) {
            @Override
            public LeafrefTypeDefinition build() {
                return new DerivedLeafrefType(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static DerivedTypeBuilder<StringTypeDefinition, String> derivedStringBuilder(
            final StringTypeDefinition baseType, final SchemaPath path) {
        return new DerivedTypeBuilder<StringTypeDefinition, String>(baseType, path) {
            @Override
            public StringTypeDefinition build() {
                return new DerivedStringType(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static DerivedTypeBuilder<UnionTypeDefinition, Object> derivedUnionBuilder(
            final UnionTypeDefinition baseType, final SchemaPath path) {
        return new DerivedTypeBuilder<UnionTypeDefinition, Object>(baseType, path) {
            @Override
            public DerivedUnionType build() {
                return new DerivedUnionType(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static DerivedTypeBuilder<Uint8TypeDefinition, Short> derivedUint8Builder(
            final Uint8TypeDefinition baseType, final SchemaPath path) {
        return new DerivedTypeBuilder<Uint8TypeDefinition, Short>(baseType, path) {
            @Override
            public Uint8TypeDefinition build() {
                return new DerivedUint8Type(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static DerivedTypeBuilder<Uint16TypeDefinition, Integer> derivedUint16Builder(
            final Uint16TypeDefinition baseType, final SchemaPath path) {
        return new DerivedTypeBuilder<Uint16TypeDefinition, Integer>(baseType, path) {
            @Override
            public Uint16TypeDefinition build() {
                return new DerivedUint16Type(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static DerivedTypeBuilder<Uint32TypeDefinition, Long> derivedUint32Builder(
            final Uint32TypeDefinition baseType, final SchemaPath path) {
        return new DerivedTypeBuilder<Uint32TypeDefinition, Long>(baseType, path) {
            @Override
            public Uint32TypeDefinition build() {
                return new DerivedUint32Type(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static DerivedTypeBuilder<Uint64TypeDefinition, BigInteger> derivedUint64Builder(
            final Uint64TypeDefinition baseType, final SchemaPath path) {
        return new DerivedTypeBuilder<Uint64TypeDefinition, BigInteger>(baseType, path) {
            @Override
            public Uint64TypeDefinition build() {
                return new DerivedUint64Type(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }
}
