/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.primitives.UnsignedInteger;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HexFormat;
import org.junit.jupiter.api.Test;

class Uint32Test {
    @Test
    void testValueOf() {
        assertEquals(127, Uint32.valueOf(Byte.MAX_VALUE).byteValue());
        assertEquals(32767, Uint32.valueOf(Short.MAX_VALUE).shortValue());
        assertEquals(2147483647, Uint32.valueOf(Integer.MAX_VALUE).intValue());
        assertEquals(4294967295L, Uint32.valueOf(4294967295L).longValue());
        assertEquals(0, Uint32.valueOf("0").intValue());
    }

    @Test
    void testSaturatedOf() {
        assertEquals(127, Uint32.saturatedOf((byte) 127).byteValue());
        assertEquals(127, Uint32.saturatedOf((short) 127).byteValue());
        assertEquals(127, Uint32.saturatedOf(127).byteValue());
        assertEquals(127, Uint32.saturatedOf(127L).byteValue());

        assertEquals(255, Uint32.saturatedOf((short) 255).intValue());
        assertEquals(255, Uint32.saturatedOf(255).intValue());
        assertEquals(255L, Uint32.saturatedOf(255L).longValue());
    }

    @Test
    @SuppressWarnings("SelfComparison")
    void testCompareTo() {
        final var five = Uint32.valueOf(5);
        final var zero = Uint32.valueOf(0);
        final var max = Uint32.valueOf(4294967295L);

        assertEquals(0, zero.compareTo(zero));
        assertEquals(-1, zero.compareTo(five));
        assertEquals(-1, zero.compareTo(max));

        assertEquals(1, five.compareTo(zero));
        assertEquals(0, five.compareTo(five));
        assertEquals(-1, five.compareTo(max));

        assertEquals(1, max.compareTo(zero));
        assertEquals(1, max.compareTo(five));
        assertEquals(0, max.compareTo(max));
    }

    @Test
    void testEquals() {
        final var five = Uint32.valueOf(5);
        final var zero = Uint32.valueOf(0);
        final var max = Uint32.valueOf(4294967295L);

        final var test = new Uint32(five);
        assertFalse(test.equals(zero));
        assertFalse(test.equals(new Object()));
        assertFalse(test.equals(max));
        assertTrue(test.equals(test));
        assertTrue(test.equals(five));
        assertTrue(five.equals(test));
    }

    @Test
    void testToString() {
        assertEquals("0", Uint32.valueOf(0).toString());
        assertEquals("2147483647", Uint32.valueOf(2147483647L).toString());
        assertEquals("2147483648", Uint32.valueOf(2147483648L).toString());
        assertEquals("4294967295", Uint32.valueOf(4294967295L).toString());
    }

    @Test
    void testHashCode() {
        assertEquals(Integer.hashCode(-63), Uint32.fromIntBits(-63).hashCode());
    }

    @Test
    void testFloatValue() {
        assertEquals(0, Uint32.valueOf(0).floatValue(), 0);
    }

    @Test
    void testDoubleValue() {
        assertEquals(0, Uint32.valueOf(0).doubleValue(), 0);
    }

    @Test
    void testConversions() {
        assertSame(Uint32.valueOf(5), Uint32.valueOf(Uint8.valueOf(5)));
        assertSame(Uint32.valueOf(10), Uint32.valueOf(Uint16.valueOf(10)));
        assertSame(Uint32.valueOf(20), Uint32.valueOf(Uint64.valueOf(20)));

        assertSame(Uint32.valueOf(5), Uint32.valueOf(UnsignedInteger.fromIntBits(5)));
        assertEquals(UnsignedInteger.fromIntBits(5), Uint32.valueOf(5).toGuava());

        assertEquals(Uint8.TEN, Uint32.TEN.toUint8());
        assertEquals(Uint16.TEN, Uint32.TEN.toUint16());
        assertEquals(Uint64.valueOf(4294967295L), Uint32.MAX_VALUE.toUint64());
    }

    @Test
    void testToUint8() {
        assertThrows(IllegalArgumentException.class, () -> Uint32.MAX_VALUE.toUint8());
    }

    @Test
    void testToUint16() {
        assertThrows(IllegalArgumentException.class, () -> Uint32.MAX_VALUE.toUint16());
    }

    @Test
    void testSerialization() throws Exception {
        final var source = Uint32.valueOf(255);
        final var bos = new ByteArrayOutputStream();
        try (var oos = new ObjectOutputStream(bos)) {
            oos.writeObject(source);
        }

        final var bytes = bos.toByteArray();
        assertEquals("""
            aced00057372002d6f72672e6f70656e6461796c696768742e79616e67746f6f6c732e79616e672e636f6d6d6f6e2e55696e7433320\
            00000000000000102000149000576616c7565787200106a6176612e6c616e672e4e756d62657286ac951d0b94e08b02000078700000\
            00ff""", HexFormat.of().formatHex(bytes));

        try (var ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            assertSame(source, ois.readObject());
        }

    }

    @Test
    void testNegativeValues() {
        assertThrows(IllegalArgumentException.class, () -> Uint32.valueOf((byte)-1));
        assertThrows(IllegalArgumentException.class, () -> Uint32.valueOf((short)-1));
        assertThrows(IllegalArgumentException.class, () -> Uint32.valueOf(-1));
        assertThrows(IllegalArgumentException.class, () -> Uint32.valueOf(-1L));

        assertEquals(Uint32.ZERO, Uint32.saturatedOf((byte)-1));
        assertEquals(Uint32.ZERO, Uint32.saturatedOf((short)-1));
        assertEquals(Uint32.ZERO, Uint32.saturatedOf(-1));
        assertEquals(Uint32.ZERO, Uint32.saturatedOf(-1L));
    }

    @Test
    void testLargeValues() {
        assertThrows(IllegalArgumentException.class, () -> Uint32.valueOf(4294967296L));

        assertEquals(Uint32.MAX_VALUE, Uint32.saturatedOf(4294967296L));
    }

    @Test
    void testNullValueOfString() {
        assertThrows(NullPointerException.class, () -> Uint32.valueOf((String) null));
    }
}
