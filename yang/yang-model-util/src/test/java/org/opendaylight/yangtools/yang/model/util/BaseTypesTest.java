/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Optional;
import org.junit.Test;

public class BaseTypesTest {

    @Test
    public void canCreateBaseTypes() {
        final Optional<?> int8 = BaseTypes.defaultBaseTypeFor("int8");
        assertEquals(Optional.of(Int8.getInstance()), int8);

        final Optional<?> int16 = BaseTypes.defaultBaseTypeFor("int16");
        assertEquals(Optional.of(Int16.getInstance()), int16);

        final Optional<?> int32 = BaseTypes.defaultBaseTypeFor("int32");
        assertEquals(Optional.of(Int32.getInstance()), int32);

        final Optional<?> int64 = BaseTypes.defaultBaseTypeFor("int64");
        assertEquals(Optional.of(Int64.getInstance()), int64);

        final Optional<?> int128 = BaseTypes.defaultBaseTypeFor("int128");
        assertEquals("wrong type", Optional.absent(), int128);

        final Optional<?> uint8 = BaseTypes.defaultBaseTypeFor("uint8");
        assertEquals(Optional.of(Uint8.getInstance()), uint8);

        final Optional<?> uint16 = BaseTypes.defaultBaseTypeFor("uint16");
        assertEquals(Optional.of(Uint16.getInstance()), uint16);

        final Optional<?> uint32 =  BaseTypes.defaultBaseTypeFor("uint32");
        assertEquals(Optional.of(Uint32.getInstance()), uint32);

        final Optional<?> uint64 = BaseTypes.defaultBaseTypeFor("uint64");
        assertEquals(Optional.of(Uint64.getInstance()), uint64);

        final Optional<?> uint128 = BaseTypes.defaultBaseTypeFor("uint128");
        assertEquals("wrong type", Optional.absent(), uint128);

        final Optional<?> stringType = BaseTypes.defaultBaseTypeFor("string");
        assertEquals(Optional.of(StringType.getInstance()), stringType);

        final Optional<?> binaryType = BaseTypes.defaultBaseTypeFor("binary");
        assertEquals(Optional.of(BinaryType.getInstance()), binaryType);

        final Optional<?> booleanType = BaseTypes.defaultBaseTypeFor("boolean");
        assertEquals(Optional.of(BooleanType.getInstance()), booleanType);

        final Optional<?> emptyType = BaseTypes.defaultBaseTypeFor("empty");
        assertEquals(Optional.of(EmptyType.getInstance()), emptyType);

        final Optional<?> instance_identifier = BaseTypes.defaultBaseTypeFor("instance_identifier");
        assertEquals(Optional.absent(), instance_identifier);

        final Optional<?> whatever = BaseTypes.defaultBaseTypeFor("whatever");
        assertEquals("wrong type", Optional.absent(), whatever);

        assertFalse("whatever is not build-in type", BaseTypes.isYangBuildInType("whatever"));
        assertTrue("int8 is build-in type", BaseTypes.isYangBuildInType("int8"));
    }

    @Test
    public void testMethodsOfInt64() {
        final Int64 int64 = Int64.getInstance();

        assertNotNull("Object 'int64' shouldn't be null.", int64);
        assertNull("Default value of 'int64' should be null.", int64.getDefaultValue());
        assertNotNull("String representation of 'int64' shouldn't be null.", int64.toString());
    }

    @Test
    public void testMethodsOfInt32() {
        final Int32 int32 = Int32.getInstance();

        assertNotNull("Object 'int32' shouldn't be null.", int32);
        assertNull("Default value of 'int32' should be null.", int32.getDefaultValue());
        assertNotNull("String representation of 'int32' shouldn't be null.", int32.toString());
    }

    @Test
    public void testMethodsOfInt16() {
        final Int16 int16 = Int16.getInstance();

        assertNotNull("Object 'int16' shouldn't be null.", int16);
        assertNull("Default value of 'int16' should be null.", int16.getDefaultValue());
        assertNotNull("String representation of 'int16' shouldn't be null.", int16.toString());
    }

    @Test
    public void testMethodsOfInt8() {
        final Int8 int8 = Int8.getInstance();

        assertNotNull("Object 'int8' shouldn't be null.", int8);
        assertNull("Default value of 'int8' should be null.", int8.getDefaultValue());
        assertNotNull("String representation of 'int8' shouldn't be null.", int8.toString());
    }

    @Test
    public void testMethodsOfUInt64() {
        final Uint64 uint64 = Uint64.getInstance();

        assertNotNull("Object 'uint64' shouldn't be null.", uint64);
        assertNull("Default value of 'uint64' should be null.", uint64.getDefaultValue());
        assertNotNull("String representation of 'uint64' shouldn't be null.", uint64.toString());
    }

    @Test
    public void testMethodsOfUInt32() {
        final Uint32 uint32 = Uint32.getInstance();

        assertNotNull("Object 'uint32' shouldn't be null.", uint32);
        assertNull("Default value of 'uint32' should be null.", uint32.getDefaultValue());
        assertNotNull("String representation of 'uint32' shouldn't be null.", uint32.toString());
    }

    @Test
    public void testMethodsOfUInt16() {
        final Uint16 uint16 = Uint16.getInstance();

        assertNotNull("Object 'uint16' shouldn't be null.", uint16);
        assertNull("Default value of 'uint16' should be null.", uint16.getDefaultValue());
        assertNotNull("String representation of 'uint16' shouldn't be null.", uint16.toString());
    }

    @Test
    public void testMethodsOfUInt8() {
        final Uint8 uint8 = Uint8.getInstance();

        assertNotNull("Object 'uint8' shouldn't be null.", uint8);
        assertNull("Default value of 'uint8' should be null.", uint8.getDefaultValue());
        assertNotNull("String representation of 'uint8' shouldn't be null.", uint8.toString());
    }
}