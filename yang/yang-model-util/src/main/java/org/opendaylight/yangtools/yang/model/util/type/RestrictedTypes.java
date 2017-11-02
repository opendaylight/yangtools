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
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
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
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint8TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;

/**
 * Restricted types are a refinement of the restrictions applied to a particular type. YANG defines restrictions only
 * on a subset of the base types, but conceptually any such definition can hold unknown nodes.
 *
 * <p>
 * 1) Restrictable
 *    binary (length)
 *    int{8,16,32,64} (range)
 *    string (length, patterns)
 *    uint{8,16,32,64} (range)
 *    decimal64 (range)
 *    instance-identifier (require-instance)
 *
 * <p>
 * 2) Non-restrictable
 *    boolean
 *    bits
 *    empty (ignores default on derivation)
 *    enumeration
 *    identityref
 *    leafref
 *    union
 *
 * <p>
 * This class holds methods which allow creation of restricted types using {@link TypeBuilder} and its subclasses. Each
 * restricted type is logically anchored at a {@link SchemaPath}, but can be substituted by its base type if it does
 * not contribute any additional restrictions. TypeBuilder instances take this into account, and result in the base type
 * being returned from the builder when the base type and restricted type are semantically equal.
 *
 * <p>
 * Restricted types inherit the default value, description, reference, status and units from the base type, if that type
 * defines them.
 */
@Beta
public final class RestrictedTypes {
    private RestrictedTypes() {
        throw new UnsupportedOperationException();
    }

    public static LengthRestrictedTypeBuilder<BinaryTypeDefinition> newBinaryBuilder(
            @Nonnull final BinaryTypeDefinition baseType, @Nonnull final SchemaPath path) {
        return new LengthRestrictedTypeBuilder<BinaryTypeDefinition>(baseType, path) {
            @Override
            BinaryTypeDefinition buildType(final @Nullable LengthConstraint lengthConstraint) {
                return new RestrictedBinaryType(getBaseType(), getPath(), getUnknownSchemaNodes(), lengthConstraint);
            }

            @Override
            LengthConstraint typeLengthConstraints() {
                /**
                 * Length constraint imposed on YANG binary type by our implementation. byte[].length is an integer,
                 * capping our ability to support arbitrary binary data.
                 */
                return JavaLengthConstraints.INTEGER_SIZE_CONSTRAINTS;
            }
        };
    }

    public static BitsTypeBuilder newBitsBuilder(final BitsTypeDefinition baseType, final SchemaPath path) {
        return new BitsTypeBuilder(baseType, path);
    }

    public static TypeBuilder<BooleanTypeDefinition> newBooleanBuilder(@Nonnull final BooleanTypeDefinition baseType,
            @Nonnull final SchemaPath path) {
        return new AbstractRestrictedTypeBuilder<BooleanTypeDefinition>(baseType, path) {
            @Override
            BooleanTypeDefinition buildType() {
                return new RestrictedBooleanType(getBaseType(), getPath(), getUnknownSchemaNodes());
            }
        };
    }

    public static RangeRestrictedTypeBuilder<DecimalTypeDefinition> newDecima64Builder(
            final DecimalTypeDefinition baseType, final SchemaPath path) {
        return new RangeRestrictedTypeBuilderWithBase<DecimalTypeDefinition>(baseType, path) {
            @Override
            DecimalTypeDefinition buildType(final RangeConstraint<?> rangeConstraint) {
                return new RestrictedDecimalType(getBaseType(), getPath(), getUnknownSchemaNodes(), rangeConstraint);
            }
        };
    }

    public static TypeBuilder<EmptyTypeDefinition> newEmptyBuilder(final EmptyTypeDefinition baseType,
            final SchemaPath path) {
        return new AbstractRestrictedTypeBuilder<EmptyTypeDefinition>(baseType, path) {
            @Override
            EmptyTypeDefinition buildType() {
                return new RestrictedEmptyType(getBaseType(), getPath(), getUnknownSchemaNodes());
            }
        };
    }

    public static EnumerationTypeBuilder newEnumerationBuilder(final EnumTypeDefinition baseType,
            final SchemaPath path) {
        return new EnumerationTypeBuilder(baseType, path);
    }

    public static TypeBuilder<IdentityrefTypeDefinition> newIdentityrefBuilder(final IdentityrefTypeDefinition baseType,
            final SchemaPath path) {
        return new AbstractRestrictedTypeBuilder<IdentityrefTypeDefinition>(baseType, path) {
            @Override
            IdentityrefTypeDefinition buildType() {
                return new RestrictedIdentityrefType(getBaseType(), getPath(), getUnknownSchemaNodes());
            }
        };
    }

    public static InstanceIdentifierTypeBuilder newInstanceIdentifierBuilder(
            final InstanceIdentifierTypeDefinition baseType, final SchemaPath path) {
        return new InstanceIdentifierTypeBuilder(baseType, path);
    }

    public static RequireInstanceRestrictedTypeBuilder<LeafrefTypeDefinition> newLeafrefBuilder(
            final LeafrefTypeDefinition baseType, final SchemaPath path) {
        return new RequireInstanceRestrictedTypeBuilder<LeafrefTypeDefinition>(baseType, path) {
            @Override
            LeafrefTypeDefinition buildType() {
                if (getRequireInstance() == getBaseType().requireInstance()) {
                    return getBaseType();
                }
                return new RestrictedLeafrefType(getBaseType(), getPath(), getUnknownSchemaNodes(),
                        getRequireInstance());
            }
        };
    }

    public static RangeRestrictedTypeBuilder<Int8TypeDefinition> newInt8Builder(
            final Int8TypeDefinition baseType, final SchemaPath path) {
        return new RangeRestrictedTypeBuilderWithBase<Int8TypeDefinition>(baseType, path) {
            @Override
            Int8TypeDefinition buildType(final RangeConstraint<?> rangeConstraint) {
                return new RestrictedInt8Type(getBaseType(), getPath(), getUnknownSchemaNodes(), rangeConstraint);
            }
        };
    }

    public static RangeRestrictedTypeBuilder<Int16TypeDefinition> newInt16Builder(
            final Int16TypeDefinition baseType, final SchemaPath path) {
        return new RangeRestrictedTypeBuilderWithBase<Int16TypeDefinition>(baseType, path) {
            @Override
            Int16TypeDefinition buildType(final RangeConstraint<?> rangeConstraint) {
                return new RestrictedInt16Type(getBaseType(), getPath(), getUnknownSchemaNodes(), rangeConstraint);
            }
        };
    }

    public static RangeRestrictedTypeBuilder<Int32TypeDefinition> newInt32Builder(
            final Int32TypeDefinition baseType, final SchemaPath path) {
        return new RangeRestrictedTypeBuilderWithBase<Int32TypeDefinition>(baseType, path) {
            @Override
            Int32TypeDefinition buildType(final RangeConstraint<?> rangeConstraint) {
                return new RestrictedInt32Type(getBaseType(), getPath(), getUnknownSchemaNodes(), rangeConstraint);
            }
        };
    }

    public static RangeRestrictedTypeBuilder<Int64TypeDefinition> newInt64Builder(
            final Int64TypeDefinition baseType, final SchemaPath path) {
        return new RangeRestrictedTypeBuilderWithBase<Int64TypeDefinition>(baseType, path) {
            @Override
            Int64TypeDefinition buildType(final RangeConstraint<?> rangeConstraint) {
                return new RestrictedInt64Type(getBaseType(), getPath(), getUnknownSchemaNodes(), rangeConstraint);
            }
        };
    }

    public static StringTypeBuilder newStringBuilder(final StringTypeDefinition baseType, final SchemaPath path) {
        return new StringTypeBuilder(baseType, path);
    }

    public static TypeBuilder<UnionTypeDefinition> newUnionBuilder(final UnionTypeDefinition baseType,
            final SchemaPath path) {
        return new AbstractRestrictedTypeBuilder<UnionTypeDefinition>(baseType, path) {
            @Override
            UnionTypeDefinition buildType() {
                return new RestrictedUnionType(getBaseType(), getPath(), getUnknownSchemaNodes());
            }
        };
    }

    public static RangeRestrictedTypeBuilder<Uint8TypeDefinition> newUint8Builder(
            final Uint8TypeDefinition baseType, final SchemaPath path) {
        return new RangeRestrictedTypeBuilderWithBase<Uint8TypeDefinition>(baseType, path) {
            @Override
            Uint8TypeDefinition buildType(final RangeConstraint<?> rangeConstraint) {
                return new RestrictedUint8Type(getBaseType(), getPath(), getUnknownSchemaNodes(), rangeConstraint);
            }
        };
    }

    public static RangeRestrictedTypeBuilder<Uint16TypeDefinition> newUint16Builder(
            final Uint16TypeDefinition baseType, final SchemaPath path) {
        return new RangeRestrictedTypeBuilderWithBase<Uint16TypeDefinition>(baseType, path) {
            @Override
            Uint16TypeDefinition buildType(final RangeConstraint<?> rangeConstraint) {
                return new RestrictedUint16Type(getBaseType(), getPath(), getUnknownSchemaNodes(), rangeConstraint);
            }
        };
    }

    public static RangeRestrictedTypeBuilder<Uint32TypeDefinition> newUint32Builder(
            final Uint32TypeDefinition baseType, final SchemaPath path) {
        return new RangeRestrictedTypeBuilderWithBase<Uint32TypeDefinition>(baseType, path) {
            @Override
            Uint32TypeDefinition buildType(final RangeConstraint<?> rangeConstraint) {
                return new RestrictedUint32Type(getBaseType(), getPath(), getUnknownSchemaNodes(), rangeConstraint);
            }
        };
    }

    public static RangeRestrictedTypeBuilder<Uint64TypeDefinition> newUint64Builder(
            final Uint64TypeDefinition baseType, final SchemaPath path) {
        return new RangeRestrictedTypeBuilderWithBase<Uint64TypeDefinition>(baseType, path) {
            @Override
            Uint64TypeDefinition buildType(final RangeConstraint<?> rangeConstraint) {
                return new RestrictedUint64Type(getBaseType(), getPath(), getUnknownSchemaNodes(), rangeConstraint);
            }
        };
    }
}
