/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
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
@NonNullByDefault
public final class DerivedTypes {
    private DerivedTypes() {
        throw new UnsupportedOperationException();
    }

    public static DerivedTypeBuilder<?> derivedTypeBuilder(final TypeDefinition<?> baseType, final SchemaPath path) {
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

    public static DerivedTypeBuilder<BinaryTypeDefinition> derivedBinaryBuilder(
            final BinaryTypeDefinition baseType, final SchemaPath path) {
        return new DerivedTypeBuilder<BinaryTypeDefinition>(baseType, path) {
            @Override
            public BinaryTypeDefinition build() {
                return new DerivedBinaryType(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static DerivedTypeBuilder<BitsTypeDefinition> derivedBitsBuilder(final BitsTypeDefinition baseType,
            final SchemaPath path) {
        return new DerivedTypeBuilder<BitsTypeDefinition>(baseType, path) {
            @Override
            public BitsTypeDefinition build() {
                return new DerivedBitsType(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static DerivedTypeBuilder<BooleanTypeDefinition> derivedBooleanBuilder(
            final BooleanTypeDefinition baseType, final SchemaPath path) {
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

    public static DerivedTypeBuilder<EmptyTypeDefinition> derivedEmptyBuilder(final EmptyTypeDefinition baseType,
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

    public static DerivedTypeBuilder<IdentityrefTypeDefinition> derivedIdentityrefBuilder(
            final IdentityrefTypeDefinition baseType, final SchemaPath path) {
        return new DerivedTypeBuilder<IdentityrefTypeDefinition>(baseType, path) {
            @Override
            public IdentityrefTypeDefinition build() {
                return new DerivedIdentityrefType(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static DerivedTypeBuilder<InstanceIdentifierTypeDefinition> derivedInstanceIdentifierBuilder(
            final InstanceIdentifierTypeDefinition baseType, final SchemaPath path) {
        return new DerivedTypeBuilder<InstanceIdentifierTypeDefinition>(baseType, path) {
            @Override
            public InstanceIdentifierTypeDefinition build() {
                return new DerivedInstanceIdentifierType(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes(), baseType.requireInstance());
            }
        };
    }

    private static DerivedTypeBuilder<Int8TypeDefinition> derivedInt8Builder(final Int8TypeDefinition baseType,
            final SchemaPath path) {
        return new DerivedTypeBuilder<Int8TypeDefinition>(baseType, path) {
            @Override
            public Int8TypeDefinition build() {
                return new DerivedInt8Type(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static DerivedTypeBuilder<Int16TypeDefinition> derivedInt16Builder(final Int16TypeDefinition baseType,
            final SchemaPath path) {
        return new DerivedTypeBuilder<Int16TypeDefinition>(baseType, path) {
            @Override
            public Int16TypeDefinition build() {
                return new DerivedInt16Type(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static DerivedTypeBuilder<Int32TypeDefinition> derivedInt32Builder(final Int32TypeDefinition baseType,
            final SchemaPath path) {
        return new DerivedTypeBuilder<Int32TypeDefinition>(baseType, path) {
            @Override
            public Int32TypeDefinition build() {
                return new DerivedInt32Type(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static DerivedTypeBuilder<Int64TypeDefinition> derivedInt64Builder(final Int64TypeDefinition baseType,
            final SchemaPath path) {
        return new DerivedTypeBuilder<Int64TypeDefinition>(baseType, path) {
            @Override
            public Int64TypeDefinition build() {
                return new DerivedInt64Type(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static DerivedTypeBuilder<LeafrefTypeDefinition> derivedLeafrefBuilder(final LeafrefTypeDefinition baseType,
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

    public static DerivedTypeBuilder<UnionTypeDefinition> derivedUnionBuilder(final UnionTypeDefinition baseType,
            final SchemaPath path) {
        return new DerivedTypeBuilder<UnionTypeDefinition>(baseType, path) {
            @Override
            public DerivedUnionType build() {
                return new DerivedUnionType(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static DerivedTypeBuilder<Uint8TypeDefinition> derivedUint8Builder(
            final Uint8TypeDefinition baseType, final SchemaPath path) {
        return new DerivedTypeBuilder<Uint8TypeDefinition>(baseType, path) {
            @Override
            public Uint8TypeDefinition build() {
                return new DerivedUint8Type(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static DerivedTypeBuilder<Uint16TypeDefinition> derivedUint16Builder(
            final Uint16TypeDefinition baseType, final SchemaPath path) {
        return new DerivedTypeBuilder<Uint16TypeDefinition>(baseType, path) {
            @Override
            public Uint16TypeDefinition build() {
                return new DerivedUint16Type(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static DerivedTypeBuilder<Uint32TypeDefinition> derivedUint32Builder(
            final Uint32TypeDefinition baseType, final SchemaPath path) {
        return new DerivedTypeBuilder<Uint32TypeDefinition>(baseType, path) {
            @Override
            public Uint32TypeDefinition build() {
                return new DerivedUint32Type(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static DerivedTypeBuilder<Uint64TypeDefinition> derivedUint64Builder(
            final Uint64TypeDefinition baseType, final SchemaPath path) {
        return new DerivedTypeBuilder<Uint64TypeDefinition>(baseType, path) {
            @Override
            public Uint64TypeDefinition build() {
                return new DerivedUint64Type(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }
}
