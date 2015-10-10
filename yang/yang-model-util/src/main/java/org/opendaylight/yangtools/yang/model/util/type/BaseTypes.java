/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
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
 * The following types have their base type fully specified and are exposed as appropriate TypeDefinition sub-interfaces:
 * <ul>
 *     <li>boolean</li>
 *     <li>empty</li>
 *     <li>binary</li>
 *     <li>int{8,16,32,64}</li>
 *     <li>string</li>
 *     <li>uint{8,16,32,64}</li>
 * </ul>
 *
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

    public static IntegerTypeDefinition int16Type() {
        return BaseInt16Type.INSTANCE;
    }

    public static IntegerTypeDefinition int32Type() {
        return BaseInt32Type.INSTANCE;
    }

    public static IntegerTypeDefinition int64Type() {
        return BaseInt64Type.INSTANCE;
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

    public static UnsignedIntegerTypeDefinition uint16Type() {
        return BaseUint16Type.INSTANCE;
    }

    public static UnsignedIntegerTypeDefinition uint32Type() {
        return BaseUint32Type.INSTANCE;
    }

    public static UnsignedIntegerTypeDefinition uint64Type() {
        return BaseUint64Type.INSTANCE;
    }
}
