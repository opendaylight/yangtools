/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HexFormat;
import org.junit.jupiter.api.Test;

class Uint16Test {
    @Test
    void testValueOf() {
        assertEquals(127, Uint16.valueOf(Byte.MAX_VALUE).byteValue());
        assertEquals(32767, Uint16.valueOf(Short.MAX_VALUE).shortValue());
        assertEquals(65535, Uint16.valueOf(65535).intValue());
        assertEquals(65535L, Uint16.valueOf(65535L).longValue());
        assertEquals(0, Uint16.valueOf("0").intValue());
    }

    @Test
    void testSaturatedOf() {
        assertEquals(127, Uint16.saturatedOf((byte) 127).byteValue());
        assertEquals(127, Uint16.saturatedOf((short) 127).byteValue());
        assertEquals(127, Uint16.saturatedOf(127).byteValue());
        assertEquals(127, Uint16.saturatedOf(127L).byteValue());

        assertEquals(255, Uint16.saturatedOf((short) 255).intValue());
        assertEquals(255, Uint16.saturatedOf(255).intValue());
        assertEquals(255L, Uint16.saturatedOf(255L).longValue());
    }

    @Test
    @SuppressWarnings("SelfComparison")
    void testCompareTo() {
        final var five = Uint16.valueOf(5);
        final var zero = Uint16.valueOf(0);
        final var max = Uint16.valueOf(65535);

        assertEquals(0, zero.compareTo(zero));
        assertThat(zero.compareTo(five)).isLessThan(0);
        assertThat(zero.compareTo(max)).isLessThan(0);

        assertThat(five.compareTo(zero)).isGreaterThan(0);
        assertEquals(0, five.compareTo(five));
        assertThat(five.compareTo(max)).isLessThan(0);

        assertThat(max.compareTo(zero)).isGreaterThan(0);
        assertThat(max.compareTo(five)).isGreaterThan(0);
        assertEquals(0, max.compareTo(max));
    }

    @Test
    void testEquals() {
        final var five = Uint16.valueOf(5);
        final var zero = Uint16.valueOf(0);
        final var max = Uint16.valueOf(65535);

        final var test = new Uint16(five);
        assertFalse(test.equals(zero));
        assertFalse(test.equals(new Object()));
        assertFalse(test.equals(max));
        assertTrue(test.equals(test));
        assertTrue(test.equals(five));
        assertTrue(five.equals(test));
    }

    @Test
    void testToString() {
        assertEquals("0", Uint16.valueOf(0).toString());
        assertEquals("32767", Uint16.valueOf(32767).toString());
        assertEquals("32768", Uint16.valueOf(32768).toString());
        assertEquals("65535", Uint16.valueOf(65535).toString());
    }

    @Test
    void testHashCode() {
        assertEquals(Short.hashCode((short)-63), Uint16.fromShortBits((short)-63).hashCode());
    }

    @Test
    void testFloatValue() {
        assertEquals(0, Uint16.valueOf(0).floatValue(), 0);
    }

    @Test
    void testDoubleValue() {
        assertEquals(0, Uint16.valueOf(0).doubleValue(), 0);
    }

    @Test
    void testConversions() {
        assertSame(Uint16.valueOf(5), Uint16.valueOf(Uint8.valueOf(5)));
        assertSame(Uint16.valueOf(10), Uint16.valueOf(Uint32.valueOf(10)));
        assertSame(Uint16.valueOf(20), Uint16.valueOf(Uint64.valueOf(20)));

        assertEquals(Uint8.TEN, Uint16.TEN.toUint8());
        assertEquals(Uint32.valueOf(65535), Uint16.MAX_VALUE.toUint32());
        assertEquals(Uint64.valueOf(65535), Uint16.MAX_VALUE.toUint64());
    }

    @Test
    void testToUint8() {
        assertThrows(IllegalArgumentException.class, () -> Uint16.MAX_VALUE.toUint8());
    }

    @Test
    void testSerialization() throws Exception {
        final var source = Uint16.valueOf(255);
        final var bos = new ByteArrayOutputStream();
        try (var oos = new ObjectOutputStream(bos)) {
            oos.writeObject(source);
        }

        final var bytes = bos.toByteArray();
        assertEquals("""
            aced00057372002d6f72672e6f70656e6461796c696768742e79616e67746f6f6c732e79616e672e636f6d6d6f6e2e55696e7431360\
            00000000000000102000153000576616c7565787200106a6176612e6c616e672e4e756d62657286ac951d0b94e08b020000787000ff\
            """, HexFormat.of().formatHex(bytes));

        try (var ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            assertSame(source, ois.readObject());
        }
    }

    @Test
    void testNegativeValues() {
        assertThrows(IllegalArgumentException.class, () -> Uint16.valueOf((byte)-1));
        assertThrows(IllegalArgumentException.class, () -> Uint16.valueOf((short)-1));
        assertThrows(IllegalArgumentException.class, () -> Uint16.valueOf(-1));
        assertThrows(IllegalArgumentException.class, () -> Uint16.valueOf(-1L));

        assertEquals(Uint16.ZERO, Uint16.saturatedOf((byte)-1));
        assertEquals(Uint16.ZERO, Uint16.saturatedOf((short)-1));
        assertEquals(Uint16.ZERO, Uint16.saturatedOf(-1));
        assertEquals(Uint16.ZERO, Uint16.saturatedOf(-1L));
    }

    @Test
    void testLargeValues() {
        assertThrows(IllegalArgumentException.class, () -> Uint16.valueOf(65536));
        assertThrows(IllegalArgumentException.class, () -> Uint16.valueOf(65536L));

        assertEquals(Uint16.MAX_VALUE, Uint16.saturatedOf(65536));
        assertEquals(Uint16.MAX_VALUE, Uint16.saturatedOf(65536L));
    }

    @Test
    void testNullValueOfString() {
        assertThrows(NullPointerException.class, () -> Uint16.valueOf((String) null));
    }
}
