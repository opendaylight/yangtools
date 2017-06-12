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
 * Support for creating {@link TypeDefinition} instances defined by leaf and leaf-list statements.
 */
@Beta
public final class ConcreteTypes {
    private ConcreteTypes() {
        throw new UnsupportedOperationException();
    }

    public static ConcreteTypeBuilder<?> concreteTypeBuilder(@Nonnull final TypeDefinition<?> baseType,
            @Nonnull final SchemaPath path) {
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
        } else if (baseType instanceof IntegerTypeDefinition) {
            return concreteIntegerBuilder((IntegerTypeDefinition) baseType, path);
        } else if (baseType instanceof LeafrefTypeDefinition) {
            return concreteLeafrefBuilder((LeafrefTypeDefinition) baseType, path);
        } else if (baseType instanceof StringTypeDefinition) {
            return concreteStringBuilder((StringTypeDefinition) baseType, path);
        } else if (baseType instanceof UnionTypeDefinition) {
            return concreteUnionBuilder((UnionTypeDefinition) baseType, path);
        } else if (baseType instanceof UnsignedIntegerTypeDefinition) {
            return concreteUnsignedBuilder((UnsignedIntegerTypeDefinition) baseType, path);
        } else {
            throw new IllegalArgumentException("Unhandled type definition class " + baseType.getClass());
        }
    }

    private static ConcreteTypeBuilder<BinaryTypeDefinition> concreteBinaryBuilder(
            @Nonnull final BinaryTypeDefinition baseType, @Nonnull final SchemaPath path) {
        return new ConcreteTypeBuilder<BinaryTypeDefinition>(baseType, path) {
            @Nonnull
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
            @Nonnull
            @Override
            public BitsTypeDefinition buildType() {
                return new DerivedBitsType(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<BooleanTypeDefinition> concreteBooleanBuilder(
            @Nonnull final BooleanTypeDefinition baseType, @Nonnull final SchemaPath path) {
        return new ConcreteTypeBuilder<BooleanTypeDefinition>(baseType, path) {
            @Nonnull
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
            @Nonnull
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
            @Nonnull
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
            @Nonnull
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
            @Nonnull
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
            @Nonnull
            @Override
            public InstanceIdentifierTypeDefinition buildType() {
                return new DerivedInstanceIdentifierType(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes(), baseType.requireInstance());
            }
        };
    }

    private static ConcreteTypeBuilder<IntegerTypeDefinition> concreteIntegerBuilder(
            final IntegerTypeDefinition baseType, final SchemaPath path) {
        return new ConcreteTypeBuilder<IntegerTypeDefinition>(baseType, path) {
            @Nonnull
            @Override
            public IntegerTypeDefinition buildType() {
                return new DerivedIntegerType(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<LeafrefTypeDefinition> concreteLeafrefBuilder(
            final LeafrefTypeDefinition baseType, final SchemaPath path) {
        return new ConcreteTypeBuilder<LeafrefTypeDefinition>(baseType, path) {
            @Nonnull
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
            @Nonnull
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
            @Nonnull
            @Override
            public DerivedUnionType buildType() {
                return new DerivedUnionType(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static ConcreteTypeBuilder<UnsignedIntegerTypeDefinition> concreteUnsignedBuilder(
            final UnsignedIntegerTypeDefinition baseType, final SchemaPath path) {
        return new ConcreteTypeBuilder<UnsignedIntegerTypeDefinition>(baseType, path) {
            @Nonnull
            @Override
            public UnsignedIntegerTypeDefinition buildType() {
                return new DerivedUnsignedType(getBaseType(), getPath(), getDefaultValue(), getDescription(),
                        getReference(), getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }
}
