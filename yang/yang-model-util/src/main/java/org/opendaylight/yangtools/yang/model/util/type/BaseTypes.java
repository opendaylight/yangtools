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
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;

/**
 * Utility access methods for creating and accessing YANG base type definitions. YANG types come in two basic variants,
 * depending on whether they fully define their base instance or model input is required to fully-form the type.
 *
 * <p>
 * The following types have their base type fully specified and are exposed as appropriate TypeDefinition
 * sub-interfaces:
 * <ul>
 *     <li>boolean</li>
 *     <li>empty</li>
 *     <li>binary</li>
 *     <li>int{8,16,32,64}</li>
 *     <li>string</li>
 *     <li>uint{8,16,32,64}</li>
 * </ul>
 *
 * <p>
 * The following types require additional specification in the model and are exposed by means of a specialized
 * {@link TypeBuilder}s for each type:
 * <ul>
 *    <li>decimal64</li>
 *    <li>instance-identifier</li>
 *    <li>enumeration</li>
 *    <li>identityref</li>
 *    <li>leafref</li>
 *    <li>union</li>
 * </ul>
 */
@Beta
public final class BaseTypes {
    private BaseTypes() {
        throw new UnsupportedOperationException();
    }

    public static BinaryTypeDefinition binaryType() {
        return BaseBinaryType.INSTANCE;
    }

    public static BitsTypeBuilder bitsTypeBuilder(final SchemaPath path) {
        return new BitsTypeBuilder(path);
    }

    public static BooleanTypeDefinition booleanType() {
        return BaseBooleanType.INSTANCE;
    }

    public static DecimalTypeBuilder decimalTypeBuilder(final SchemaPath path) {
        return new DecimalTypeBuilder(path);
    }

    public static EmptyTypeDefinition emptyType() {
        return BaseEmptyType.INSTANCE;
    }

    public static EnumerationTypeBuilder enumerationTypeBuilder(final SchemaPath path) {
        return new EnumerationTypeBuilder(path);
    }

    public static IdentityrefTypeBuilder identityrefTypeBuilder(final SchemaPath path) {
        return new IdentityrefTypeBuilder(path);
    }

    public static InstanceIdentifierTypeDefinition instanceIdentifierType() {
        return BaseInstanceIdentifierType.INSTANCE;
    }

    public static IntegerTypeDefinition int8Type() {
        return BaseInt8Type.INSTANCE;
    }

    public static boolean isInt8(final TypeDefinition<?> type) {
        return BaseInt8Type.INSTANCE.getPath().equals(type.getPath());
    }

    public static IntegerTypeDefinition int16Type() {
        return BaseInt16Type.INSTANCE;
    }

    public static boolean isInt16(final TypeDefinition<?> type) {
        return BaseInt16Type.INSTANCE.getPath().equals(type.getPath());
    }

    public static IntegerTypeDefinition int32Type() {
        return BaseInt32Type.INSTANCE;
    }

    public static boolean isInt32(final TypeDefinition<?> type) {
        return BaseInt32Type.INSTANCE.getPath().equals(type.getPath());
    }

    public static IntegerTypeDefinition int64Type() {
        return BaseInt64Type.INSTANCE;
    }

    public static boolean isInt64(final TypeDefinition<?> type) {
        return BaseInt64Type.INSTANCE.getPath().equals(type.getPath());
    }

    public static LeafrefTypeBuilder leafrefTypeBuilder(final SchemaPath path) {
        return new LeafrefTypeBuilder(path);
    }

    public static StringTypeDefinition stringType() {
        return BaseStringType.INSTANCE;
    }

    public static UnionTypeBuilder unionTypeBuilder(final SchemaPath path) {
        return new UnionTypeBuilder(path);
    }

    public static UnsignedIntegerTypeDefinition uint8Type() {
        return BaseUint8Type.INSTANCE;
    }

    /**
     * Check if a particular type is the base type for uint8. Unlike {@link DerivedTypes#isUint8(TypeDefinition)},
     * this method does not perform recursive base type lookup.
     *
     * @param type The type to check
     * @return If the type corresponds to the base uint8 type.
     * @throws NullPointerException if type is null
     */
    public static boolean isUint8(@Nonnull final TypeDefinition<?> type) {
        return BaseUint8Type.INSTANCE.getPath().equals(type.getPath());
    }

    public static UnsignedIntegerTypeDefinition uint16Type() {
        return BaseUint16Type.INSTANCE;
    }

    /**
     * Check if a particular type is the base type for uint16. Unlike {@link DerivedTypes#isUint16(TypeDefinition)},
     * this method does not perform recursive base type lookup.
     *
     * @param type The type to check
     * @return If the type corresponds to the base uint16 type.
     * @throws NullPointerException if type is null
     */
    public static boolean isUint16(@Nonnull final TypeDefinition<?> type) {
        return BaseUint16Type.INSTANCE.getPath().equals(type.getPath());
    }

    public static UnsignedIntegerTypeDefinition uint32Type() {
        return BaseUint32Type.INSTANCE;
    }

    /**
     * Check if a particular type is the base type for uint32. Unlike {@link DerivedTypes#isUint32(TypeDefinition)},
     * this method does not perform recursive base type lookup.
     *
     * @param type The type to check
     * @return If the type corresponds to the base uint32 type.
     * @throws NullPointerException if type is null
     */
    public static boolean isUint32(@Nonnull final TypeDefinition<?> type) {
        return BaseUint32Type.INSTANCE.getPath().equals(type.getPath());
    }

    public static UnsignedIntegerTypeDefinition uint64Type() {
        return BaseUint64Type.INSTANCE;
    }

    /**
     * Check if a particular type is the base type for uint64. Unlike {@link DerivedTypes#isUint64(TypeDefinition)},
     * this method does not perform recursive base type lookup.
     *
     * @param type The type to check
     * @return If the type corresponds to the base uint64 type.
     * @throws NullPointerException if type is null
     */
    public static boolean isUint64(@Nonnull final TypeDefinition<?> type) {
        return BaseUint64Type.INSTANCE.getPath().equals(type.getPath());
    }

    /**
     * Return the base type of a particular type. This method performs recursive lookup through the type's base type
     * until it finds the last element and returns it. If the argument is already the base type, it is returned as is.
     *
     * @param type Type for which to find the base type
     * @return Base type of specified type
     * @throws NullPointerException if type is null
     */
    public static TypeDefinition<?> baseTypeOf(@Nonnull final TypeDefinition<?> type) {
        TypeDefinition<?> ret = type;
        while (ret.getBaseType() != null) {
            ret = ret.getBaseType();
        }
        return ret;
    }
}
