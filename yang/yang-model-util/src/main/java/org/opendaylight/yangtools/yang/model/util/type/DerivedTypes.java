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
 * A derived type can redefine the default value, description, status and reference of a particular type definition.
 * It can only refine the units attribute, as that attribute is tied to the semantics of the value. The default value,
 * and units attributes are inherited from the super (base or restricted) type, others are left undefined if not
 * explicitly set. Status defaults to current.
 */
/*
 * FIXME: Create ConcreteTypes
 *        Leaf and leaf-list statements provide for a similar mechanism by which a particular type is changed, most
 *        notably with the ability to redefine the default type. The resulting types could conceivably be called
 *        'concrete types', as they cannot be referenced by another leaf or type definition. This aspect needs to be
 *        split out into a 'ConcreteTypes' class.
 *
 *        Builders should use the fly-weight pattern to minimize footprint for cases when leaves do not override any
 *        aspect of the base type.
 */
@Beta
public final class DerivedTypes {
    private DerivedTypes() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    public static <T extends TypeDefinition<T>> DerivedTypeBuilder<T> derivedTypeBuilder(@Nonnull final T baseType,
            @Nonnull final SchemaPath path) {
        if (baseType instanceof BinaryTypeDefinition) {
            return (DerivedTypeBuilder<T>) derivedBinaryBuilder((BinaryTypeDefinition) baseType, path);
        } else if (baseType instanceof BitsTypeDefinition) {
            return (DerivedTypeBuilder<T>) derivedBitsBuilder((BitsTypeDefinition) baseType, path);
        } else if (baseType instanceof BooleanTypeDefinition) {
            return (DerivedTypeBuilder<T>) derivedBooleanBuilder((BooleanTypeDefinition) baseType, path);
        } else if (baseType instanceof DecimalTypeDefinition) {
            return (DerivedTypeBuilder<T>) derivedDecimalBuilder((DecimalTypeDefinition) baseType, path);
        } else if (baseType instanceof EmptyTypeDefinition) {
            return (DerivedTypeBuilder<T>) derivedEmptyBuilder((EmptyTypeDefinition) baseType, path);
        } else if (baseType instanceof EnumTypeDefinition) {
            return (DerivedTypeBuilder<T>) derivedEnumerationBuilder((EnumTypeDefinition) baseType, path);
        } else if (baseType instanceof IdentityrefTypeDefinition) {
            return (DerivedTypeBuilder<T>) derivedIdentityrefBuilder((IdentityrefTypeDefinition) baseType, path);
        } else if (baseType instanceof InstanceIdentifierTypeDefinition) {
            return (DerivedTypeBuilder<T>) derivedInstanceIdentifierBuilder((InstanceIdentifierTypeDefinition) baseType, path);
        } else if (baseType instanceof IntegerTypeDefinition) {
            return (DerivedTypeBuilder<T>) derivedIntegerBuilder((IntegerTypeDefinition) baseType, path);
        } else if (baseType instanceof LeafrefTypeDefinition) {
            return (DerivedTypeBuilder<T>) derivedLeafrefBuilder((LeafrefTypeDefinition) baseType, path);
        } else if (baseType instanceof StringTypeDefinition) {
            return (DerivedTypeBuilder<T>) derivedStringBuilder((StringTypeDefinition) baseType, path);
        } else if (baseType instanceof UnionTypeDefinition) {
            return (DerivedTypeBuilder<T>) derivedUnionBuilder((UnionTypeDefinition) baseType, path);
        } else if (baseType instanceof UnsignedIntegerTypeDefinition) {
            return (DerivedTypeBuilder<T>) derivedUnsignedBuilder((UnsignedIntegerTypeDefinition) baseType, path);
        } else {
            throw new IllegalArgumentException("Unhandled type definition class " + baseType.getClass());
        }
    }

    public static DerivedTypeBuilder<BinaryTypeDefinition> derivedBinaryBuilder(@Nonnull final BinaryTypeDefinition baseType, @Nonnull final SchemaPath path) {
        return new DerivedTypeBuilder<BinaryTypeDefinition>(baseType, path) {
            @Override
            public BinaryTypeDefinition build() {
                return new DerivedBinaryType(getBaseType(), getPath(), getDefaultValue(), getDescription(), getReference(),
                    getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static DerivedTypeBuilder<BitsTypeDefinition> derivedBitsBuilder(final BitsTypeDefinition baseType, final SchemaPath path) {
        return new DerivedTypeBuilder<BitsTypeDefinition>(baseType, path) {
            @Override
            public BitsTypeDefinition build() {
                return new DerivedBitsType(getBaseType(), getPath(), getDefaultValue(), getDescription(), getReference(),
                    getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static DerivedTypeBuilder<BooleanTypeDefinition> derivedBooleanBuilder(@Nonnull final BooleanTypeDefinition baseType, @Nonnull final SchemaPath path) {
        return new DerivedTypeBuilder<BooleanTypeDefinition>(baseType, path) {
            @Override
            public BooleanTypeDefinition build() {
                return new DerivedBooleanType(getBaseType(), getPath(), getDefaultValue(), getDescription(), getReference(),
                    getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static DerivedTypeBuilder<DecimalTypeDefinition> derivedDecimalBuilder(final DecimalTypeDefinition baseType, final SchemaPath path) {
        return new DerivedTypeBuilder<DecimalTypeDefinition>(baseType, path) {
            @Override
            public DecimalTypeDefinition build() {
                return new DerivedDecimalType(getBaseType(), getPath(), getDefaultValue(), getDescription(), getReference(),
                    getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static DerivedTypeBuilder<EmptyTypeDefinition> derivedEmptyBuilder(final EmptyTypeDefinition baseType, final SchemaPath path) {
        return new DerivedTypeBuilder<EmptyTypeDefinition>(baseType, path) {
            @Override
            public EmptyTypeDefinition build() {
                return new DerivedEmptyType(getBaseType(), getPath(), getDefaultValue(), getDescription(), getReference(),
                    getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static DerivedTypeBuilder<EnumTypeDefinition> derivedEnumerationBuilder(final EnumTypeDefinition baseType, final SchemaPath path) {
        return new DerivedTypeBuilder<EnumTypeDefinition>(baseType, path) {
            @Override
            public EnumTypeDefinition build() {
                return new DerivedEnumerationType(getBaseType(), getPath(), getDefaultValue(), getDescription(), getReference(),
                    getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static DerivedTypeBuilder<IdentityrefTypeDefinition> derivedIdentityrefBuilder(final IdentityrefTypeDefinition baseType, final SchemaPath path) {
        return new DerivedTypeBuilder<IdentityrefTypeDefinition>(baseType, path) {
            @Override
            public IdentityrefTypeDefinition build() {
                return new DerivedIdentityrefType(getBaseType(), getPath(), getDefaultValue(), getDescription(), getReference(),
                    getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static DerivedTypeBuilder<InstanceIdentifierTypeDefinition> derivedInstanceIdentifierBuilder(final InstanceIdentifierTypeDefinition baseType, final SchemaPath path) {
        return new DerivedTypeBuilder<InstanceIdentifierTypeDefinition>(baseType, path) {
            @Override
            public InstanceIdentifierTypeDefinition build() {
                return new DerivedInstanceIdentifierType(getBaseType(), getPath(), getDefaultValue(), getDescription(), getReference(),
                    getStatus(), getUnits(), getUnknownSchemaNodes(), baseType.requireInstance());
            }
        };
    }

    public static DerivedTypeBuilder<IntegerTypeDefinition> derivedIntegerBuilder(final IntegerTypeDefinition baseType, final SchemaPath path) {
        return new DerivedTypeBuilder<IntegerTypeDefinition>(baseType, path) {
            @Override
            public IntegerTypeDefinition build() {
                return new DerivedIntegerType(getBaseType(), getPath(), getDefaultValue(), getDescription(), getReference(),
                    getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static DerivedTypeBuilder<LeafrefTypeDefinition> derivedLeafrefBuilder(final LeafrefTypeDefinition baseType, final SchemaPath path) {
        return new DerivedTypeBuilder<LeafrefTypeDefinition>(baseType, path) {
            @Override
            public LeafrefTypeDefinition build() {
                return new DerivedLeafrefType(getBaseType(), getPath(), getDefaultValue(), getDescription(), getReference(),
                    getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static DerivedTypeBuilder<StringTypeDefinition> derivedStringBuilder(final StringTypeDefinition baseType, final SchemaPath path) {
        return new DerivedTypeBuilder<StringTypeDefinition>(baseType, path) {
            @Override
            public StringTypeDefinition build() {
                return new DerivedStringType(getBaseType(), getPath(), getDefaultValue(), getDescription(), getReference(),
                    getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static DerivedTypeBuilder<UnionTypeDefinition> derivedUnionBuilder(final UnionTypeDefinition baseType, final SchemaPath path) {
        return new DerivedTypeBuilder<UnionTypeDefinition>(baseType, path) {
            @Override
            public DerivedUnionType build() {
                return new DerivedUnionType(getBaseType(), getPath(), getDefaultValue(), getDescription(), getReference(),
                    getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    public static DerivedTypeBuilder<UnsignedIntegerTypeDefinition> derivedUnsignedBuilder(final UnsignedIntegerTypeDefinition baseType, final SchemaPath path) {
        return new DerivedTypeBuilder<UnsignedIntegerTypeDefinition>(baseType, path) {
            @Override
            public UnsignedIntegerTypeDefinition build() {
                return new DerivedUnsignedType(getBaseType(), getPath(), getDefaultValue(), getDescription(), getReference(),
                    getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }
}
