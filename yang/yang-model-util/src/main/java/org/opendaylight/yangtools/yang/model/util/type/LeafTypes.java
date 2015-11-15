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
public final class LeafTypes {
    private LeafTypes() {
        throw new UnsupportedOperationException();
    }

    public static LeafTypeBuilder<?> leafTypeBuilder(@Nonnull final TypeDefinition<?> baseType,
            @Nonnull final SchemaPath path) {
        if (baseType instanceof BinaryTypeDefinition) {
            return leafBinaryBuilder((BinaryTypeDefinition) baseType, path);
        } else if (baseType instanceof BitsTypeDefinition) {
            return leafBitsBuilder((BitsTypeDefinition) baseType, path);
        } else if (baseType instanceof BooleanTypeDefinition) {
            return leafBooleanBuilder((BooleanTypeDefinition) baseType, path);
        } else if (baseType instanceof DecimalTypeDefinition) {
            return leafDecimalBuilder((DecimalTypeDefinition) baseType, path);
        } else if (baseType instanceof EmptyTypeDefinition) {
            return leafEmptyBuilder((EmptyTypeDefinition) baseType, path);
        } else if (baseType instanceof EnumTypeDefinition) {
            return leafEnumerationBuilder((EnumTypeDefinition) baseType, path);
        } else if (baseType instanceof IdentityrefTypeDefinition) {
            return leafIdentityrefBuilder((IdentityrefTypeDefinition) baseType, path);
        } else if (baseType instanceof InstanceIdentifierTypeDefinition) {
            return leafInstanceIdentifierBuilder((InstanceIdentifierTypeDefinition) baseType, path);
        } else if (baseType instanceof IntegerTypeDefinition) {
            return leafIntegerBuilder((IntegerTypeDefinition) baseType, path);
        } else if (baseType instanceof LeafrefTypeDefinition) {
            return leafLeafrefBuilder((LeafrefTypeDefinition) baseType, path);
        } else if (baseType instanceof StringTypeDefinition) {
            return leafStringBuilder((StringTypeDefinition) baseType, path);
        } else if (baseType instanceof UnionTypeDefinition) {
            return leafUnionBuilder((UnionTypeDefinition) baseType, path);
        } else if (baseType instanceof UnsignedIntegerTypeDefinition) {
            return leafUnsignedBuilder((UnsignedIntegerTypeDefinition) baseType, path);
        } else {
            throw new IllegalArgumentException("Unhandled type definition class " + baseType.getClass());
        }
    }

    private static LeafTypeBuilder<BinaryTypeDefinition> leafBinaryBuilder(@Nonnull final BinaryTypeDefinition baseType, @Nonnull final SchemaPath path) {
        return new LeafTypeBuilder<BinaryTypeDefinition>(baseType, path) {
            @Override
            public BinaryTypeDefinition buildType() {
                return new DerivedBinaryType(getBaseType(), getPath(), getDefaultValue(), getDescription(), getReference(),
                    getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static LeafTypeBuilder<BitsTypeDefinition> leafBitsBuilder(final BitsTypeDefinition baseType, final SchemaPath path) {
        return new LeafTypeBuilder<BitsTypeDefinition>(baseType, path) {
            @Override
            public BitsTypeDefinition buildType() {
                return new DerivedBitsType(getBaseType(), getPath(), getDefaultValue(), getDescription(), getReference(),
                    getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static LeafTypeBuilder<BooleanTypeDefinition> leafBooleanBuilder(@Nonnull final BooleanTypeDefinition baseType, @Nonnull final SchemaPath path) {
        return new LeafTypeBuilder<BooleanTypeDefinition>(baseType, path) {
            @Override
            public BooleanTypeDefinition buildType() {
                return new DerivedBooleanType(getBaseType(), getPath(), getDefaultValue(), getDescription(), getReference(),
                    getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static LeafTypeBuilder<DecimalTypeDefinition> leafDecimalBuilder(final DecimalTypeDefinition baseType, final SchemaPath path) {
        return new LeafTypeBuilder<DecimalTypeDefinition>(baseType, path) {
            @Override
            public DecimalTypeDefinition buildType() {
                return new DerivedDecimalType(getBaseType(), getPath(), getDefaultValue(), getDescription(), getReference(),
                    getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static LeafTypeBuilder<EmptyTypeDefinition> leafEmptyBuilder(final EmptyTypeDefinition baseType, final SchemaPath path) {
        return new LeafTypeBuilder<EmptyTypeDefinition>(baseType, path) {
            @Override
            public EmptyTypeDefinition buildType() {
                return new DerivedEmptyType(getBaseType(), getPath(), getDefaultValue(), getDescription(), getReference(),
                    getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static LeafTypeBuilder<EnumTypeDefinition> leafEnumerationBuilder(final EnumTypeDefinition baseType, final SchemaPath path) {
        return new LeafTypeBuilder<EnumTypeDefinition>(baseType, path) {
            @Override
            public EnumTypeDefinition buildType() {
                return new DerivedEnumerationType(getBaseType(), getPath(), getDefaultValue(), getDescription(), getReference(),
                    getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static LeafTypeBuilder<IdentityrefTypeDefinition> leafIdentityrefBuilder(final IdentityrefTypeDefinition baseType, final SchemaPath path) {
        return new LeafTypeBuilder<IdentityrefTypeDefinition>(baseType, path) {
            @Override
            public IdentityrefTypeDefinition buildType() {
                return new DerivedIdentityrefType(getBaseType(), getPath(), getDefaultValue(), getDescription(), getReference(),
                    getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static LeafTypeBuilder<InstanceIdentifierTypeDefinition> leafInstanceIdentifierBuilder(final InstanceIdentifierTypeDefinition baseType, final SchemaPath path) {
        return new LeafTypeBuilder<InstanceIdentifierTypeDefinition>(baseType, path) {
            @Override
            public InstanceIdentifierTypeDefinition buildType() {
                return new DerivedInstanceIdentifierType(getBaseType(), getPath(), getDefaultValue(), getDescription(), getReference(),
                    getStatus(), getUnits(), getUnknownSchemaNodes(), baseType.requireInstance());
            }
        };
    }

    private static LeafTypeBuilder<IntegerTypeDefinition> leafIntegerBuilder(final IntegerTypeDefinition baseType, final SchemaPath path) {
        return new LeafTypeBuilder<IntegerTypeDefinition>(baseType, path) {
            @Override
            public IntegerTypeDefinition buildType() {
                return new DerivedIntegerType(getBaseType(), getPath(), getDefaultValue(), getDescription(), getReference(),
                    getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static LeafTypeBuilder<LeafrefTypeDefinition> leafLeafrefBuilder(final LeafrefTypeDefinition baseType, final SchemaPath path) {
        return new LeafTypeBuilder<LeafrefTypeDefinition>(baseType, path) {
            @Override
            public LeafrefTypeDefinition buildType() {
                return new DerivedLeafrefType(getBaseType(), getPath(), getDefaultValue(), getDescription(), getReference(),
                    getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static LeafTypeBuilder<StringTypeDefinition> leafStringBuilder(final StringTypeDefinition baseType, final SchemaPath path) {
        return new LeafTypeBuilder<StringTypeDefinition>(baseType, path) {
            @Override
            public StringTypeDefinition buildType() {
                return new DerivedStringType(getBaseType(), getPath(), getDefaultValue(), getDescription(), getReference(),
                    getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static LeafTypeBuilder<UnionTypeDefinition> leafUnionBuilder(final UnionTypeDefinition baseType, final SchemaPath path) {
        return new LeafTypeBuilder<UnionTypeDefinition>(baseType, path) {
            @Override
            public DerivedUnionType buildType() {
                return new DerivedUnionType(getBaseType(), getPath(), getDefaultValue(), getDescription(), getReference(),
                    getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }

    private static LeafTypeBuilder<UnsignedIntegerTypeDefinition> leafUnsignedBuilder(final UnsignedIntegerTypeDefinition baseType, final SchemaPath path) {
        return new LeafTypeBuilder<UnsignedIntegerTypeDefinition>(baseType, path) {
            @Override
            public UnsignedIntegerTypeDefinition buildType() {
                return new DerivedUnsignedType(getBaseType(), getPath(), getDefaultValue(), getDescription(), getReference(),
                    getStatus(), getUnits(), getUnknownSchemaNodes());
            }
        };
    }
}
