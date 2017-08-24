/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.annotations.Beta;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
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
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;

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
            BinaryTypeDefinition buildType(final List<LengthConstraint> lengthConstraints) {
                return new RestrictedBinaryType(getBaseType(), getPath(), getUnknownSchemaNodes(), lengthConstraints);
            }

            @Override
            List<LengthConstraint> typeLengthConstraints() {
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
            DecimalTypeDefinition buildType(final List<RangeConstraint> rangeConstraints) {
                return new RestrictedDecimalType(getBaseType(), getPath(), getUnknownSchemaNodes(), rangeConstraints);
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

    public static RangeRestrictedTypeBuilder<IntegerTypeDefinition> newIntegerBuilder(
            final IntegerTypeDefinition baseType, final SchemaPath path) {
        return new RangeRestrictedTypeBuilderWithBase<IntegerTypeDefinition>(baseType, path) {
            @Override
            IntegerTypeDefinition buildType(final List<RangeConstraint> rangeConstraints) {
                return new RestrictedIntegerType(getBaseType(), getPath(), getUnknownSchemaNodes(), rangeConstraints);
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

    public static RangeRestrictedTypeBuilder<UnsignedIntegerTypeDefinition> newUnsignedBuilder(
            final UnsignedIntegerTypeDefinition baseType, final SchemaPath path) {
        return new RangeRestrictedTypeBuilderWithBase<UnsignedIntegerTypeDefinition>(baseType, path) {
            @Override
            UnsignedIntegerTypeDefinition buildType(final List<RangeConstraint> rangeConstraints) {
                return new RestrictedUnsignedType(getBaseType(), getPath(), getUnknownSchemaNodes(), rangeConstraints);
            }
        };
    }
}
