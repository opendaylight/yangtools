/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.test.codecs;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;

import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.codec.Int16Codec;
import org.opendaylight.yangtools.yang.data.api.codec.Int32Codec;
import org.opendaylight.yangtools.yang.data.api.codec.Int64Codec;
import org.opendaylight.yangtools.yang.data.api.codec.Int8Codec;
import org.opendaylight.yangtools.yang.data.api.codec.Uint16Codec;
import org.opendaylight.yangtools.yang.data.api.codec.Uint32Codec;
import org.opendaylight.yangtools.yang.data.api.codec.Uint64Codec;
import org.opendaylight.yangtools.yang.data.api.codec.Uint8Codec;
import org.opendaylight.yangtools.yang.model.util.Int16;
import org.opendaylight.yangtools.yang.model.util.Int32;
import org.opendaylight.yangtools.yang.model.util.Int64;
import org.opendaylight.yangtools.yang.model.util.Int8;
import org.opendaylight.yangtools.yang.model.util.Uint16;
import org.opendaylight.yangtools.yang.model.util.Uint32;
import org.opendaylight.yangtools.yang.model.util.Uint64;
import org.opendaylight.yangtools.yang.model.util.Uint8;

import static org.opendaylight.yangtools.yang.data.impl.test.codecs.TypeDefinitionAwareCodecTest.getCodec;
import static org.opendaylight.yangtools.yang.data.impl.test.codecs.TypeDefinitionAwareCodecTest.deserializeWithExpectedIllegalArgEx;

public class IntCodecStringTest {

    @SuppressWarnings({ "unchecked" })
    @Test
    public void uint8CodecTest() {
        final String hexa = "0x40";
        final String octal = "0100";
        final String integer = "64";

        Uint8Codec<String> codec = getCodec(Uint8.getInstance(), Uint8Codec.class);
        assertEquals(codec.deserialize(hexa), Short.valueOf("040", 16));
        assertEquals(codec.deserialize(octal), Short.valueOf(octal, 8));
        assertEquals(codec.deserialize(integer), Short.valueOf(integer, 10));
        assertEquals(codec.deserialize("0"), Short.valueOf("0", 10));

        deserializeWithExpectedIllegalArgEx(codec, "1o");
        deserializeWithExpectedIllegalArgEx(codec, "");
        deserializeWithExpectedIllegalArgEx(codec, null);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void uint16CodecTest() {
        final String hexa = "0X45c";
        final String octal = "02134";
        final String integer = "1116";

        Uint16Codec<String> codec = getCodec(Uint16.getInstance(), Uint16Codec.class);
        assertEquals(codec.deserialize(hexa), Integer.valueOf("045c", 16));
        assertEquals(codec.deserialize(octal), Integer.valueOf(octal, 8));
        assertEquals(codec.deserialize(integer), Integer.valueOf(integer, 10));

        deserializeWithExpectedIllegalArgEx(codec, "1o");
        deserializeWithExpectedIllegalArgEx(codec, "");
        deserializeWithExpectedIllegalArgEx(codec, null);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void uint32CodecTest() {
        final String hexa = "0x45FFFCDE";
        final String octal = "010577776336";
        final String integer = "1174404318";

        Uint32Codec<String> codec = getCodec(Uint32.getInstance(), Uint32Codec.class);
        assertEquals(codec.deserialize(hexa), Long.valueOf("45FFFCDE", 16));
        assertEquals(codec.deserialize(octal), Long.valueOf(octal, 8));
        assertEquals(codec.deserialize(integer), Long.valueOf(integer, 10));

        deserializeWithExpectedIllegalArgEx(codec, "1o");
        deserializeWithExpectedIllegalArgEx(codec, "");
        deserializeWithExpectedIllegalArgEx(codec, null);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void uint64CodecTest() {
        final String hexa = "0X75EDC78edCBA";
        final String octal = "03536670743556272";
        final String integer = "129664115727546";

        Uint64Codec<String> codec = getCodec(Uint64.getInstance(), Uint64Codec.class);
        assertEquals(codec.deserialize(hexa),
                new BigInteger("75EDC78edCBA", 16));
        assertEquals(codec.deserialize(octal), new BigInteger(octal, 8));
        assertEquals(codec.deserialize(integer), new BigInteger(integer, 10));

        deserializeWithExpectedIllegalArgEx(codec, "12345o");
        deserializeWithExpectedIllegalArgEx(codec, "");
        deserializeWithExpectedIllegalArgEx(codec, null);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void int8CodecTest() {
        final String hexa = "0x40";
        final String negHexa = "-0x40";
        final String octal = "+0100";
        final String negOctal = "-0100";
        final String integer = "64";
        final String negInteger = "-64";

        Int8Codec<String> codec = getCodec(Int8.getInstance(), Int8Codec.class);
        assertEquals(codec.deserialize(hexa), Byte.valueOf("040", 16));
        assertEquals(codec.deserialize(negHexa), Byte.valueOf("-040", 16));
        assertEquals(codec.deserialize(octal), Byte.valueOf(octal, 8));
        assertEquals(codec.deserialize(negOctal), Byte.valueOf(negOctal, 8));
        assertEquals(codec.deserialize(integer), Byte.valueOf(integer, 10));
        assertEquals(codec.deserialize(negInteger), Byte.valueOf(negInteger, 10));

        deserializeWithExpectedIllegalArgEx(codec, "1o");
        deserializeWithExpectedIllegalArgEx(codec, "");
        deserializeWithExpectedIllegalArgEx(codec, null);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void int16CodecTest() {
        final String hexa = "+0X45c";
        final String negHexa = "-0X45c";
        final String octal = "02134";
        final String negOctal = "-02134";
        final String integer = "+1116";
        final String negInteger = "-1116";

        Int16Codec<String> codec = getCodec(Int16.getInstance(), Int16Codec.class);
        assertEquals(codec.deserialize(hexa), Short.valueOf("+045c", 16));
        assertEquals(codec.deserialize(negHexa), Short.valueOf("-045c", 16));
        assertEquals(codec.deserialize(octal), Short.valueOf(octal, 8));
        assertEquals(codec.deserialize(negOctal), Short.valueOf(negOctal, 8));
        assertEquals(codec.deserialize(integer), Short.valueOf(integer, 10));
        assertEquals(codec.deserialize(negInteger),
                Short.valueOf(negInteger, 10));

        deserializeWithExpectedIllegalArgEx(codec, "1o");
        deserializeWithExpectedIllegalArgEx(codec, "");
        deserializeWithExpectedIllegalArgEx(codec, null);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void int32CodecTest() {
        final String hexa = "0x45FFFCDE";
        final String negHexa = "-0x45FFFCDE";
        final String octal = "010577776336";
        final String negOctal = "-010577776336";
        final String integer = "1174404318";
        final String negInteger = "-1174404318";

        Int32Codec<String> codec = getCodec(Int32.getInstance(), Int32Codec.class);
        assertEquals(codec.deserialize(hexa), Integer.valueOf("+045FFFCDE", 16));
        assertEquals(codec.deserialize(negHexa),
                Integer.valueOf("-045FFFCDE", 16));
        assertEquals(codec.deserialize(octal), Integer.valueOf(octal, 8));
        assertEquals(codec.deserialize(negOctal), Integer.valueOf(negOctal, 8));
        assertEquals(codec.deserialize(integer), Integer.valueOf(integer, 10));
        assertEquals(codec.deserialize(negInteger),
                Integer.valueOf(negInteger, 10));

        deserializeWithExpectedIllegalArgEx(codec, "1o");
        deserializeWithExpectedIllegalArgEx(codec, "");
        deserializeWithExpectedIllegalArgEx(codec, null);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void int64CodecTest() {
        final String hexa = "0X75EDC78edCBA";
        final String negHexa = "-0X75EDC78edCBA";
        final String octal = "+03536670743556272";
        final String negOctal = "-03536670743556272";
        final String integer = "+129664115727546";
        final String negInteger = "-129664115727546";

        Int64Codec<String> codec = getCodec(Int64.getInstance(), Int64Codec.class);
        assertEquals(codec.deserialize(hexa), Long.valueOf("075EDC78edCBA", 16));
        assertEquals(codec.deserialize(negHexa),
                Long.valueOf("-075EDC78edCBA", 16));
        assertEquals(codec.deserialize(octal), Long.valueOf(octal, 8));
        assertEquals(codec.deserialize(negOctal), Long.valueOf(negOctal, 8));
        assertEquals(codec.deserialize(integer), Long.valueOf(integer, 10));
        assertEquals(codec.deserialize(negInteger), Long.valueOf(negInteger, 10));

        deserializeWithExpectedIllegalArgEx(codec, "1234o");
        deserializeWithExpectedIllegalArgEx(codec, "");
        deserializeWithExpectedIllegalArgEx(codec, null);
    }
}
