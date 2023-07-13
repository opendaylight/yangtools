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
import org.opendaylight.yangtools.yang.data.api.codec.Int32Codec;
import org.opendaylight.yangtools.yang.model.ri.type.BaseTypes;

/**
 * Unit tests for Int32CodecString.
 *
 * @author Thomas Pantelis
 */
class Int32CodecStringTest {
    @SuppressWarnings("unchecked")
    @Test
    void testSerialize() {
        final var codec = TypeDefinitionAwareCodecTestHelper.getCodec(BaseTypes.int32Type(), Int32Codec.class);
        assertEquals("10", codec.serialize(Integer.valueOf(10)), "serialize");
    }

    @SuppressWarnings("unchecked")
    @Test
    void testDeserialize() {
        final var hexa = "0x45FFFCDE";
        final var negHexa = "-0x45FFFCDE";
        final var octal = "010577776336";
        final var negOctal = "-010577776336";
        final var integer = "1174404318";
        final var negInteger = "-1174404318";

        final var codec = TypeDefinitionAwareCodecTestHelper.getCodec(BaseTypes.int32Type(), Int32Codec.class);

        assertEquals(codec.deserialize(hexa), Integer.valueOf("+045FFFCDE", 16), "deserialize");
        assertEquals(codec.deserialize(negHexa), Integer.valueOf("-045FFFCDE", 16), "deserialize");
        assertEquals(codec.deserialize(octal), Integer.valueOf(octal, 8), "deserialize");
        assertEquals(codec.deserialize(negOctal), Integer.valueOf(negOctal, 8), "deserialize");
        assertEquals(codec.deserialize(integer), Integer.valueOf(integer, 10), "deserialize");
        assertEquals(codec.deserialize(negInteger), Integer.valueOf(negInteger, 10), "deserialize");

        TypeDefinitionAwareCodecTestHelper.deserializeWithExpectedIllegalArgEx(codec, "1o");
        TypeDefinitionAwareCodecTestHelper.deserializeWithExpectedIllegalArgEx(codec, "");
    }
}
