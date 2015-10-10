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

/*
 * The types are divided to following groups:
 *
 * 1) Concrete base type, restrictable
 *    binary (length)
 *    int{8,16,32,64} (range)
 *    string (length, patterns)
 *    uint{8,16,32,64} (range)
 *
 * 2) Abstract base type, restrictable
 *    decimal64 (range)
 *    instance-identifier (require-instance)
 *
 * 3) Concrete base type, non-restrictable
 *    boolean
 *    empty (ignores default on derivation)
 *
 * 4) Abstract base type, non-restrictable
 *    bits
 *    enumeration
 *    leafref
 *    identityref
 *    union
 *
 * There are four operations which can be performed on a type:
 *
 * - restriction via a 'type' statement, which can be done multiple times
 * - derivation via a 'typedef' statement, which can be done multiple times
 * - concretization via a 'leaf' statement
 * - concretization via a 'leaf-list' statement (no 'default' possible)
 */
@Beta
public final class RestrictedTypes {
    private RestrictedTypes() {
        throw new UnsupportedOperationException();
    }

    public static LengthRestrictedTypeBuilder<BinaryTypeDefinition> newBinaryBuilder(@Nonnull final SchemaPath path) {
        return newBinaryBuilder(BaseBinaryType.INSTANCE, path);
    }

    public static LengthRestrictedTypeBuilder<BinaryTypeDefinition> newBinaryBuilder(@Nonnull final BinaryTypeDefinition baseType, @Nonnull final SchemaPath path) {
        return new LengthRestrictedTypeBuilder<BinaryTypeDefinition>(baseType, path) {
            @Override
            BinaryTypeDefinition buildType() {
                return new RestrictedBinaryType(getBaseType(), getPath(), getUnknownSchemaNodes(),
                    calculateLenghtConstraints(getBaseType().getLengthConstraints()));
            }
        };
    }

    public static TypeBuilder<BooleanTypeDefinition> newBooleanBuilder(final SchemaPath path) {
        return newBooleanBuilder(BaseBooleanType.INSTANCE, path);
    }

    public static TypeBuilder<BooleanTypeDefinition> newBooleanBuilder(@Nonnull final BooleanTypeDefinition baseType, @Nonnull final SchemaPath path) {
        return new AbstractRestrictedTypeBuilder<BooleanTypeDefinition>(baseType, path) {
            @Override
            BooleanTypeDefinition buildType() {
                return new RestrictedBooleanType(getBaseType(), getPath(), getUnknownSchemaNodes());
            }
        };
    }

    public static TypeBuilder<EmptyTypeDefinition> newEmptyBuilder(final SchemaPath path) {
        return newEmptyBuilder(BaseEmptyType.INSTANCE, path);
    }

    public static TypeBuilder<EmptyTypeDefinition> newEmptyBuilder(final EmptyTypeDefinition baseType, final SchemaPath path) {
        return new AbstractRestrictedTypeBuilder<EmptyTypeDefinition>(baseType, path) {
            @Override
            EmptyTypeDefinition buildType() {
                return new RestrictedEmptyType(getBaseType(), getPath(), getUnknownSchemaNodes());
            }
        };
    }

    public static RangeRestrictedTypeBuilder<IntegerTypeDefinition> newInt8Builder(final SchemaPath path) {
        return newIntegerBuilder(BaseInt8Type.INSTANCE, path);
    }

    public static RangeRestrictedTypeBuilder<IntegerTypeDefinition> newInt16Builder(final SchemaPath path) {
        return newIntegerBuilder(BaseInt16Type.INSTANCE, path);
    }

    public static RangeRestrictedTypeBuilder<IntegerTypeDefinition> newInt32Builder(final SchemaPath path) {
        return newIntegerBuilder(BaseInt32Type.INSTANCE, path);
    }

    public static RangeRestrictedTypeBuilder<IntegerTypeDefinition> newInt64Builder(final SchemaPath path) {
        return newIntegerBuilder(BaseInt64Type.INSTANCE, path);
    }

    public static RangeRestrictedTypeBuilder<IntegerTypeDefinition> newIntegerBuilder(final IntegerTypeDefinition baseType, final SchemaPath path) {
        return new RangeRestrictedTypeBuilder<IntegerTypeDefinition>(baseType, path) {
            @Override
            IntegerTypeDefinition buildType() {
                return new RestrictedIntegerType(getBaseType(), getPath(), getUnknownSchemaNodes(),
                    calculateRangeConstraints(getBaseType().getRangeConstraints()));
            }
        };
    }

    public static RangeRestrictedTypeBuilder<UnsignedIntegerTypeDefinition> newUint8Builder(final SchemaPath path) {
        return newUnsignedBuilder(BaseUint8Type.INSTANCE, path);
    }

    public static RangeRestrictedTypeBuilder<UnsignedIntegerTypeDefinition> newUint16Builder(final SchemaPath path) {
        return newUnsignedBuilder(BaseUint16Type.INSTANCE, path);
    }

    public static RangeRestrictedTypeBuilder<UnsignedIntegerTypeDefinition> newUint32Builder(final SchemaPath path) {
        return newUnsignedBuilder(BaseUint32Type.INSTANCE, path);
    }

    public static RangeRestrictedTypeBuilder<UnsignedIntegerTypeDefinition> newUint64Builder(final SchemaPath path) {
        return newUnsignedBuilder(BaseUint64Type.INSTANCE, path);
    }

    public static RangeRestrictedTypeBuilder<UnsignedIntegerTypeDefinition> newUnsignedBuilder(final UnsignedIntegerTypeDefinition baseType, final SchemaPath path) {
        return new RangeRestrictedTypeBuilder<UnsignedIntegerTypeDefinition>(baseType, path) {
            @Override
            UnsignedIntegerTypeDefinition buildType() {
                return new RestrictedUnsignedType(getBaseType(), getPath(), getUnknownSchemaNodes(),
                    calculateRangeConstraints(getBaseType().getRangeConstraints()));
            }
        };
    }

    public static TypeBuilder<BitsTypeDefinition> newBitsBuilder(final BitsTypeDefinition baseType, final SchemaPath path) {
        return new AbstractRestrictedTypeBuilder<BitsTypeDefinition>(baseType, path) {
            @Override
            BitsTypeDefinition buildType() {
                return new RestrictedBitsType(getBaseType(), getPath(), getUnknownSchemaNodes());
            }
        };
    }

    public static RangeRestrictedTypeBuilder<DecimalTypeDefinition> newDecima64Builder(final DecimalTypeDefinition baseType, final SchemaPath path) {
        return new RangeRestrictedTypeBuilder<DecimalTypeDefinition>(baseType, path) {
            @Override
            DecimalTypeDefinition buildType() {
                return new RestrictedDecimalType(getBaseType(), getPath(), getUnknownSchemaNodes(),
                    calculateRangeConstraints(getBaseType().getRangeConstraints()));
            }
        };
    }

    public static TypeBuilder<EnumTypeDefinition> newEnumerationBuilder(final EnumTypeDefinition baseType, final SchemaPath path) {
        return new AbstractRestrictedTypeBuilder<EnumTypeDefinition>(baseType, path) {
            @Override
            EnumTypeDefinition buildType() {
                return new RestrictedEnumerationType(getBaseType(), getPath(), getUnknownSchemaNodes());
            }
        };
    }

    public static TypeBuilder<IdentityrefTypeDefinition> newIdentityrefBuilder(final IdentityrefTypeDefinition baseType, final SchemaPath path) {
        return new AbstractRestrictedTypeBuilder<IdentityrefTypeDefinition>(baseType, path) {
            @Override
            IdentityrefTypeDefinition buildType() {
                return new RestrictedIdentityrefType(getBaseType(), getPath(), getUnknownSchemaNodes());
            }
        };
    }

    public static InstanceIdentifierTypeBuilder newInstanceIdentifierBuilder(final SchemaPath path) {
        return newInstanceIdentifierBuilder(BaseInstanceIdentifierType.INSTANCE, path);
    }

    public static InstanceIdentifierTypeBuilder newInstanceIdentifierBuilder(final InstanceIdentifierTypeDefinition baseType, final SchemaPath path) {
        return new InstanceIdentifierTypeBuilder(baseType, path);
    }

    public static TypeBuilder<LeafrefTypeDefinition> newLeafrefBuilder(final LeafrefTypeDefinition baseType, final SchemaPath path) {
        return new AbstractRestrictedTypeBuilder<LeafrefTypeDefinition>(baseType, path) {
            @Override
            LeafrefTypeDefinition buildType() {
                return new RestrictedLeafrefType(getBaseType(), getPath(), getUnknownSchemaNodes());
            }
        };
    }

    public static StringTypeBuilder newStringBuilder(final SchemaPath path) {
        return newStringBuilder(BaseStringType.INSTANCE, path);
    }

    public static StringTypeBuilder newStringBuilder(final StringTypeDefinition baseType, final SchemaPath path) {
        return new StringTypeBuilder(baseType, path);
    }

    public static TypeBuilder<UnionTypeDefinition> newUnionBuilder(final UnionTypeDefinition baseType, final SchemaPath path) {
        return new AbstractRestrictedTypeBuilder<UnionTypeDefinition>(baseType, path) {
            @Override
            UnionTypeDefinition buildType() {
                return new RestrictedUnionType(getBaseType(), getPath(), getUnknownSchemaNodes());
            }
        };
    }

    public static BitsTypeBuilder newBitsBuilder(final SchemaPath path) {
        return new BitsTypeBuilder(path);
    }

    public static DecimalTypeBuilder newDecima64Builder(final SchemaPath path) {
        return new DecimalTypeBuilder(path);
    }

    public static EnumerationTypeBuilder newEnumerationBuilder(final SchemaPath path) {
        return new EnumerationTypeBuilder(path);
    }

    public static IdentityrefTypeBuilder newIdentityrefBuilder(final SchemaPath path) {
        return new IdentityrefTypeBuilder(path);
    }

    public static LeafrefTypeBuilder newLeafrefBuilder(final SchemaPath path) {
        return new LeafrefTypeBuilder(path);
    }

    public static UnionTypeBuilder newUnionBuilder(final SchemaPath path) {
        return new UnionTypeBuilder(path);
    }
}
