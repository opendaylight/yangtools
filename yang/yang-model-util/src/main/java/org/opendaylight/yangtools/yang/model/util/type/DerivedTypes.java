/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

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

public final class DerivedTypes {
    private DerivedTypes() {
        throw new UnsupportedOperationException();
    }

    public static DerivedTypeBuilder<?> newBuilder(@Nonnull final TypeDefinition<?> baseType, @Nonnull final SchemaPath path) {
        if (baseType instanceof BinaryTypeDefinition) {
            return newBinaryBuilder((BinaryTypeDefinition) baseType, path);
        } else if (baseType instanceof BitsTypeDefinition) {
            return newBitsBuilder((BitsTypeDefinition) baseType, path);
        } else if (baseType instanceof BooleanTypeDefinition) {
            return newBooleanBuilder((BooleanTypeDefinition) baseType, path);
        } else if (baseType instanceof DecimalTypeDefinition) {
            return newDecima64Builder((DecimalTypeDefinition) baseType, path);
        } else if (baseType instanceof EmptyTypeDefinition) {
            return newEmptyBuilder((EmptyTypeDefinition) baseType, path);
        } else if (baseType instanceof EnumTypeDefinition) {
            return newEnumerationBuilder((EnumTypeDefinition) baseType, path);
        } else if (baseType instanceof IdentityrefTypeDefinition) {
            return newIdentityrefBuilder((IdentityrefTypeDefinition) baseType, path);
        } else if (baseType instanceof InstanceIdentifierTypeDefinition) {
            return newInstanceIdentifierBuilder((InstanceIdentifierTypeDefinition) baseType, path);
        } else if (baseType instanceof IntegerTypeDefinition) {
            return newIntegerBuilder((IntegerTypeDefinition) baseType, path);
        } else if (baseType instanceof LeafrefTypeDefinition) {
            return newLeafrefBuilder((LeafrefTypeDefinition) baseType, path);
        } else if (baseType instanceof StringTypeDefinition) {
            return newStringBuilder((StringTypeDefinition) baseType, path);
        } else if (baseType instanceof UnionTypeDefinition) {
            return newUnionBuilder((UnionTypeDefinition) baseType, path);
        } else if (baseType instanceof UnsignedIntegerTypeDefinition) {
            return newUnsignedBuilder((UnsignedIntegerTypeDefinition) baseType, path);
        } else {
            throw new IllegalArgumentException("Unhandled type definition class " + baseType.getClass());
        }
    }

    public static DerivedTypeBuilder<BinaryTypeDefinition> newBinaryBuilder(@Nonnull final SchemaPath path) {
        return newBinaryBuilder(BaseBinaryType.INSTANCE, path);
    }

    public static DerivedTypeBuilder<BinaryTypeDefinition> newBinaryBuilder(@Nonnull final BinaryTypeDefinition baseType, @Nonnull final SchemaPath path) {
        return new DerivedTypeBuilder<BinaryTypeDefinition>(baseType, path) {
            @Override
            public BinaryTypeDefinition build() {
                return new DerivedBinaryType(getBaseType(), getPath(), getDefaultValue(), getDescription(), getReference(),
                    getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static DerivedTypeBuilder<BooleanTypeDefinition> newBooleanBuilder(final SchemaPath path) {
        return newBooleanBuilder(BaseBooleanType.INSTANCE, path);
    }

    public static DerivedTypeBuilder<BooleanTypeDefinition> newBooleanBuilder(@Nonnull final BooleanTypeDefinition baseType, @Nonnull final SchemaPath path) {
        return new DerivedTypeBuilder<BooleanTypeDefinition>(baseType, path) {
            @Override
            public BooleanTypeDefinition build() {
                return new DerivedBooleanType(getBaseType(), getPath(), getDefaultValue(), getDescription(), getReference(),
                    getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static DerivedTypeBuilder<EmptyTypeDefinition> newEmptyBuilder(final SchemaPath path) {
        return newEmptyBuilder(BaseEmptyType.INSTANCE, path);
    }

    public static DerivedTypeBuilder<EmptyTypeDefinition> newEmptyBuilder(final EmptyTypeDefinition baseType, final SchemaPath path) {
        return new DerivedTypeBuilder<EmptyTypeDefinition>(baseType, path) {
            @Override
            public EmptyTypeDefinition build() {
                return new DerivedEmptyType(getBaseType(), getPath(), getDefaultValue(), getDescription(), getReference(),
                    getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static DerivedTypeBuilder<IntegerTypeDefinition> newInt8Builder(final SchemaPath path) {
        return newIntegerBuilder(BaseInt8Type.INSTANCE, path);
    }

    public static DerivedTypeBuilder<IntegerTypeDefinition> newInt16Builder(final SchemaPath path) {
        return newIntegerBuilder(BaseInt16Type.INSTANCE, path);
    }

    public static DerivedTypeBuilder<IntegerTypeDefinition> newInt32Builder(final SchemaPath path) {
        return newIntegerBuilder(BaseInt32Type.INSTANCE, path);
    }

    public static DerivedTypeBuilder<IntegerTypeDefinition> newInt64Builder(final SchemaPath path) {
        return newIntegerBuilder(BaseInt64Type.INSTANCE, path);
    }

    public static DerivedTypeBuilder<IntegerTypeDefinition> newIntegerBuilder(final IntegerTypeDefinition baseType, final SchemaPath path) {
        return new DerivedTypeBuilder<IntegerTypeDefinition>(baseType, path) {
            @Override
            public IntegerTypeDefinition build() {
                return new DerivedIntegerType(getBaseType(), getPath(), getDefaultValue(), getDescription(), getReference(),
                    getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static DerivedTypeBuilder<UnsignedIntegerTypeDefinition> newUint8Builder(final SchemaPath path) {
        return newUnsignedBuilder(BaseUint8Type.INSTANCE, path);
    }

    public static DerivedTypeBuilder<UnsignedIntegerTypeDefinition> newUint16Builder(final SchemaPath path) {
        return newUnsignedBuilder(BaseUint16Type.INSTANCE, path);
    }

    public static DerivedTypeBuilder<UnsignedIntegerTypeDefinition> newUint32Builder(final SchemaPath path) {
        return newUnsignedBuilder(BaseUint32Type.INSTANCE, path);
    }

    public static DerivedTypeBuilder<UnsignedIntegerTypeDefinition> newUint64Builder(final SchemaPath path) {
        return newUnsignedBuilder(BaseUint64Type.INSTANCE, path);
    }

    public static DerivedTypeBuilder<UnsignedIntegerTypeDefinition> newUnsignedBuilder(final UnsignedIntegerTypeDefinition baseType, final SchemaPath path) {
        return new DerivedTypeBuilder<UnsignedIntegerTypeDefinition>(baseType, path) {
            @Override
            public UnsignedIntegerTypeDefinition build() {
                return new DerivedUnsignedType(getBaseType(), getPath(), getDefaultValue(), getDescription(), getReference(),
                    getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static DerivedTypeBuilder<BitsTypeDefinition> newBitsBuilder(final BitsTypeDefinition baseType, final SchemaPath path) {
        return new DerivedTypeBuilder<BitsTypeDefinition>(baseType, path) {
            @Override
            public BitsTypeDefinition build() {
                return new DerivedBitsType(getBaseType(), getPath(), getDefaultValue(), getDescription(), getReference(),
                    getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static DerivedTypeBuilder<DecimalTypeDefinition> newDecima64Builder(final DecimalTypeDefinition baseType, final SchemaPath path) {
        return new DerivedTypeBuilder<DecimalTypeDefinition>(baseType, path) {
            @Override
            public DecimalTypeDefinition build() {
                return new DerivedDecimalType(getBaseType(), getPath(), getDefaultValue(), getDescription(), getReference(),
                    getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static DerivedTypeBuilder<EnumTypeDefinition> newEnumerationBuilder(final EnumTypeDefinition baseType, final SchemaPath path) {
        return new DerivedTypeBuilder<EnumTypeDefinition>(baseType, path) {
            @Override
            public EnumTypeDefinition build() {
                return new DerivedEnumerationType(getBaseType(), getPath(), getDefaultValue(), getDescription(), getReference(),
                    getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static DerivedTypeBuilder<IdentityrefTypeDefinition> newIdentityrefBuilder(final IdentityrefTypeDefinition baseType, final SchemaPath path) {
        return new DerivedTypeBuilder<IdentityrefTypeDefinition>(baseType, path) {
            @Override
            public IdentityrefTypeDefinition build() {
                return new DerivedIdentityrefType(getBaseType(), getPath(), getDefaultValue(), getDescription(), getReference(),
                    getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static DerivedTypeBuilder<InstanceIdentifierTypeDefinition> newInstanceIdentifierBuilder(final InstanceIdentifierTypeDefinition baseType, final SchemaPath path) {
        return new DerivedTypeBuilder<InstanceIdentifierTypeDefinition>(baseType, path) {
            @Override
            public InstanceIdentifierTypeDefinition build() {
                return new DerivedInstanceIdentifierType(getBaseType(), getPath(), getDefaultValue(), getDescription(), getReference(),
                    getStatus(), getUnits(), getUnknownSchemaNodes(), baseType.requireInstance());
            }
        };
    }

    public static DerivedTypeBuilder<LeafrefTypeDefinition> newLeafrefBuilder(final LeafrefTypeDefinition baseType, final SchemaPath path) {
        return new DerivedTypeBuilder<LeafrefTypeDefinition>(baseType, path) {
            @Override
            public LeafrefTypeDefinition build() {
                return new DerivedLeafrefType(getBaseType(), getPath(), getDefaultValue(), getDescription(), getReference(),
                    getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static DerivedTypeBuilder<StringTypeDefinition> newStringBuilder(final StringTypeDefinition baseType, final SchemaPath path) {
        return new DerivedTypeBuilder<StringTypeDefinition>(baseType, path) {
            @Override
            public StringTypeDefinition build() {
                return new DerivedStringType(getBaseType(), getPath(), getDefaultValue(), getDescription(), getReference(),
                    getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static DerivedTypeBuilder<UnionTypeDefinition> newUnionBuilder(final UnionTypeDefinition baseType, final SchemaPath path) {
        return new DerivedTypeBuilder<UnionTypeDefinition>(baseType, path) {
            @Override
            public DerivedUnionType build() {
                return new DerivedUnionType(getBaseType(), getPath(), getDefaultValue(), getDescription(), getReference(),
                    getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }
}
