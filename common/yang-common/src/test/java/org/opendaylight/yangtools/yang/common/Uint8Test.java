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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HexFormat;
import org.junit.jupiter.api.Test;

class Uint8Test {
    @Test
    void testValueOf() {
        assertEquals(127, Uint8.valueOf(Byte.MAX_VALUE).byteValue());
        assertEquals(255, Uint8.valueOf(255).intValue());
        assertEquals(255L, Uint8.valueOf(255L).longValue());
        assertEquals(0, Uint8.valueOf("0").intValue());
    }

    @Test
    void testSaturatedOf() {
        assertEquals(127, Uint8.saturatedOf((byte) 127).byteValue());
        assertEquals(127, Uint8.saturatedOf((short) 127).byteValue());
        assertEquals(127, Uint8.saturatedOf(127).byteValue());
        assertEquals(127, Uint8.saturatedOf(127L).byteValue());

        assertEquals(255, Uint8.saturatedOf((short) 255).intValue());
        assertEquals(255, Uint8.saturatedOf(255).intValue());
        assertEquals(255L, Uint8.saturatedOf(255L).longValue());
    }

    @Test
    @SuppressWarnings("SelfComparison")
    void testCompareTo() {
        final var five = Uint8.valueOf(5);
        final var zero = Uint8.valueOf(0);
        final var max = Uint8.valueOf(255);

        assertEquals(0, zero.compareTo(zero));
        assertEquals(-5, zero.compareTo(five));
        assertEquals(-255, zero.compareTo(max));

        assertEquals(5, five.compareTo(zero));
        assertEquals(0, five.compareTo(five));
        assertEquals(-250, five.compareTo(max));

        assertEquals(255, max.compareTo(zero));
        assertEquals(250, max.compareTo(five));
        assertEquals(0, max.compareTo(max));
    }

    @Test
    void testEquals() {
        final var five = Uint8.valueOf(5);
        final var zero = Uint8.valueOf(0);
        final var max = Uint8.valueOf(255);

        final var test = new Uint8(five);
        assertFalse(test.equals(zero));
        assertFalse(test.equals(new Object()));
        assertFalse(test.equals(max));
        assertTrue(test.equals(test));
        assertTrue(test.equals(five));
        assertTrue(five.equals(test));
    }

    @Test
    void testToString() {
        assertEquals("0", Uint8.valueOf(0).toString());
        assertEquals("127", Uint8.valueOf(127).toString());
        assertEquals("128", Uint8.valueOf(128).toString());
        assertEquals("255", Uint8.valueOf(255).toString());
    }

    @Test
    void testHashCode() {
        assertEquals(Byte.hashCode((byte)-63), Uint8.fromByteBits((byte)-63).hashCode());
    }

    @Test
    void testFloatValue() {
        assertEquals(0, Uint8.valueOf(0).floatValue(), 0);
    }

    @Test
    void testDoubleValue() {
        assertEquals(0, Uint8.valueOf(0).doubleValue(), 0);
    }

    @Test
    void testConversions() {
        assertSame(Uint8.valueOf(5), Uint8.valueOf(Uint16.valueOf(5)));
        assertSame(Uint8.valueOf(10), Uint8.valueOf(Uint32.valueOf(10)));
        assertSame(Uint8.valueOf(20), Uint8.valueOf(Uint64.valueOf(20)));

        assertEquals(Uint16.valueOf(255), Uint8.MAX_VALUE.toUint16());
        assertEquals(Uint32.valueOf(255), Uint8.MAX_VALUE.toUint32());
        assertEquals(Uint64.valueOf(255), Uint8.MAX_VALUE.toUint64());
    }

    @Test
    void testSerialization() throws IOException, ClassNotFoundException {
        final var source = Uint8.valueOf(255);
        final var bos = new ByteArrayOutputStream();
        try (var oos = new ObjectOutputStream(bos)) {
            oos.writeObject(source);
        }

        final var bytes = bos.toByteArray();
        assertEquals("""
            aced00057372002b6f72672e6f70656e6461796c696768742e79616e67746f6f6c732e79616e672e636f6d6d6f6e2e5531763100000\
            00000000000020001420004626974737870ff""", HexFormat.of().formatHex(bytes));

        try (var ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            assertSame(source, ois.readObject());
        }

    }

    @Test
    void testNegativeValues() {
        assertThrows(IllegalArgumentException.class, () -> Uint8.valueOf((byte)-1));
        assertThrows(IllegalArgumentException.class, () -> Uint8.valueOf((short)-1));
        assertThrows(IllegalArgumentException.class, () -> Uint8.valueOf(-1));
        assertThrows(IllegalArgumentException.class, () -> Uint8.valueOf(-1L));

        assertEquals(Uint8.ZERO, Uint8.saturatedOf((byte)-1));
        assertEquals(Uint8.ZERO, Uint8.saturatedOf((short)-1));
        assertEquals(Uint8.ZERO, Uint8.saturatedOf(-1));
        assertEquals(Uint8.ZERO, Uint8.saturatedOf(-1L));
    }

    @Test
    void testLargeValues() {
        assertThrows(IllegalArgumentException.class, () -> Uint8.valueOf((short)256));
        assertThrows(IllegalArgumentException.class, () -> Uint8.valueOf(256));
        assertThrows(IllegalArgumentException.class, () -> Uint8.valueOf(256L));

        assertEquals(Uint8.MAX_VALUE, Uint8.saturatedOf(Short.MAX_VALUE));
        assertEquals(Uint8.MAX_VALUE, Uint8.saturatedOf(Integer.MAX_VALUE));
        assertEquals(Uint8.MAX_VALUE, Uint8.saturatedOf(Long.MAX_VALUE));
    }

    @Test
    void testNullValueOfString() {
        assertThrows(NullPointerException.class, () -> Uint8.valueOf((String) null));
    }
}
