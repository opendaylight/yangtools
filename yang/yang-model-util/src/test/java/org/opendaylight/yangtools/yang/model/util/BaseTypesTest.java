package org.opendaylight.yangtools.yang.model.util;

import com.google.common.base.Optional;
import org.junit.Test;

import static org.junit.Assert.*;

public class BaseTypesTest {

    @Test
    public void canCreateBaseTypes() {
        Optional<?> int8 = BaseTypes.defaultBaseTypeFor("int8");
        assertEquals(Optional.of(Int8.getInstance()), int8);

        Optional<?> int16 = BaseTypes.defaultBaseTypeFor("int16");
        assertEquals(Optional.of(Int16.getInstance()), int16);

        Optional<?> int32 = BaseTypes.defaultBaseTypeFor("int32");
        assertEquals(Optional.of(Int32.getInstance()), int32);

        Optional<?> int64 = BaseTypes.defaultBaseTypeFor("int64");
        assertEquals(Optional.of(Int64.getInstance()), int64);

        Optional<?> int128 = BaseTypes.defaultBaseTypeFor("int128");
        assertEquals("wrong type", Optional.absent(), int128);

        Optional<?> uint8 = BaseTypes.defaultBaseTypeFor("uint8");
        assertEquals(Optional.of(Uint8.getInstance()), uint8);

        Optional<?> uint16 = BaseTypes.defaultBaseTypeFor("uint16");
        assertEquals(Optional.of(Uint16.getInstance()), uint16);

        Optional<?> uint32 =  BaseTypes.defaultBaseTypeFor("uint32");
        assertEquals(Optional.of(Uint32.getInstance()), uint32);

        Optional<?> uint64 = BaseTypes.defaultBaseTypeFor("uint64");
        assertEquals(Optional.of(Uint64.getInstance()), uint64);

        Optional<?> uint128 = BaseTypes.defaultBaseTypeFor("uint128");
        assertEquals("wrong type", Optional.absent(), uint128);

        Optional<?> stringType = BaseTypes.defaultBaseTypeFor("string");
        assertEquals(Optional.of(StringType.getInstance()), stringType);

        Optional<?> binaryType = BaseTypes.defaultBaseTypeFor("binary");
        assertEquals(Optional.of(BinaryType.getInstance()), binaryType);

        Optional<?> booleanType = BaseTypes.defaultBaseTypeFor("boolean");
        assertEquals(Optional.of(BooleanType.getInstance()), booleanType);

        Optional<?> emptyType = BaseTypes.defaultBaseTypeFor("empty");
        assertEquals(Optional.of(EmptyType.getInstance()), emptyType);

        Optional<?> instance_identifier = BaseTypes.defaultBaseTypeFor("instance_identifier");
        assertEquals(Optional.absent(), instance_identifier);

        Optional<?> whatever = BaseTypes.defaultBaseTypeFor("whatever");
        assertEquals("wrong type", Optional.absent(), whatever);

        assertFalse("whatever is not build-in type", BaseTypes.isYangBuildInType("whatever"));
        assertTrue("int8 is build-in type", BaseTypes.isYangBuildInType("int8"));
    }

}