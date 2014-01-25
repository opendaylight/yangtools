package org.opendaylight.yangtools.yang.data.impl.test.codecs;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;

import org.junit.Test;
import org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodec;

public class IntCodecStringTest {

    @Test
    public void uint8CodecTest() {
        final String hexa = "0x40";
        final String octal = "0100";
        final String integer = "64";

        assertEquals(TypeDefinitionAwareCodec.UINT8_DEFAULT_CODEC.deserialize(hexa), Short.valueOf("040", 16));
        assertEquals(TypeDefinitionAwareCodec.UINT8_DEFAULT_CODEC.deserialize(octal), Short.valueOf(octal, 8));
        assertEquals(TypeDefinitionAwareCodec.UINT8_DEFAULT_CODEC.deserialize(integer), Short.valueOf(integer, 10));
        assertEquals(TypeDefinitionAwareCodec.UINT8_DEFAULT_CODEC.deserialize("0"), Short.valueOf("0", 10));
    }

    @Test
    public void uint16CodecTest() {
        final String hexa = "0X45c";
        final String octal = "02134";
        final String integer = "1116";

        assertEquals(TypeDefinitionAwareCodec.UINT16_DEFAULT_CODEC.deserialize(hexa), Integer.valueOf("045c", 16));
        assertEquals(TypeDefinitionAwareCodec.UINT16_DEFAULT_CODEC.deserialize(octal), Integer.valueOf(octal, 8));
        assertEquals(TypeDefinitionAwareCodec.UINT16_DEFAULT_CODEC.deserialize(integer), Integer.valueOf(integer, 10));
    }

    @Test
    public void uint32CodecTest() {
        final String hexa = "0x45FFFCDE";
        final String octal = "010577776336";
        final String integer = "1174404318";

        assertEquals(TypeDefinitionAwareCodec.UINT32_DEFAULT_CODEC.deserialize(hexa), Long.valueOf("45FFFCDE", 16));
        assertEquals(TypeDefinitionAwareCodec.UINT32_DEFAULT_CODEC.deserialize(octal), Long.valueOf(octal, 8));
        assertEquals(TypeDefinitionAwareCodec.UINT32_DEFAULT_CODEC.deserialize(integer), Long.valueOf(integer, 10));
    }

    @Test
    public void uint64CodecTest() {
        final String hexa = "0X75EDC78edCBA";
        final String octal = "03536670743556272";
        final String integer = "129664115727546";

        assertEquals(TypeDefinitionAwareCodec.UINT64_DEFAULT_CODEC.deserialize(hexa),
                new BigInteger("75EDC78edCBA", 16));
        assertEquals(TypeDefinitionAwareCodec.UINT64_DEFAULT_CODEC.deserialize(octal), new BigInteger(octal, 8));
        assertEquals(TypeDefinitionAwareCodec.UINT64_DEFAULT_CODEC.deserialize(integer), new BigInteger(integer, 10));
    }

    @Test
    public void int8CodecTest() {
        final String hexa = "0x40";
        final String negHexa = "-0x40";
        final String octal = "+0100";
        final String negOctal = "-0100";
        final String integer = "64";
        final String negInteger = "-64";

        assertEquals(TypeDefinitionAwareCodec.INT8_DEFAULT_CODEC.deserialize(hexa), Byte.valueOf("040", 16));
        assertEquals(TypeDefinitionAwareCodec.INT8_DEFAULT_CODEC.deserialize(negHexa), Byte.valueOf("-040", 16));
        assertEquals(TypeDefinitionAwareCodec.INT8_DEFAULT_CODEC.deserialize(octal), Byte.valueOf(octal, 8));
        assertEquals(TypeDefinitionAwareCodec.INT8_DEFAULT_CODEC.deserialize(negOctal), Byte.valueOf(negOctal, 8));
        assertEquals(TypeDefinitionAwareCodec.INT8_DEFAULT_CODEC.deserialize(integer), Byte.valueOf(integer, 10));
        assertEquals(TypeDefinitionAwareCodec.INT8_DEFAULT_CODEC.deserialize(negInteger), Byte.valueOf(negInteger, 10));
    }

    @Test
    public void int16CodecTest() {
        final String hexa = "+0X45c";
        final String negHexa = "-0X45c";
        final String octal = "02134";
        final String negOctal = "-02134";
        final String integer = "+1116";
        final String negInteger = "-1116";

        assertEquals(TypeDefinitionAwareCodec.INT16_DEFAULT_CODEC.deserialize(hexa), Short.valueOf("+045c", 16));
        assertEquals(TypeDefinitionAwareCodec.INT16_DEFAULT_CODEC.deserialize(negHexa), Short.valueOf("-045c", 16));
        assertEquals(TypeDefinitionAwareCodec.INT16_DEFAULT_CODEC.deserialize(octal), Short.valueOf(octal, 8));
        assertEquals(TypeDefinitionAwareCodec.INT16_DEFAULT_CODEC.deserialize(negOctal), Short.valueOf(negOctal, 8));
        assertEquals(TypeDefinitionAwareCodec.INT16_DEFAULT_CODEC.deserialize(integer), Short.valueOf(integer, 10));
        assertEquals(TypeDefinitionAwareCodec.INT16_DEFAULT_CODEC.deserialize(negInteger),
                Short.valueOf(negInteger, 10));
    }

    @Test
    public void int32CodecTest() {
        final String hexa = "0x45FFFCDE";
        final String negHexa = "-0x45FFFCDE";
        final String octal = "010577776336";
        final String negOctal = "-010577776336";
        final String integer = "1174404318";
        final String negInteger = "-1174404318";

        assertEquals(TypeDefinitionAwareCodec.INT32_DEFAULT_CODEC.deserialize(hexa), Integer.valueOf("+045FFFCDE", 16));
        assertEquals(TypeDefinitionAwareCodec.INT32_DEFAULT_CODEC.deserialize(negHexa),
                Integer.valueOf("-045FFFCDE", 16));
        assertEquals(TypeDefinitionAwareCodec.INT32_DEFAULT_CODEC.deserialize(octal), Integer.valueOf(octal, 8));
        assertEquals(TypeDefinitionAwareCodec.INT32_DEFAULT_CODEC.deserialize(negOctal), Integer.valueOf(negOctal, 8));
        assertEquals(TypeDefinitionAwareCodec.INT32_DEFAULT_CODEC.deserialize(integer), Integer.valueOf(integer, 10));
        assertEquals(TypeDefinitionAwareCodec.INT32_DEFAULT_CODEC.deserialize(negInteger),
                Integer.valueOf(negInteger, 10));
    }

    @Test
    public void int64CodecTest() {
        final String hexa = "0X75EDC78edCBA";
        final String negHexa = "-0X75EDC78edCBA";
        final String octal = "+03536670743556272";
        final String negOctal = "-03536670743556272";
        final String integer = "+129664115727546";
        final String negInteger = "-129664115727546";

        assertEquals(TypeDefinitionAwareCodec.INT64_DEFAULT_CODEC.deserialize(hexa), Long.valueOf("075EDC78edCBA", 16));
        assertEquals(TypeDefinitionAwareCodec.INT64_DEFAULT_CODEC.deserialize(negHexa),
                Long.valueOf("-075EDC78edCBA", 16));
        assertEquals(TypeDefinitionAwareCodec.INT64_DEFAULT_CODEC.deserialize(octal), Long.valueOf(octal, 8));
        assertEquals(TypeDefinitionAwareCodec.INT64_DEFAULT_CODEC.deserialize(negOctal), Long.valueOf(negOctal, 8));
        assertEquals(TypeDefinitionAwareCodec.INT64_DEFAULT_CODEC.deserialize(integer), Long.valueOf(integer, 10));
        assertEquals(TypeDefinitionAwareCodec.INT64_DEFAULT_CODEC.deserialize(negInteger), Long.valueOf(negInteger, 10));
    }
}
