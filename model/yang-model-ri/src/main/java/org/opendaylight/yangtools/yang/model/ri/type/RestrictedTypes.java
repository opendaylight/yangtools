/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.type;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.Decimal64;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.opendaylight.yangtools.yang.model.api.EffectiveStatementInference;
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
 * <p>1) Restrictable
 *    binary (length)
 *    int{8,16,32,64} (range)
 *    string (length, patterns)
 *    uint{8,16,32,64} (range)
 *    decimal64 (range)
 *    instance-identifier (require-instance)
 *
 * <p>2) Non-restrictable
 *    boolean
 *    bits
 *    empty (ignores default on derivation)
 *    enumeration
 *    identityref
 *    leafref
 *    union
 *
 * <p>This class holds methods which allow creation of restricted types using {@link TypeBuilder} and its subclasses.
 * Each restricted type is logically anchored at an {@link EffectiveStatementInference}, but can be substituted by its
 * base type if it does not contribute any additional restrictions. TypeBuilder instances take this into account, and
 * result in the base type being returned from the builder when the base type and restricted type are semantically
 * equal.
 *
 * <p>Restricted types inherit the default value, description, reference, status and units from the base type, if that
 * type defines them.
 */
@Beta
public final class RestrictedTypes {
    private RestrictedTypes() {
        // Hidden on purpose
    }

    public static @NonNull LengthRestrictedTypeBuilder<BinaryTypeDefinition> newBinaryBuilder(
            final @NonNull BinaryTypeDefinition baseType, final @NonNull QName qname) {
        return new LengthRestrictedTypeBuilder<>(baseType, qname) {
            @Override
            BinaryTypeDefinition buildType(final @Nullable LengthConstraint constraint) {
                return new RestrictedBinaryType(getBaseType(), getQName(), getUnknownSchemaNodes(), constraint);
            }

            @Override
            LengthConstraint typeLengthConstraints() {
                // Length constraint imposed on YANG binary type by our implementation. byte[].length is an integer,
                // capping our ability to support arbitrary binary data.
                return JavaLengthConstraints.INTEGER_SIZE_CONSTRAINTS;
            }
        };
    }

    public static @NonNull BitsTypeBuilder newBitsBuilder(final BitsTypeDefinition baseType, final QName qname) {
        return new BitsTypeBuilder(baseType, qname);
    }

    public static @NonNull TypeBuilder<BooleanTypeDefinition> newBooleanBuilder(
            final @NonNull BooleanTypeDefinition baseType, final @NonNull QName qname) {
        return new AbstractRestrictedTypeBuilder<>(baseType, qname) {
            @Override
            BooleanTypeDefinition buildType() {
                return new RestrictedBooleanType(getBaseType(), getQName(), getUnknownSchemaNodes());
            }
        };
    }

    public static @NonNull RangeRestrictedTypeBuilder<DecimalTypeDefinition, Decimal64> newDecima64Builder(
            final DecimalTypeDefinition baseType, final QName qname) {
        return new RangeRestrictedTypeBuilderWithBase<>(baseType, qname) {
            @Override
            DecimalTypeDefinition buildType(final RangeConstraint<@NonNull Decimal64> rangeConstraint) {
                return new RestrictedDecimalType(getBaseType(), getQName(), getUnknownSchemaNodes(), rangeConstraint);
            }
        };
    }

    public static @NonNull TypeBuilder<EmptyTypeDefinition> newEmptyBuilder(final EmptyTypeDefinition baseType,
            final QName qname) {
        return new AbstractRestrictedTypeBuilder<>(baseType, qname) {
            @Override
            EmptyTypeDefinition buildType() {
                return new RestrictedEmptyType(getBaseType(), getQName(), getUnknownSchemaNodes());
            }
        };
    }

    public static @NonNull EnumerationTypeBuilder newEnumerationBuilder(final EnumTypeDefinition baseType,
            final QName qname) {
        return new EnumerationTypeBuilder(baseType, qname);
    }

    public static @NonNull TypeBuilder<IdentityrefTypeDefinition> newIdentityrefBuilder(
            final IdentityrefTypeDefinition baseType, final QName qname) {
        return new AbstractRestrictedTypeBuilder<>(baseType, qname) {
            @Override
            IdentityrefTypeDefinition buildType() {
                return new RestrictedIdentityrefType(getBaseType(), getQName(), getUnknownSchemaNodes());
            }
        };
    }

    public static @NonNull InstanceIdentifierTypeBuilder newInstanceIdentifierBuilder(
            final InstanceIdentifierTypeDefinition baseType, final QName qname) {
        return new InstanceIdentifierTypeBuilder(baseType, qname);
    }

    public static @NonNull RequireInstanceRestrictedTypeBuilder<LeafrefTypeDefinition> newLeafrefBuilder(
            final LeafrefTypeDefinition baseType, final QName qname) {
        return new RequireInstanceRestrictedTypeBuilder<>(baseType, qname) {
            @Override
            LeafrefTypeDefinition buildType() {
                final LeafrefTypeDefinition base = getBaseType();
                if (getRequireInstance() == base.requireInstance()) {
                    return base;
                }
                return new RestrictedLeafrefType(getBaseType(), getQName(), getUnknownSchemaNodes(),
                        getRequireInstance());
            }
        };
    }

    public static @NonNull RangeRestrictedTypeBuilder<Int8TypeDefinition, Byte> newInt8Builder(
            final Int8TypeDefinition baseType, final QName qname) {
        return new RangeRestrictedTypeBuilderWithBase<>(baseType, qname) {
            @Override
            Int8TypeDefinition buildType(final RangeConstraint<Byte> rangeConstraint) {
                return new RestrictedInt8Type(getBaseType(), getQName(), getUnknownSchemaNodes(), rangeConstraint);
            }
        };
    }

    public static @NonNull RangeRestrictedTypeBuilder<Int16TypeDefinition, Short> newInt16Builder(
            final Int16TypeDefinition baseType, final QName qname) {
        return new RangeRestrictedTypeBuilderWithBase<>(baseType, qname) {
            @Override
            Int16TypeDefinition buildType(final RangeConstraint<Short> rangeConstraint) {
                return new RestrictedInt16Type(getBaseType(), getQName(), getUnknownSchemaNodes(), rangeConstraint);
            }
        };
    }

    public static @NonNull RangeRestrictedTypeBuilder<Int32TypeDefinition, Integer> newInt32Builder(
            final Int32TypeDefinition baseType, final QName qname) {
        return new RangeRestrictedTypeBuilderWithBase<>(baseType, qname) {
            @Override
            Int32TypeDefinition buildType(final RangeConstraint<Integer> rangeConstraint) {
                return new RestrictedInt32Type(getBaseType(), getQName(), getUnknownSchemaNodes(), rangeConstraint);
            }
        };
    }

    public static @NonNull RangeRestrictedTypeBuilder<Int64TypeDefinition, Long> newInt64Builder(
            final Int64TypeDefinition baseType, final QName qname) {
        return new RangeRestrictedTypeBuilderWithBase<>(baseType, qname) {
            @Override
            Int64TypeDefinition buildType(final RangeConstraint<Long> rangeConstraint) {
                return new RestrictedInt64Type(getBaseType(), getQName(), getUnknownSchemaNodes(), rangeConstraint);
            }
        };
    }

    public static @NonNull StringTypeBuilder newStringBuilder(final StringTypeDefinition baseType, final QName qname) {
        return new StringTypeBuilder(baseType, qname);
    }

    public static @NonNull TypeBuilder<UnionTypeDefinition> newUnionBuilder(final UnionTypeDefinition baseType,
            final QName qname) {
        return new AbstractRestrictedTypeBuilder<>(baseType, qname) {
            @Override
            UnionTypeDefinition buildType() {
                return new RestrictedUnionType(getBaseType(), getQName(), getUnknownSchemaNodes());
            }
        };
    }

    public static @NonNull RangeRestrictedTypeBuilder<Uint8TypeDefinition, Uint8> newUint8Builder(
            final Uint8TypeDefinition baseType, final QName qname) {
        return new RangeRestrictedTypeBuilderWithBase<>(baseType, qname) {
            @Override
            Uint8TypeDefinition buildType(final RangeConstraint<@NonNull Uint8> rangeConstraint) {
                return new RestrictedUint8Type(getBaseType(), getQName(), getUnknownSchemaNodes(), rangeConstraint);
            }
        };
    }

    public static @NonNull RangeRestrictedTypeBuilder<Uint16TypeDefinition, Uint16> newUint16Builder(
            final Uint16TypeDefinition baseType, final QName qname) {
        return new RangeRestrictedTypeBuilderWithBase<>(baseType, qname) {
            @Override
            Uint16TypeDefinition buildType(final RangeConstraint<@NonNull Uint16> rangeConstraint) {
                return new RestrictedUint16Type(getBaseType(), getQName(), getUnknownSchemaNodes(), rangeConstraint);
            }
        };
    }

    public static @NonNull RangeRestrictedTypeBuilder<Uint32TypeDefinition, Uint32> newUint32Builder(
            final Uint32TypeDefinition baseType, final QName qname) {
        return new RangeRestrictedTypeBuilderWithBase<>(baseType, qname) {
            @Override
            Uint32TypeDefinition buildType(final RangeConstraint<@NonNull Uint32> rangeConstraint) {
                return new RestrictedUint32Type(getBaseType(), getQName(), getUnknownSchemaNodes(), rangeConstraint);
            }
        };
    }

    public static @NonNull RangeRestrictedTypeBuilder<Uint64TypeDefinition, Uint64> newUint64Builder(
            final Uint64TypeDefinition baseType, final QName qname) {
        return new RangeRestrictedTypeBuilderWithBase<>(baseType, qname) {
            @Override
            Uint64TypeDefinition buildType(final RangeConstraint<@NonNull Uint64> rangeConstraint) {
                return new RestrictedUint64Type(getBaseType(), getQName(), getUnknownSchemaNodes(), rangeConstraint);
            }
        };
    }
}
