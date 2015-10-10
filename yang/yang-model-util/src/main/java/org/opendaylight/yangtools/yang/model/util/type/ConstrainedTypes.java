/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public final class ConstrainedTypes {
    private ConstrainedTypes() {
        throw new UnsupportedOperationException();
    }

    /*
     * FIXME: the types are divided to following groups:
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
     *    decimal64
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
     *
     * This should guide a major simplification of this package.
     */

    public static BinaryConstrainedTypeBuilder newBinaryBuilder(final SchemaPath path) {
        return BinaryBaseType.INSTANCE.newConstrainedTypeBuilder(path);
    }

    public static BooleanConstrainedTypeBuilder newBooleanBuilder(final SchemaPath path) {
        return BooleanBaseType.INSTANCE.newConstrainedTypeBuilder(path);
    }

    public static BitsBaseTypeBuilder newBitsBuilder(final SchemaPath path) {
        return new BitsBaseTypeBuilder(path);
    }

    public static DecimalBaseTypeBuilder newDecima64Builder(final SchemaPath path) {
        return new DecimalBaseTypeBuilder(path);
    }

    public static EmptyConstrainedTypeBuilder newEmptyBuilder(final SchemaPath path) {
        return EmptyBaseType.INSTANCE.newConstrainedTypeBuilder(path);
    }

    public static EnumerationBaseTypeBuilder newEnumerationBuilder(final SchemaPath path) {
        return new EnumerationBaseTypeBuilder(path);
    }

    public static IdentityrefBaseTypeBuilder newIdentityrefBuilder(final SchemaPath path) {
        return new IdentityrefBaseTypeBuilder(path);
    }

    public static InstanceIdentifierBaseTypeBuilder newInstanceIdentifierBuilder(final SchemaPath path) {
        return new InstanceIdentifierBaseTypeBuilder(path);
    }

    public static IntegerConstrainedTypeBuilder newInt8Builder(final SchemaPath path) {
        return Int8BaseType.INSTANCE.newConstrainedTypeBuilder(path);
    }

    public static IntegerConstrainedTypeBuilder newInt16Builder(final SchemaPath path) {
        return Int16BaseType.INSTANCE.newConstrainedTypeBuilder(path);
    }

    public static IntegerConstrainedTypeBuilder newInt32Builder(final SchemaPath path) {
        return Int32BaseType.INSTANCE.newConstrainedTypeBuilder(path);
    }

    public static IntegerConstrainedTypeBuilder newInt64Builder(final SchemaPath path) {
        return Int64BaseType.INSTANCE.newConstrainedTypeBuilder(path);
    }

    public static LeafrefBaseTypeBuilder newLeafrefBuilder(final SchemaPath path) {
        return new LeafrefBaseTypeBuilder(path);
    }

    public static UnsignedConstrainedTypeBuilder newUint8Builder(final SchemaPath path) {
        return Uint8BaseType.INSTANCE.newConstrainedTypeBuilder(path);
    }

    public static UnsignedConstrainedTypeBuilder newUint16Builder(final SchemaPath path) {
        return Uint16BaseType.INSTANCE.newConstrainedTypeBuilder(path);
    }

    public static UnsignedConstrainedTypeBuilder newUint32Builder(final SchemaPath path) {
        return Uint32BaseType.INSTANCE.newConstrainedTypeBuilder(path);
    }

    public static UnsignedConstrainedTypeBuilder newUint64Builder(final SchemaPath path) {
        return Uint64BaseType.INSTANCE.newConstrainedTypeBuilder(path);
    }

    public static UnionBaseTypeBuilder newUnionBuilder(final SchemaPath path) {
        return new UnionBaseTypeBuilder(path);
    }
}
