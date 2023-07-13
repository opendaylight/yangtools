/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.data.api.codec.Int8Codec;
import org.opendaylight.yangtools.yang.model.ri.type.BaseTypes;

/**
 * Unit tests for Int8CodecString.
 *
 * @author Thomas Pantelis
 */
class Int8CodecStringTest {
    @SuppressWarnings("unchecked")
    @Test
    void testSerialize() {
        final var codec = TypeDefinitionAwareCodecTestHelper.getCodec(BaseTypes.int8Type(), Int8Codec.class);

        assertEquals("10", codec.serialize(Byte.valueOf((byte) 10)), "serialize");
    }

    @SuppressWarnings("unchecked")
    @Test
    void testDeserialize() {
        final var hexa = "0x40";
        final var negHexa = "-0x40";
        final var octal = "+0100";
        final var negOctal = "-0100";
        final var integer = "64";
        final var negInteger = "-64";

        final var codec = TypeDefinitionAwareCodecTestHelper.getCodec(BaseTypes.int8Type(), Int8Codec.class);

        assertEquals(codec.deserialize(hexa), Byte.valueOf("040", 16), "deserialize");
        assertEquals(codec.deserialize(negHexa), Byte.valueOf("-040", 16), "deserialize");
        assertEquals(codec.deserialize(octal), Byte.valueOf(octal, 8), "deserialize");
        assertEquals(codec.deserialize(negOctal), Byte.valueOf(negOctal, 8), "deserialize");
        assertEquals(codec.deserialize(integer), Byte.valueOf(integer, 10), "deserialize");
        assertEquals(codec.deserialize(negInteger), Byte.valueOf(negInteger, 10), "deserialize");

        TypeDefinitionAwareCodecTestHelper.deserializeWithExpectedIllegalArgEx(codec, "1o");
        TypeDefinitionAwareCodecTestHelper.deserializeWithExpectedIllegalArgEx(codec, "");
    }
}
