/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
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
        // Hidden on purpose
    }

    public static @NonNull DerivedTypeBuilder<?> derivedTypeBuilder(final @NonNull TypeDefinition<?> baseType) {
        if (baseType instanceof BinaryTypeDefinition) {
            return derivedBinaryBuilder((BinaryTypeDefinition) baseType);
        } else if (baseType instanceof BitsTypeDefinition) {
            return derivedBitsBuilder((BitsTypeDefinition) baseType);
        } else if (baseType instanceof BooleanTypeDefinition) {
            return derivedBooleanBuilder((BooleanTypeDefinition) baseType);
        } else if (baseType instanceof DecimalTypeDefinition) {
            return derivedDecimalBuilder((DecimalTypeDefinition) baseType);
        } else if (baseType instanceof EmptyTypeDefinition) {
            return derivedEmptyBuilder((EmptyTypeDefinition) baseType);
        } else if (baseType instanceof EnumTypeDefinition) {
            return derivedEnumerationBuilder((EnumTypeDefinition) baseType);
        } else if (baseType instanceof IdentityrefTypeDefinition) {
            return derivedIdentityrefBuilder((IdentityrefTypeDefinition) baseType);
        } else if (baseType instanceof InstanceIdentifierTypeDefinition) {
            return derivedInstanceIdentifierBuilder((InstanceIdentifierTypeDefinition) baseType);
        } else if (baseType instanceof Int8TypeDefinition) {
            return derivedInt8Builder((Int8TypeDefinition) baseType);
        } else if (baseType instanceof Int16TypeDefinition) {
            return derivedInt16Builder((Int16TypeDefinition) baseType);
        } else if (baseType instanceof Int32TypeDefinition) {
            return derivedInt32Builder((Int32TypeDefinition) baseType);
        } else if (baseType instanceof Int64TypeDefinition) {
            return derivedInt64Builder((Int64TypeDefinition) baseType);
        } else if (baseType instanceof LeafrefTypeDefinition) {
            return derivedLeafrefBuilder((LeafrefTypeDefinition) baseType);
        } else if (baseType instanceof StringTypeDefinition) {
            return derivedStringBuilder((StringTypeDefinition) baseType);
        } else if (baseType instanceof UnionTypeDefinition) {
            return derivedUnionBuilder((UnionTypeDefinition) baseType);
        } else if (baseType instanceof Uint8TypeDefinition) {
            return derivedUint8Builder((Uint8TypeDefinition) baseType);
        } else if (baseType instanceof Uint16TypeDefinition) {
            return derivedUint16Builder((Uint16TypeDefinition) baseType);
        } else if (baseType instanceof Uint32TypeDefinition) {
            return derivedUint32Builder((Uint32TypeDefinition) baseType);
        } else if (baseType instanceof Uint64TypeDefinition) {
            return derivedUint64Builder((Uint64TypeDefinition) baseType);
        } else {
            throw new IllegalArgumentException("Unhandled type definition class " + baseType.getClass());
        }
    }

    public static @NonNull DerivedTypeBuilder<BinaryTypeDefinition> derivedBinaryBuilder(
            final @NonNull BinaryTypeDefinition baseType) {
        return new DerivedTypeBuilder<BinaryTypeDefinition>(baseType) {
            @Override
            public BinaryTypeDefinition build() {
                return new DerivedBinaryType(getBaseType(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static @NonNull DerivedTypeBuilder<BitsTypeDefinition> derivedBitsBuilder(
            final BitsTypeDefinition baseType) {
        return new DerivedTypeBuilder<BitsTypeDefinition>(baseType) {
            @Override
            public BitsTypeDefinition build() {
                return new DerivedBitsType(getBaseType(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static @NonNull DerivedTypeBuilder<BooleanTypeDefinition> derivedBooleanBuilder(
            final @NonNull BooleanTypeDefinition baseType) {
        return new DerivedTypeBuilder<BooleanTypeDefinition>(baseType) {
            @Override
            public BooleanTypeDefinition build() {
                return new DerivedBooleanType(getBaseType(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static @NonNull DerivedTypeBuilder<DecimalTypeDefinition> derivedDecimalBuilder(
            final DecimalTypeDefinition baseType) {
        return new DerivedTypeBuilder<DecimalTypeDefinition>(baseType) {
            @Override
            public DecimalTypeDefinition build() {
                return new DerivedDecimalType(getBaseType(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static @NonNull DerivedTypeBuilder<EmptyTypeDefinition> derivedEmptyBuilder(
            final EmptyTypeDefinition baseType) {
        return new DerivedTypeBuilder<EmptyTypeDefinition>(baseType) {
            @Override
            public EmptyTypeDefinition build() {
                return new DerivedEmptyType(getBaseType(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static @NonNull DerivedTypeBuilder<EnumTypeDefinition> derivedEnumerationBuilder(
            final EnumTypeDefinition baseType) {
        return new DerivedTypeBuilder<EnumTypeDefinition>(baseType) {
            @Override
            public EnumTypeDefinition build() {
                return new DerivedEnumerationType(getBaseType(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static @NonNull DerivedTypeBuilder<IdentityrefTypeDefinition> derivedIdentityrefBuilder(
            final IdentityrefTypeDefinition baseType) {
        return new DerivedTypeBuilder<IdentityrefTypeDefinition>(baseType) {
            @Override
            public IdentityrefTypeDefinition build() {
                return new DerivedIdentityrefType(getBaseType(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static @NonNull DerivedTypeBuilder<InstanceIdentifierTypeDefinition> derivedInstanceIdentifierBuilder(
            final InstanceIdentifierTypeDefinition baseType) {
        return new DerivedTypeBuilder<InstanceIdentifierTypeDefinition>(baseType) {
            @Override
            public InstanceIdentifierTypeDefinition build() {
                return new DerivedInstanceIdentifierType(getBaseType(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes(), baseType.requireInstance());
            }
        };
    }

    private static @NonNull DerivedTypeBuilder<Int8TypeDefinition> derivedInt8Builder(
            final Int8TypeDefinition baseType) {
        return new DerivedTypeBuilder<Int8TypeDefinition>(baseType) {
            @Override
            public Int8TypeDefinition build() {
                return new DerivedInt8Type(getBaseType(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static @NonNull DerivedTypeBuilder<Int16TypeDefinition> derivedInt16Builder(
            final Int16TypeDefinition baseType) {
        return new DerivedTypeBuilder<Int16TypeDefinition>(baseType) {
            @Override
            public Int16TypeDefinition build() {
                return new DerivedInt16Type(getBaseType(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static @NonNull DerivedTypeBuilder<Int32TypeDefinition> derivedInt32Builder(
            final Int32TypeDefinition baseType) {
        return new DerivedTypeBuilder<Int32TypeDefinition>(baseType) {
            @Override
            public Int32TypeDefinition build() {
                return new DerivedInt32Type(getBaseType(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static @NonNull DerivedTypeBuilder<Int64TypeDefinition> derivedInt64Builder(
            final Int64TypeDefinition baseType) {
        return new DerivedTypeBuilder<Int64TypeDefinition>(baseType) {
            @Override
            public Int64TypeDefinition build() {
                return new DerivedInt64Type(getBaseType(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static @NonNull DerivedTypeBuilder<LeafrefTypeDefinition> derivedLeafrefBuilder(
            final LeafrefTypeDefinition baseType) {
        return new DerivedTypeBuilder<LeafrefTypeDefinition>(baseType) {
            @Override
            public LeafrefTypeDefinition build() {
                return new DerivedLeafrefType(getBaseType(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static @NonNull DerivedTypeBuilder<StringTypeDefinition> derivedStringBuilder(
            final StringTypeDefinition baseType) {
        return new DerivedTypeBuilder<StringTypeDefinition>(baseType) {
            @Override
            public StringTypeDefinition build() {
                return new DerivedStringType(getBaseType(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static @NonNull DerivedTypeBuilder<UnionTypeDefinition> derivedUnionBuilder(
            final UnionTypeDefinition baseType) {
        return new DerivedTypeBuilder<UnionTypeDefinition>(baseType) {
            @Override
            public DerivedUnionType build() {
                return new DerivedUnionType(getBaseType(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static @NonNull DerivedTypeBuilder<Uint8TypeDefinition> derivedUint8Builder(
            final Uint8TypeDefinition baseType) {
        return new DerivedTypeBuilder<Uint8TypeDefinition>(baseType) {
            @Override
            public Uint8TypeDefinition build() {
                return new DerivedUint8Type(getBaseType(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static @NonNull DerivedTypeBuilder<Uint16TypeDefinition> derivedUint16Builder(
            final Uint16TypeDefinition baseType) {
        return new DerivedTypeBuilder<Uint16TypeDefinition>(baseType) {
            @Override
            public Uint16TypeDefinition build() {
                return new DerivedUint16Type(getBaseType(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static @NonNull DerivedTypeBuilder<Uint32TypeDefinition> derivedUint32Builder(
            final Uint32TypeDefinition baseType) {
        return new DerivedTypeBuilder<Uint32TypeDefinition>(baseType) {
            @Override
            public Uint32TypeDefinition build() {
                return new DerivedUint32Type(getBaseType(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static @NonNull DerivedTypeBuilder<Uint64TypeDefinition> derivedUint64Builder(
            final Uint64TypeDefinition baseType) {
        return new DerivedTypeBuilder<Uint64TypeDefinition>(baseType) {
            @Override
            public Uint64TypeDefinition build() {
                return new DerivedUint64Type(getBaseType(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }
}
