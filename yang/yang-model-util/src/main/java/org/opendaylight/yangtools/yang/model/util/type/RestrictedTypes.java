/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.annotations.Beta;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.eclipse.jdt.annotation.NonNull;
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

    public static @NonNull LengthRestrictedTypeBuilder<BinaryTypeDefinition> newBinaryBuilder(
            final @NonNull BinaryTypeDefinition baseType, final @NonNull SchemaPath path) {
        return new LengthRestrictedTypeBuilder<BinaryTypeDefinition>(baseType, path) {
            @Override
            BinaryTypeDefinition buildType(final @Nullable LengthConstraint constraint) {
                return new RestrictedBinaryType(getBaseType(), getPath(), getUnknownSchemaNodes(), constraint);
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

    public static @NonNull BitsTypeBuilder newBitsBuilder(final BitsTypeDefinition baseType,
            final @NonNull SchemaPath path) {
        return new BitsTypeBuilder(baseType, path);
    }

    public static @NonNull TypeBuilder<BooleanTypeDefinition> newBooleanBuilder(
            final @NonNull BooleanTypeDefinition baseType, final @NonNull SchemaPath path) {
        return new AbstractRestrictedTypeBuilder<BooleanTypeDefinition>(baseType, path) {
            @Override
            BooleanTypeDefinition buildType() {
                return new RestrictedBooleanType(getBaseType(), getPath(), getUnknownSchemaNodes());
            }
        };
    }

    public static @NonNull RangeRestrictedTypeBuilder<DecimalTypeDefinition, BigDecimal> newDecima64Builder(
            final @NonNull DecimalTypeDefinition baseType, final @NonNull SchemaPath path) {
        return new RangeRestrictedTypeBuilderWithBase<DecimalTypeDefinition, BigDecimal>(baseType, path) {
            @Override
            DecimalTypeDefinition buildType(final RangeConstraint<BigDecimal> rangeConstraint) {
                return new RestrictedDecimalType(getBaseType(), getPath(), getUnknownSchemaNodes(), rangeConstraint);
            }
        };
    }

    public static TypeBuilder<EmptyTypeDefinition> newEmptyBuilder(final EmptyTypeDefinition baseType,
            final @NonNull SchemaPath path) {
        return new AbstractRestrictedTypeBuilder<EmptyTypeDefinition>(baseType, path) {
            @Override
            EmptyTypeDefinition buildType() {
                return new RestrictedEmptyType(getBaseType(), getPath(), getUnknownSchemaNodes());
            }
        };
    }

    public static @NonNull EnumerationTypeBuilder newEnumerationBuilder(final EnumTypeDefinition baseType,
            final SchemaPath path) {
        return new EnumerationTypeBuilder(baseType, path);
    }

    public static @NonNull TypeBuilder<IdentityrefTypeDefinition> newIdentityrefBuilder(
            final IdentityrefTypeDefinition baseType, final @NonNull SchemaPath path) {
        return new AbstractRestrictedTypeBuilder<IdentityrefTypeDefinition>(baseType, path) {
            @Override
            IdentityrefTypeDefinition buildType() {
                return new RestrictedIdentityrefType(getBaseType(), getPath(), getUnknownSchemaNodes());
            }
        };
    }

    public static @NonNull InstanceIdentifierTypeBuilder newInstanceIdentifierBuilder(
            final InstanceIdentifierTypeDefinition baseType, final SchemaPath path) {
        return new InstanceIdentifierTypeBuilder(baseType, path);
    }

    public static @NonNull RequireInstanceRestrictedTypeBuilder<LeafrefTypeDefinition> newLeafrefBuilder(
            final LeafrefTypeDefinition baseType, final SchemaPath path) {
        return new RequireInstanceRestrictedTypeBuilder<LeafrefTypeDefinition>(baseType, path) {
            @Override
            LeafrefTypeDefinition buildType() {
                final LeafrefTypeDefinition base = getBaseType();
                if (getRequireInstance() == base.requireInstance()) {
                    return base;
                }
                return new RestrictedLeafrefType(getBaseType(), getPath(), getUnknownSchemaNodes(),
                        getRequireInstance());
            }
        };
    }

    public static @NonNull RangeRestrictedTypeBuilder<Int8TypeDefinition, Byte> newInt8Builder(
            final @NonNull Int8TypeDefinition baseType, final @NonNull SchemaPath path) {
        return new RangeRestrictedTypeBuilderWithBase<Int8TypeDefinition, Byte>(baseType, path) {
            @Override
            Int8TypeDefinition buildType(final RangeConstraint<Byte> rangeConstraint) {
                return new RestrictedInt8Type(getBaseType(), getPath(), getUnknownSchemaNodes(), rangeConstraint);
            }
        };
    }

    public static @NonNull RangeRestrictedTypeBuilder<Int16TypeDefinition, Short> newInt16Builder(
            final @NonNull Int16TypeDefinition baseType, final @NonNull SchemaPath path) {
        return new RangeRestrictedTypeBuilderWithBase<Int16TypeDefinition, Short>(baseType, path) {
            @Override
            Int16TypeDefinition buildType(final RangeConstraint<Short> rangeConstraint) {
                return new RestrictedInt16Type(getBaseType(), getPath(), getUnknownSchemaNodes(), rangeConstraint);
            }
        };
    }

    public static @NonNull RangeRestrictedTypeBuilder<Int32TypeDefinition, Integer> newInt32Builder(
            final @NonNull Int32TypeDefinition baseType, final @NonNull SchemaPath path) {
        return new RangeRestrictedTypeBuilderWithBase<Int32TypeDefinition, Integer>(baseType, path) {
            @Override
            Int32TypeDefinition buildType(final RangeConstraint<Integer> rangeConstraint) {
                return new RestrictedInt32Type(getBaseType(), getPath(), getUnknownSchemaNodes(), rangeConstraint);
            }
        };
    }

    public static @NonNull RangeRestrictedTypeBuilder<Int64TypeDefinition, Long> newInt64Builder(
            final @NonNull Int64TypeDefinition baseType, final @NonNull SchemaPath path) {
        return new RangeRestrictedTypeBuilderWithBase<Int64TypeDefinition, Long>(baseType, path) {
            @Override
            Int64TypeDefinition buildType(final RangeConstraint<Long> rangeConstraint) {
                return new RestrictedInt64Type(getBaseType(), getPath(), getUnknownSchemaNodes(), rangeConstraint);
            }
        };
    }

    public static @NonNull StringTypeBuilder newStringBuilder(final @NonNull StringTypeDefinition baseType,
            final @NonNull SchemaPath path) {
        return new StringTypeBuilder(baseType, path);
    }

    public static @NonNull TypeBuilder<UnionTypeDefinition> newUnionBuilder(final UnionTypeDefinition baseType,
            final @NonNull SchemaPath path) {
        return new AbstractRestrictedTypeBuilder<UnionTypeDefinition>(baseType, path) {
            @Override
            UnionTypeDefinition buildType() {
                return new RestrictedUnionType(getBaseType(), getPath(), getUnknownSchemaNodes());
            }
        };
    }

    public static @NonNull RangeRestrictedTypeBuilder<Uint8TypeDefinition, Short> newUint8Builder(
            final @NonNull Uint8TypeDefinition baseType, final @NonNull SchemaPath path) {
        return new RangeRestrictedTypeBuilderWithBase<Uint8TypeDefinition, Short>(baseType, path) {
            @Override
            Uint8TypeDefinition buildType(final RangeConstraint<Short> rangeConstraint) {
                return new RestrictedUint8Type(getBaseType(), getPath(), getUnknownSchemaNodes(), rangeConstraint);
            }
        };
    }

    public static @NonNull RangeRestrictedTypeBuilder<Uint16TypeDefinition, Integer> newUint16Builder(
            final @NonNull Uint16TypeDefinition baseType, final @NonNull SchemaPath path) {
        return new RangeRestrictedTypeBuilderWithBase<Uint16TypeDefinition, Integer>(baseType, path) {
            @Override
            Uint16TypeDefinition buildType(final RangeConstraint<Integer> rangeConstraint) {
                return new RestrictedUint16Type(getBaseType(), getPath(), getUnknownSchemaNodes(), rangeConstraint);
            }
        };
    }

    public static @NonNull RangeRestrictedTypeBuilder<Uint32TypeDefinition, Long> newUint32Builder(
            final @NonNull Uint32TypeDefinition baseType, final @NonNull SchemaPath path) {
        return new RangeRestrictedTypeBuilderWithBase<Uint32TypeDefinition, Long>(baseType, path) {
            @Override
            Uint32TypeDefinition buildType(final RangeConstraint<Long> rangeConstraint) {
                return new RestrictedUint32Type(getBaseType(), getPath(), getUnknownSchemaNodes(), rangeConstraint);
            }
        };
    }

    public static @NonNull RangeRestrictedTypeBuilder<Uint64TypeDefinition, BigInteger> newUint64Builder(
            final @NonNull Uint64TypeDefinition baseType, final @NonNull SchemaPath path) {
        return new RangeRestrictedTypeBuilderWithBase<Uint64TypeDefinition, BigInteger>(baseType, path) {
            @Override
            Uint64TypeDefinition buildType(final RangeConstraint<BigInteger> rangeConstraint) {
                return new RestrictedUint64Type(getBaseType(), getPath(), getUnknownSchemaNodes(), rangeConstraint);
            }
        };
    }
}
