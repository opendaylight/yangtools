/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int8TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint8TypeDefinition;

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
        // Hidden on purpose
    }

    public static @NonNull BinaryTypeDefinition binaryType() {
        return BaseBinaryType.INSTANCE;
    }

    public static @NonNull BitsTypeBuilder bitsTypeBuilder(final SchemaPath path) {
        return new BitsTypeBuilder(path);
    }

    public static @NonNull BooleanTypeDefinition booleanType() {
        return BaseBooleanType.INSTANCE;
    }

    public static @NonNull DecimalTypeBuilder decimalTypeBuilder(final SchemaPath path) {
        return new DecimalTypeBuilder(path);
    }

    public static @NonNull EmptyTypeDefinition emptyType() {
        return BaseEmptyType.INSTANCE;
    }

    public static @NonNull EnumerationTypeBuilder enumerationTypeBuilder(final SchemaPath path) {
        return new EnumerationTypeBuilder(path);
    }

    public static @NonNull IdentityrefTypeBuilder identityrefTypeBuilder(final SchemaPath path) {
        return new IdentityrefTypeBuilder(path);
    }

    public static @NonNull InstanceIdentifierTypeDefinition instanceIdentifierType() {
        return BaseInstanceIdentifierType.INSTANCE;
    }

    public static @NonNull Int8TypeDefinition int8Type() {
        return BaseInt8Type.INSTANCE;
    }

    public static @NonNull Int16TypeDefinition int16Type() {
        return BaseInt16Type.INSTANCE;
    }

    public static @NonNull Int32TypeDefinition int32Type() {
        return BaseInt32Type.INSTANCE;
    }

    public static @NonNull Int64TypeDefinition int64Type() {
        return BaseInt64Type.INSTANCE;
    }

    public static @NonNull LeafrefTypeBuilder leafrefTypeBuilder(final SchemaPath path) {
        return new LeafrefTypeBuilder(path);
    }

    public static @NonNull StringTypeDefinition stringType() {
        return BaseStringType.INSTANCE;
    }

    public static UnionTypeBuilder unionTypeBuilder(final SchemaPath path) {
        return new UnionTypeBuilder(path);
    }

    public static @NonNull Uint8TypeDefinition uint8Type() {
        return BaseUint8Type.INSTANCE;
    }

    public static @NonNull Uint16TypeDefinition uint16Type() {
        return BaseUint16Type.INSTANCE;
    }

    public static @NonNull Uint32TypeDefinition uint32Type() {
        return BaseUint32Type.INSTANCE;
    }

    public static @NonNull Uint64TypeDefinition uint64Type() {
        return BaseUint64Type.INSTANCE;
    }

    /**
     * Return the base type of a particular type. This method performs recursive lookup through the type's base type
     * until it finds the last element and returns it. If the argument is already the base type, it is returned as is.
     *
     * @param type Type for which to find the base type
     * @return Base type of specified type
     * @throws NullPointerException if type is null
     */
    public static @NonNull TypeDefinition<?> baseTypeOf(final @NonNull TypeDefinition<?> type) {
        TypeDefinition<?> ret = type;
        while (ret.getBaseType() != null) {
            ret = ret.getBaseType();
        }
        return ret;
    }
}
