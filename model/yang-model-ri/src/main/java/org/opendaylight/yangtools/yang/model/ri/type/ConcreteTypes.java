/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.type;

import com.google.common.annotations.Beta;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
        return typeBuilderOf(baseType, qname);
    }

    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "Ungrokked pattern match cast")
    private static ConcreteTypeBuilder<?> typeBuilderOf(final TypeDefinition<?> baseType, final QName qname) {
        return switch (baseType) {
            case BinaryTypeDefinition binary -> concreteBinaryBuilder(binary, qname);
            case BitsTypeDefinition bits -> concreteBitsBuilder(bits, qname);
            case BooleanTypeDefinition bool -> concreteBooleanBuilder(bool, qname);
            case DecimalTypeDefinition decimal -> concreteDecimalBuilder(decimal, qname);
            case EmptyTypeDefinition empty -> concreteEmptyBuilder(empty, qname);
            case EnumTypeDefinition enumType -> concreteEnumerationBuilder(enumType, qname);
            case IdentityrefTypeDefinition identityRef -> concreteIdentityrefBuilder(identityRef, qname);
            case InstanceIdentifierTypeDefinition iid -> concreteInstanceIdentifierBuilder(iid, qname);
            case Int8TypeDefinition int8 -> concreteInt8Builder(int8, qname);
            case Int16TypeDefinition int16 -> concreteInt16Builder(int16, qname);
            case Int32TypeDefinition int32 -> concreteInt32Builder(int32, qname);
            case Int64TypeDefinition int64 -> concreteInt64Builder(int64, qname);
            case LeafrefTypeDefinition leafRef -> concreteLeafrefBuilder(leafRef, qname);
            case StringTypeDefinition string -> concreteStringBuilder(string, qname);
            case UnionTypeDefinition union -> concreteUnionBuilder(union, qname);
            case Uint8TypeDefinition uint8 -> concreteUint8Builder(uint8, qname);
            case Uint16TypeDefinition uint16 -> concreteUint16Builder(uint16, qname);
            case Uint32TypeDefinition uint32 -> concreteUint32Builder(uint32, qname);
            case Uint64TypeDefinition uint64 -> concreteUint64Builder(uint64, qname);
            default -> throw new IllegalArgumentException("Unhandled type definition class " + baseType.getClass());
        };
    }

    public static TypeDefinition<?> typeOf(final LeafEffectiveStatement leaf) {
        final var typeStmt = leaf.findFirstEffectiveSubstatement(TypeEffectiveStatement.class).orElseThrow();
        final var builder = concreteTypeBuilder(typeStmt.getTypeDefinition(), leaf.argument());
        leaf.effectiveSubstatements().forEach(stmt -> {
            switch (stmt) {
                case DefaultEffectiveStatement dflt -> builder.setDefaultValue(dflt.argument());
                case DescriptionEffectiveStatement description -> builder.setDescription(description.argument());
                case ReferenceEffectiveStatement reference -> builder.setReference(reference.argument());
                case StatusEffectiveStatement status -> builder.setStatus(status.argument());
                case UnitsEffectiveStatement units -> builder.setUnits(units.argument());
                default -> {
                    // No-op
                }
            }
        });
        return builder.build();
    }

    public static TypeDefinition<?> typeOf(final LeafListEffectiveStatement leafList) {
        final var typeStmt = leafList.findFirstEffectiveSubstatement(TypeEffectiveStatement.class).orElseThrow();
        final var builder = concreteTypeBuilder(typeStmt.getTypeDefinition(), leafList.argument());
        leafList.effectiveSubstatements().forEach(stmt -> {
            // NOTE: 'default' is omitted here on purpose
            switch (stmt) {
                case DescriptionEffectiveStatement description -> builder.setDescription(description.argument());
                case ReferenceEffectiveStatement reference -> builder.setReference(reference.argument());
                case StatusEffectiveStatement status -> builder.setStatus(status.argument());
                case UnitsEffectiveStatement units -> builder.setUnits(units.argument());
                default -> {
                    // No-op
                }
            }
        });
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
