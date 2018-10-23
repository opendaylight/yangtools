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
 * Support for creating {@link TypeDefinition} instances defined by leaf and leaf-list statements.
 */
@Beta
@NonNullByDefault
public final class ConcreteTypes {
    private ConcreteTypes() {
        throw new UnsupportedOperationException();
    }

    public static ConcreteTypeBuilder<?> concreteTypeBuilder(final TypeDefinition<?> baseType, final SchemaPath path) {
        if (baseType instanceof BinaryTypeDefinition) {
            return concreteBinaryBuilder((BinaryTypeDefinition) baseType, path);
        } else if (baseType instanceof BitsTypeDefinition) {
            return concreteBitsBuilder((BitsTypeDefinition) baseType, path);
        } else if (baseType instanceof BooleanTypeDefinition) {
            return concreteBooleanBuilder((BooleanTypeDefinition) baseType, path);
        } else if (baseType instanceof DecimalTypeDefinition) {
            return concreteDecimalBuilder((DecimalTypeDefinition) baseType, path);
        } else if (baseType instanceof EmptyTypeDefinition) {
            return concreteEmptyBuilder((EmptyTypeDefinition) baseType, path);
        } else if (baseType instanceof EnumTypeDefinition) {
            return concreteEnumerationBuilder((EnumTypeDefinition) baseType, path);
        } else if (baseType instanceof IdentityrefTypeDefinition) {
            return concreteIdentityrefBuilder((IdentityrefTypeDefinition) baseType, path);
        } else if (baseType instanceof InstanceIdentifierTypeDefinition) {
            return concreteInstanceIdentifierBuilder((InstanceIdentifierTypeDefinition) baseType, path);
        } else if (baseType instanceof Int8TypeDefinition) {
            return concreteInt8Builder((Int8TypeDefinition) baseType, path);
        } else if (baseType instanceof Int16TypeDefinition) {
            return concreteInt16Builder((Int16TypeDefinition) baseType, path);
        } else if (baseType instanceof Int32TypeDefinition) {
            return concreteInt32Builder((Int32TypeDefinition) baseType, path);
        } else if (baseType instanceof Int64TypeDefinition) {
            return concreteInt64Builder((Int64TypeDefinition) baseType, path);
        } else if (baseType instanceof LeafrefTypeDefinition) {
            return concreteLeafrefBuilder((LeafrefTypeDefinition) baseType, path);
        } else if (baseType instanceof StringTypeDefinition) {
            return concreteStringBuilder((StringTypeDefinition) baseType, path);
        } else if (baseType instanceof UnionTypeDefinition) {
            return concreteUnionBuilder((UnionTypeDefinition) baseType, path);
        } else if (baseType instanceof Uint8TypeDefinition) {
            return concreteUint8Builder((Uint8TypeDefinition) baseType, path);
        } else if (baseType instanceof Uint16TypeDefinition) {
            return concreteUint16Builder((Uint16TypeDefinition) baseType, path);
        } else if (baseType instanceof Uint32TypeDefinition) {
            return concreteUint32Builder((Uint32TypeDefinition) baseType, path);
        } else if (baseType instanceof Uint64TypeDefinition) {
            return concreteUint64Builder((Uint64TypeDefinition) baseType, path);
        } else {
            throw new IllegalArgumentException("Unhandled type definition class " + baseType.getClass());
        }
    }

    private static ConcreteTypeBuilder<BinaryTypeDefinition> concreteBinaryBuilder(
            final BinaryTypeDefinition baseType, final SchemaPath path) {
        return new ConcreteTypeBuilder<BinaryTypeDefinition>(baseType, path) {
            @Override
            public BinaryTypeDefinition buildType() {
                return new DerivedBinaryType(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<BitsTypeDefinition> concreteBitsBuilder(final BitsTypeDefinition baseType,
            final SchemaPath path) {
        return new ConcreteTypeBuilder<BitsTypeDefinition>(baseType, path) {
            @Override
            public BitsTypeDefinition buildType() {
                return new DerivedBitsType(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<BooleanTypeDefinition> concreteBooleanBuilder(
            final BooleanTypeDefinition baseType, final SchemaPath path) {
        return new ConcreteTypeBuilder<BooleanTypeDefinition>(baseType, path) {
            @Override
            public BooleanTypeDefinition buildType() {
                return new DerivedBooleanType(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<DecimalTypeDefinition> concreteDecimalBuilder(
            final DecimalTypeDefinition baseType, final SchemaPath path) {
        return new ConcreteTypeBuilder<DecimalTypeDefinition>(baseType, path) {
            @Override
            public DecimalTypeDefinition buildType() {
                return new DerivedDecimalType(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<EmptyTypeDefinition> concreteEmptyBuilder(final EmptyTypeDefinition baseType,
            final SchemaPath path) {
        return new ConcreteTypeBuilder<EmptyTypeDefinition>(baseType, path) {
            @Override
            public EmptyTypeDefinition buildType() {
                return new DerivedEmptyType(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<EnumTypeDefinition> concreteEnumerationBuilder(
            final EnumTypeDefinition baseType, final SchemaPath path) {
        return new ConcreteTypeBuilder<EnumTypeDefinition>(baseType, path) {
            @Override
            public EnumTypeDefinition buildType() {
                return new DerivedEnumerationType(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<IdentityrefTypeDefinition> concreteIdentityrefBuilder(
            final IdentityrefTypeDefinition baseType, final SchemaPath path) {
        return new ConcreteTypeBuilder<IdentityrefTypeDefinition>(baseType, path) {
            @Override
            public IdentityrefTypeDefinition buildType() {
                return new DerivedIdentityrefType(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<InstanceIdentifierTypeDefinition> concreteInstanceIdentifierBuilder(
            final InstanceIdentifierTypeDefinition baseType, final SchemaPath path) {
        return new ConcreteTypeBuilder<InstanceIdentifierTypeDefinition>(baseType, path) {
            @Override
            public InstanceIdentifierTypeDefinition buildType() {
                return new DerivedInstanceIdentifierType(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes(), baseType.requireInstance());
            }
        };
    }

    private static ConcreteTypeBuilder<Int8TypeDefinition> concreteInt8Builder(
            final Int8TypeDefinition baseType, final SchemaPath path) {
        return new ConcreteTypeBuilder<Int8TypeDefinition>(baseType, path) {
            @Override
            public Int8TypeDefinition buildType() {
                return new DerivedInt8Type(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<Int16TypeDefinition> concreteInt16Builder(
            final Int16TypeDefinition baseType, final SchemaPath path) {
        return new ConcreteTypeBuilder<Int16TypeDefinition>(baseType, path) {
            @Override
            public Int16TypeDefinition buildType() {
                return new DerivedInt16Type(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<Int32TypeDefinition> concreteInt32Builder(
            final Int32TypeDefinition baseType, final SchemaPath path) {
        return new ConcreteTypeBuilder<Int32TypeDefinition>(baseType, path) {
            @Override
            public Int32TypeDefinition buildType() {
                return new DerivedInt32Type(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<Int64TypeDefinition> concreteInt64Builder(
            final Int64TypeDefinition baseType, final SchemaPath path) {
        return new ConcreteTypeBuilder<Int64TypeDefinition>(baseType, path) {
            @Override
            public Int64TypeDefinition buildType() {
                return new DerivedInt64Type(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<LeafrefTypeDefinition> concreteLeafrefBuilder(
            final LeafrefTypeDefinition baseType, final SchemaPath path) {
        return new ConcreteTypeBuilder<LeafrefTypeDefinition>(baseType, path) {
            @Override
            public LeafrefTypeDefinition buildType() {
                return new DerivedLeafrefType(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<StringTypeDefinition> concreteStringBuilder(final StringTypeDefinition baseType,
            final SchemaPath path) {
        return new ConcreteTypeBuilder<StringTypeDefinition>(baseType, path) {
            @Override
            public StringTypeDefinition buildType() {
                return new DerivedStringType(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<UnionTypeDefinition> concreteUnionBuilder(final UnionTypeDefinition baseType,
            final SchemaPath path) {
        return new ConcreteTypeBuilder<UnionTypeDefinition>(baseType, path) {
            @Override
            public DerivedUnionType buildType() {
                return new DerivedUnionType(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<Uint8TypeDefinition> concreteUint8Builder(
            final Uint8TypeDefinition baseType, final SchemaPath path) {
        return new ConcreteTypeBuilder<Uint8TypeDefinition>(baseType, path) {
            @Override
            public Uint8TypeDefinition buildType() {
                return new DerivedUint8Type(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<Uint16TypeDefinition> concreteUint16Builder(
            final Uint16TypeDefinition baseType, final SchemaPath path) {
        return new ConcreteTypeBuilder<Uint16TypeDefinition>(baseType, path) {
            @Override
            public Uint16TypeDefinition buildType() {
                return new DerivedUint16Type(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<Uint32TypeDefinition> concreteUint32Builder(
            final Uint32TypeDefinition baseType, final SchemaPath path) {
        return new ConcreteTypeBuilder<Uint32TypeDefinition>(baseType, path) {
            @Override
            public Uint32TypeDefinition buildType() {
                return new DerivedUint32Type(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<Uint64TypeDefinition> concreteUint64Builder(
            final Uint64TypeDefinition baseType, final SchemaPath path) {
        return new ConcreteTypeBuilder<Uint64TypeDefinition>(baseType, path) {
            @Override
            public Uint64TypeDefinition buildType() {
                return new DerivedUint64Type(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }
}
