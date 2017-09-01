/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.codecs;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.codec.Int8Codec;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;

/**
 * Unit tests for Int8CodecString.
 *
 * @author Thomas Pantelis
 */
public class Int8CodecStringTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testSerialize() {
        Int8Codec<String> codec = TypeDefinitionAwareCodecTestHelper.getCodec(BaseTypes.int8Type(), Int8Codec.class);

        assertEquals("serialize", "10", codec.serialize(Byte.valueOf((byte) 10)));
        assertEquals("serialize", "", codec.serialize(null));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDeserialize() {
        final String hexa = "0x40";
        final String negHexa = "-0x40";
        final String octal = "+0100";
        final String negOctal = "-0100";
        final String integer = "64";
        final String negInteger = "-64";

        Int8Codec<String> codec = TypeDefinitionAwareCodecTestHelper.getCodec(BaseTypes.int8Type(), Int8Codec.class);

        assertEquals("deserialize", codec.deserialize(hexa), Byte.valueOf("040", 16));
        assertEquals("deserialize", codec.deserialize(negHexa), Byte.valueOf("-040", 16));
        assertEquals("deserialize", codec.deserialize(octal), Byte.valueOf(octal, 8));
        assertEquals("deserialize", codec.deserialize(negOctal), Byte.valueOf(negOctal, 8));
        assertEquals("deserialize", codec.deserialize(integer), Byte.valueOf(integer, 10));
        assertEquals("deserialize", codec.deserialize(negInteger), Byte.valueOf(negInteger, 10));

        TypeDefinitionAwareCodecTestHelper.deserializeWithExpectedIllegalArgEx(codec, "1o");
        TypeDefinitionAwareCodecTestHelper.deserializeWithExpectedIllegalArgEx(codec, "");
        TypeDefinitionAwareCodecTestHelper.deserializeWithExpectedIllegalArgEx(codec, null);
    }
}
