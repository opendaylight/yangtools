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
        // Hidden on purpose
    }

    public static ConcreteTypeBuilder<?> concreteTypeBuilder(final TypeDefinition<?> baseType) {
        if (baseType instanceof BinaryTypeDefinition) {
            return concreteBinaryBuilder((BinaryTypeDefinition) baseType);
        } else if (baseType instanceof BitsTypeDefinition) {
            return concreteBitsBuilder((BitsTypeDefinition) baseType);
        } else if (baseType instanceof BooleanTypeDefinition) {
            return concreteBooleanBuilder((BooleanTypeDefinition) baseType);
        } else if (baseType instanceof DecimalTypeDefinition) {
            return concreteDecimalBuilder((DecimalTypeDefinition) baseType);
        } else if (baseType instanceof EmptyTypeDefinition) {
            return concreteEmptyBuilder((EmptyTypeDefinition) baseType);
        } else if (baseType instanceof EnumTypeDefinition) {
            return concreteEnumerationBuilder((EnumTypeDefinition) baseType);
        } else if (baseType instanceof IdentityrefTypeDefinition) {
            return concreteIdentityrefBuilder((IdentityrefTypeDefinition) baseType);
        } else if (baseType instanceof InstanceIdentifierTypeDefinition) {
            return concreteInstanceIdentifierBuilder((InstanceIdentifierTypeDefinition) baseType);
        } else if (baseType instanceof Int8TypeDefinition) {
            return concreteInt8Builder((Int8TypeDefinition) baseType);
        } else if (baseType instanceof Int16TypeDefinition) {
            return concreteInt16Builder((Int16TypeDefinition) baseType);
        } else if (baseType instanceof Int32TypeDefinition) {
            return concreteInt32Builder((Int32TypeDefinition) baseType);
        } else if (baseType instanceof Int64TypeDefinition) {
            return concreteInt64Builder((Int64TypeDefinition) baseType);
        } else if (baseType instanceof LeafrefTypeDefinition) {
            return concreteLeafrefBuilder((LeafrefTypeDefinition) baseType);
        } else if (baseType instanceof StringTypeDefinition) {
            return concreteStringBuilder((StringTypeDefinition) baseType);
        } else if (baseType instanceof UnionTypeDefinition) {
            return concreteUnionBuilder((UnionTypeDefinition) baseType);
        } else if (baseType instanceof Uint8TypeDefinition) {
            return concreteUint8Builder((Uint8TypeDefinition) baseType);
        } else if (baseType instanceof Uint16TypeDefinition) {
            return concreteUint16Builder((Uint16TypeDefinition) baseType);
        } else if (baseType instanceof Uint32TypeDefinition) {
            return concreteUint32Builder((Uint32TypeDefinition) baseType);
        } else if (baseType instanceof Uint64TypeDefinition) {
            return concreteUint64Builder((Uint64TypeDefinition) baseType);
        } else {
            throw new IllegalArgumentException("Unhandled type definition class " + baseType.getClass());
        }
    }

    private static ConcreteTypeBuilder<BinaryTypeDefinition> concreteBinaryBuilder(
            final BinaryTypeDefinition baseType) {
        return new ConcreteTypeBuilder<>(baseType) {
            @Override
            public BinaryTypeDefinition buildType() {
                return new DerivedBinaryType(getBaseType(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<BitsTypeDefinition> concreteBitsBuilder(final BitsTypeDefinition baseType) {
        return new ConcreteTypeBuilder<>(baseType) {
            @Override
            public BitsTypeDefinition buildType() {
                return new DerivedBitsType(getBaseType(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<BooleanTypeDefinition> concreteBooleanBuilder(
            final BooleanTypeDefinition baseType) {
        return new ConcreteTypeBuilder<>(baseType) {
            @Override
            public BooleanTypeDefinition buildType() {
                return new DerivedBooleanType(getBaseType(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<DecimalTypeDefinition> concreteDecimalBuilder(
            final DecimalTypeDefinition baseType) {
        return new ConcreteTypeBuilder<>(baseType) {
            @Override
            public DecimalTypeDefinition buildType() {
                return new DerivedDecimalType(getBaseType(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<EmptyTypeDefinition> concreteEmptyBuilder(final EmptyTypeDefinition baseType) {
        return new ConcreteTypeBuilder<>(baseType) {
            @Override
            public EmptyTypeDefinition buildType() {
                return new DerivedEmptyType(getBaseType(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<EnumTypeDefinition> concreteEnumerationBuilder(
            final EnumTypeDefinition baseType) {
        return new ConcreteTypeBuilder<>(baseType) {
            @Override
            public EnumTypeDefinition buildType() {
                return new DerivedEnumerationType(getBaseType(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<IdentityrefTypeDefinition> concreteIdentityrefBuilder(
            final IdentityrefTypeDefinition baseType) {
        return new ConcreteTypeBuilder<>(baseType) {
            @Override
            public IdentityrefTypeDefinition buildType() {
                return new DerivedIdentityrefType(getBaseType(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<InstanceIdentifierTypeDefinition> concreteInstanceIdentifierBuilder(
            final InstanceIdentifierTypeDefinition baseType) {
        return new ConcreteTypeBuilder<>(baseType) {
            @Override
            public InstanceIdentifierTypeDefinition buildType() {
                return new DerivedInstanceIdentifierType(getBaseType(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes(), baseType.requireInstance());
            }
        };
    }

    private static ConcreteTypeBuilder<Int8TypeDefinition> concreteInt8Builder(
            final Int8TypeDefinition baseType) {
        return new ConcreteTypeBuilder<>(baseType) {
            @Override
            public Int8TypeDefinition buildType() {
                return new DerivedInt8Type(getBaseType(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<Int16TypeDefinition> concreteInt16Builder(
            final Int16TypeDefinition baseType) {
        return new ConcreteTypeBuilder<>(baseType) {
            @Override
            public Int16TypeDefinition buildType() {
                return new DerivedInt16Type(getBaseType(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<Int32TypeDefinition> concreteInt32Builder(
            final Int32TypeDefinition baseType) {
        return new ConcreteTypeBuilder<>(baseType) {
            @Override
            public Int32TypeDefinition buildType() {
                return new DerivedInt32Type(getBaseType(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<Int64TypeDefinition> concreteInt64Builder(
            final Int64TypeDefinition baseType) {
        return new ConcreteTypeBuilder<>(baseType) {
            @Override
            public Int64TypeDefinition buildType() {
                return new DerivedInt64Type(getBaseType(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<LeafrefTypeDefinition> concreteLeafrefBuilder(
            final LeafrefTypeDefinition baseType) {
        return new ConcreteTypeBuilder<>(baseType) {
            @Override
            public LeafrefTypeDefinition buildType() {
                return new DerivedLeafrefType(getBaseType(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<StringTypeDefinition> concreteStringBuilder(
            final StringTypeDefinition baseType) {
        return new ConcreteTypeBuilder<>(baseType) {
            @Override
            public StringTypeDefinition buildType() {
                return new DerivedStringType(getBaseType(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<UnionTypeDefinition> concreteUnionBuilder(final UnionTypeDefinition baseType) {
        return new ConcreteTypeBuilder<>(baseType) {
            @Override
            public DerivedUnionType buildType() {
                return new DerivedUnionType(getBaseType(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<Uint8TypeDefinition> concreteUint8Builder(
            final Uint8TypeDefinition baseType) {
        return new ConcreteTypeBuilder<>(baseType) {
            @Override
            public Uint8TypeDefinition buildType() {
                return new DerivedUint8Type(getBaseType(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<Uint16TypeDefinition> concreteUint16Builder(
            final Uint16TypeDefinition baseType) {
        return new ConcreteTypeBuilder<>(baseType) {
            @Override
            public Uint16TypeDefinition buildType() {
                return new DerivedUint16Type(getBaseType(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<Uint32TypeDefinition> concreteUint32Builder(
            final Uint32TypeDefinition baseType) {
        return new ConcreteTypeBuilder<>(baseType) {
            @Override
            public Uint32TypeDefinition buildType() {
                return new DerivedUint32Type(getBaseType(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<Uint64TypeDefinition> concreteUint64Builder(
            final Uint64TypeDefinition baseType) {
        return new ConcreteTypeBuilder<>(baseType) {
            @Override
            public Uint64TypeDefinition buildType() {
                return new DerivedUint64Type(getBaseType(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }
}
