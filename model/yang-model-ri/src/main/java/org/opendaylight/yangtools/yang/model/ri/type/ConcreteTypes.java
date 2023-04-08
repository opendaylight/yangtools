/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.type;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnitsEffectiveStatement;
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

    public static ConcreteTypeBuilder<?> concreteTypeBuilder(final TypeDefinition<?> baseType, final QName qname) {
        if (baseType instanceof BinaryTypeDefinition binary) {
            return concreteBinaryBuilder(binary, qname);
        } else if (baseType instanceof BitsTypeDefinition bits) {
            return concreteBitsBuilder(bits, qname);
        } else if (baseType instanceof BooleanTypeDefinition bool) {
            return concreteBooleanBuilder(bool, qname);
        } else if (baseType instanceof DecimalTypeDefinition decimal) {
            return concreteDecimalBuilder(decimal, qname);
        } else if (baseType instanceof EmptyTypeDefinition empty) {
            return concreteEmptyBuilder(empty, qname);
        } else if (baseType instanceof EnumTypeDefinition enumType) {
            return concreteEnumerationBuilder(enumType, qname);
        } else if (baseType instanceof IdentityrefTypeDefinition identityRef) {
            return concreteIdentityrefBuilder(identityRef, qname);
        } else if (baseType instanceof InstanceIdentifierTypeDefinition instanceIdentifier) {
            return concreteInstanceIdentifierBuilder(instanceIdentifier, qname);
        } else if (baseType instanceof Int8TypeDefinition int8) {
            return concreteInt8Builder(int8, qname);
        } else if (baseType instanceof Int16TypeDefinition int16) {
            return concreteInt16Builder(int16, qname);
        } else if (baseType instanceof Int32TypeDefinition int32) {
            return concreteInt32Builder(int32, qname);
        } else if (baseType instanceof Int64TypeDefinition int64) {
            return concreteInt64Builder(int64, qname);
        } else if (baseType instanceof LeafrefTypeDefinition leafRef) {
            return concreteLeafrefBuilder(leafRef, qname);
        } else if (baseType instanceof StringTypeDefinition string) {
            return concreteStringBuilder(string, qname);
        } else if (baseType instanceof UnionTypeDefinition union) {
            return concreteUnionBuilder(union, qname);
        } else if (baseType instanceof Uint8TypeDefinition uint8) {
            return concreteUint8Builder(uint8, qname);
        } else if (baseType instanceof Uint16TypeDefinition uint16) {
            return concreteUint16Builder(uint16, qname);
        } else if (baseType instanceof Uint32TypeDefinition uint32) {
            return concreteUint32Builder(uint32, qname);
        } else if (baseType instanceof Uint64TypeDefinition uint64) {
            return concreteUint64Builder(uint64, qname);
        } else {
            throw new IllegalArgumentException("Unhandled type definition class " + baseType.getClass());
        }
    }

    public static TypeDefinition<?> typeOf(final LeafEffectiveStatement leaf) {
        final var typeStmt = leaf.findFirstEffectiveSubstatement(TypeEffectiveStatement.class).orElseThrow();
        final var builder = concreteTypeBuilder(typeStmt.getTypeDefinition(), leaf.argument());
        for (var stmt : leaf.effectiveSubstatements()) {
            if (stmt instanceof DefaultEffectiveStatement dflt) {
                builder.setDefaultValue(dflt.argument());
            } else if (stmt instanceof DescriptionEffectiveStatement description) {
                builder.setDescription(description.argument());
            } else if (stmt instanceof ReferenceEffectiveStatement reference) {
                builder.setReference(reference.argument());
            } else if (stmt instanceof StatusEffectiveStatement status) {
                builder.setStatus(status.argument());
            } else if (stmt instanceof UnitsEffectiveStatement units) {
                builder.setUnits(units.argument());
            }
        }
        return builder.build();
    }

    public static TypeDefinition<?> typeOf(final LeafListEffectiveStatement leafList) {
        final var typeStmt = leafList.findFirstEffectiveSubstatement(TypeEffectiveStatement.class).orElseThrow();
        final var builder = concreteTypeBuilder(typeStmt.getTypeDefinition(), leafList.argument());
        for (var stmt : leafList.effectiveSubstatements()) {
            // NOTE: 'default' is omitted here on purpose
            if (stmt instanceof DescriptionEffectiveStatement description) {
                builder.setDescription(description.argument());
            } else if (stmt instanceof ReferenceEffectiveStatement reference) {
                builder.setReference(reference.argument());
            } else if (stmt instanceof StatusEffectiveStatement status) {
                builder.setStatus(status.argument());
            } else if (stmt instanceof UnitsEffectiveStatement units) {
                builder.setUnits(units.argument());
            }
        }
        return builder.build();
    }

    private static ConcreteTypeBuilder<BinaryTypeDefinition> concreteBinaryBuilder(
            final BinaryTypeDefinition baseType, final QName qname) {
        return new ConcreteTypeBuilder<>(baseType, qname) {
            @Override
            public BinaryTypeDefinition buildType() {
                return new DerivedBinaryType(getBaseType(), getQName(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<BitsTypeDefinition> concreteBitsBuilder(final BitsTypeDefinition baseType,
            final QName qname) {
        return new ConcreteTypeBuilder<>(baseType, qname) {
            @Override
            public BitsTypeDefinition buildType() {
                return new DerivedBitsType(getBaseType(), getQName(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<BooleanTypeDefinition> concreteBooleanBuilder(
            final BooleanTypeDefinition baseType, final QName qname) {
        return new ConcreteTypeBuilder<>(baseType, qname) {
            @Override
            public BooleanTypeDefinition buildType() {
                return new DerivedBooleanType(getBaseType(), getQName(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<DecimalTypeDefinition> concreteDecimalBuilder(
            final DecimalTypeDefinition baseType, final QName qname) {
        return new ConcreteTypeBuilder<>(baseType, qname) {
            @Override
            public DecimalTypeDefinition buildType() {
                return new DerivedDecimalType(getBaseType(), getQName(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<EmptyTypeDefinition> concreteEmptyBuilder(final EmptyTypeDefinition baseType,
            final QName qname) {
        return new ConcreteTypeBuilder<>(baseType, qname) {
            @Override
            public EmptyTypeDefinition buildType() {
                return new DerivedEmptyType(getBaseType(), getQName(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<EnumTypeDefinition> concreteEnumerationBuilder(
            final EnumTypeDefinition baseType, final QName qname) {
        return new ConcreteTypeBuilder<>(baseType, qname) {
            @Override
            public EnumTypeDefinition buildType() {
                return new DerivedEnumerationType(getBaseType(), getQName(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<IdentityrefTypeDefinition> concreteIdentityrefBuilder(
            final IdentityrefTypeDefinition baseType, final QName qname) {
        return new ConcreteTypeBuilder<>(baseType, qname) {
            @Override
            public IdentityrefTypeDefinition buildType() {
                return new DerivedIdentityrefType(getBaseType(), getQName(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<InstanceIdentifierTypeDefinition> concreteInstanceIdentifierBuilder(
            final InstanceIdentifierTypeDefinition baseType, final QName qname) {
        return new ConcreteTypeBuilder<>(baseType, qname) {
            @Override
            public InstanceIdentifierTypeDefinition buildType() {
                return new DerivedInstanceIdentifierType(getBaseType(), getQName(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes(), baseType.requireInstance());
            }
        };
    }

    private static ConcreteTypeBuilder<Int8TypeDefinition> concreteInt8Builder(
            final Int8TypeDefinition baseType, final QName qname) {
        return new ConcreteTypeBuilder<>(baseType, qname) {
            @Override
            public Int8TypeDefinition buildType() {
                return new DerivedInt8Type(getBaseType(), getQName(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<Int16TypeDefinition> concreteInt16Builder(
            final Int16TypeDefinition baseType, final QName qname) {
        return new ConcreteTypeBuilder<>(baseType, qname) {
            @Override
            public Int16TypeDefinition buildType() {
                return new DerivedInt16Type(getBaseType(), getQName(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<Int32TypeDefinition> concreteInt32Builder(
            final Int32TypeDefinition baseType, final QName qname) {
        return new ConcreteTypeBuilder<>(baseType, qname) {
            @Override
            public Int32TypeDefinition buildType() {
                return new DerivedInt32Type(getBaseType(), getQName(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<Int64TypeDefinition> concreteInt64Builder(
            final Int64TypeDefinition baseType, final QName qname) {
        return new ConcreteTypeBuilder<>(baseType, qname) {
            @Override
            public Int64TypeDefinition buildType() {
                return new DerivedInt64Type(getBaseType(), getQName(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<LeafrefTypeDefinition> concreteLeafrefBuilder(
            final LeafrefTypeDefinition baseType, final QName qname) {
        return new ConcreteTypeBuilder<>(baseType, qname) {
            @Override
            public LeafrefTypeDefinition buildType() {
                return new DerivedLeafrefType(getBaseType(), getQName(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<StringTypeDefinition> concreteStringBuilder(final StringTypeDefinition baseType,
            final QName qname) {
        return new ConcreteTypeBuilder<>(baseType, qname) {
            @Override
            public StringTypeDefinition buildType() {
                return new DerivedStringType(getBaseType(), getQName(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<UnionTypeDefinition> concreteUnionBuilder(final UnionTypeDefinition baseType,
            final QName qname) {
        return new ConcreteTypeBuilder<>(baseType, qname) {
            @Override
            public DerivedUnionType buildType() {
                return new DerivedUnionType(getBaseType(), getQName(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<Uint8TypeDefinition> concreteUint8Builder(
            final Uint8TypeDefinition baseType, final QName qname) {
        return new ConcreteTypeBuilder<>(baseType, qname) {
            @Override
            public Uint8TypeDefinition buildType() {
                return new DerivedUint8Type(getBaseType(), getQName(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<Uint16TypeDefinition> concreteUint16Builder(
            final Uint16TypeDefinition baseType, final QName qname) {
        return new ConcreteTypeBuilder<>(baseType, qname) {
            @Override
            public Uint16TypeDefinition buildType() {
                return new DerivedUint16Type(getBaseType(), getQName(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<Uint32TypeDefinition> concreteUint32Builder(
            final Uint32TypeDefinition baseType, final QName qname) {
        return new ConcreteTypeBuilder<>(baseType, qname) {
            @Override
            public Uint32TypeDefinition buildType() {
                return new DerivedUint32Type(getBaseType(), getQName(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<Uint64TypeDefinition> concreteUint64Builder(
            final Uint64TypeDefinition baseType, final QName qname) {
        return new ConcreteTypeBuilder<>(baseType, qname) {
            @Override
            public Uint64TypeDefinition buildType() {
                return new DerivedUint64Type(getBaseType(), getQName(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }
}
