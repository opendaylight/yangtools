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
import org.junit.jupiter.api.Test;

public class Uint8Test {
    @Test
    public void testValueOf() {
        assertEquals(127, Uint8.valueOf(Byte.MAX_VALUE).byteValue());
        assertEquals(255, Uint8.valueOf(255).intValue());
        assertEquals(255L, Uint8.valueOf(255L).longValue());
        assertEquals(0, Uint8.valueOf("0").intValue());
    }

    @Test
    public void testSaturatedOf() {
        assertEquals(127, Uint8.saturatedOf((byte) 127).byteValue());
        assertEquals(127, Uint8.saturatedOf((short) 127).byteValue());
        assertEquals(127, Uint8.saturatedOf(127).byteValue());
        assertEquals(127, Uint8.saturatedOf(127L).byteValue());

        assertEquals(255, Uint8.saturatedOf((short) 255).intValue());
        assertEquals(255, Uint8.saturatedOf(255).intValue());
        assertEquals(255L, Uint8.saturatedOf(255L).longValue());
    }

    @Test
    public void testCompareTo() {
        final Uint8 five = Uint8.valueOf(5);
        final Uint8 zero = Uint8.valueOf(0);
        final Uint8 max = Uint8.valueOf(255);

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
    public void testEquals() {
        final Uint8 five = Uint8.valueOf(5);
        final Uint8 zero = Uint8.valueOf(0);
        final Uint8 max = Uint8.valueOf(255);

        final Uint8 test = new Uint8(five);
        assertFalse(test.equals(zero));
        assertFalse(test.equals(new Object()));
        assertFalse(test.equals(max));
        assertTrue(test.equals(test));
        assertTrue(test.equals(five));
        assertTrue(five.equals(test));
    }

    @Test
    public void testToString() {
        assertEquals("0", Uint8.valueOf(0).toString());
        assertEquals("127", Uint8.valueOf(127).toString());
        assertEquals("128", Uint8.valueOf(128).toString());
        assertEquals("255", Uint8.valueOf(255).toString());
    }

    @Test
    public void testHashCode() {
        assertEquals(Byte.hashCode((byte)-63), Uint8.fromByteBits((byte)-63).hashCode());
    }

    @Test
    public void testFloatValue() {
        assertEquals(0, Uint8.valueOf(0).floatValue(), 0);
    }

    @Test
    public void testDoubleValue() {
        assertEquals(0, Uint8.valueOf(0).doubleValue(), 0);
    }

    @Test
    public void testConversions() {
        assertSame(Uint8.valueOf(5), Uint8.valueOf(Uint16.valueOf(5)));
        assertSame(Uint8.valueOf(10), Uint8.valueOf(Uint32.valueOf(10)));
        assertSame(Uint8.valueOf(20), Uint8.valueOf(Uint64.valueOf(20)));

        assertEquals(Uint16.valueOf(255), Uint8.MAX_VALUE.toUint16());
        assertEquals(Uint32.valueOf(255), Uint8.MAX_VALUE.toUint32());
        assertEquals(Uint64.valueOf(255), Uint8.MAX_VALUE.toUint64());
    }

    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        final Uint8 source = Uint8.valueOf(255);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(source);
        }

        final Object read;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            read = ois.readObject();
        }

        assertSame(source, read);
    }

    @Test
    public void testNegativeValues() {
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
    public void testLargeValues() {
        assertThrows(IllegalArgumentException.class, () -> Uint8.valueOf((short)256));
        assertThrows(IllegalArgumentException.class, () -> Uint8.valueOf(256));
        assertThrows(IllegalArgumentException.class, () -> Uint8.valueOf(256L));

        assertEquals(Uint8.MAX_VALUE, Uint8.saturatedOf(Short.MAX_VALUE));
        assertEquals(Uint8.MAX_VALUE, Uint8.saturatedOf(Integer.MAX_VALUE));
        assertEquals(Uint8.MAX_VALUE, Uint8.saturatedOf(Long.MAX_VALUE));
    }

    @Test
    public void testNullValueOfString() {
        assertThrows(NullPointerException.class, () -> Uint8.valueOf((String) null));
    }
}
