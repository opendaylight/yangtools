/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.google.common.primitives.UnsignedInteger;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.junit.Test;

public class Uint32Test {
    @Test
    public void testValueOf() {
        assertEquals(127, Uint32.valueOf(Byte.MAX_VALUE).byteValue());
        assertEquals(32767, Uint32.valueOf(Short.MAX_VALUE).shortValue());
        assertEquals(2147483647, Uint32.valueOf(Integer.MAX_VALUE).intValue());
        assertEquals(4294967295L, Uint32.valueOf(4294967295L).longValue());
        assertEquals(0, Uint32.valueOf("0").intValue());
    }

    @Test
    public void testCompareTo() {
        final Uint32 five = Uint32.valueOf(5);
        final Uint32 zero = Uint32.valueOf(0);
        final Uint32 max = Uint32.valueOf(4294967295L);

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
    public void testEquals() {
        final Uint32 five = Uint32.valueOf(5);
        final Uint32 zero = Uint32.valueOf(0);
        final Uint32 max = Uint32.valueOf(4294967295L);

        final Uint32 test = new Uint32(five);
        assertFalse(test.equals(zero));
        assertFalse(test.equals(new Object()));
        assertFalse(test.equals(max));
        assertTrue(test.equals(test));
        assertTrue(test.equals(five));
        assertTrue(five.equals(test));
    }

    @Test
    public void testToString() {
        assertEquals("0", Uint32.valueOf(0).toString());
        assertEquals("2147483647", Uint32.valueOf(2147483647L).toString());
        assertEquals("2147483648", Uint32.valueOf(2147483648L).toString());
        assertEquals("4294967295", Uint32.valueOf(4294967295L).toString());
    }

    @Test
    public void testHashCode() {
        assertEquals(Integer.hashCode(-63), Uint32.fromIntBits(-63).hashCode());
    }

    @Test
    public void testFloatValue() {
        assertEquals(0, Uint32.valueOf(0).floatValue(), 0);
    }

    @Test
    public void testDoubleValue() {
        assertEquals(0, Uint32.valueOf(0).doubleValue(), 0);
    }

    @Test
    public void testConversions() {
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
    public void testToUint8() {
        assertThrows(IllegalArgumentException.class, () -> Uint32.MAX_VALUE.toUint8());
    }

    @Test
    public void testToUint16() {
        assertThrows(IllegalArgumentException.class, () -> Uint32.MAX_VALUE.toUint16());
    }

    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        final Uint32 source = Uint32.valueOf(255);
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
    public void testNegativeByte() {
        assertThrows(IllegalArgumentException.class, () -> Uint32.valueOf((byte)-1));
    }

    @Test
    public void testNegativeShort() {
        assertThrows(IllegalArgumentException.class, () -> Uint32.valueOf((short)-1));
    }

    @Test
    public void testNegativeInt() {
        assertThrows(IllegalArgumentException.class, () -> Uint32.valueOf(-1));
    }

    @Test
    public void testNegativeLong() {
        assertThrows(IllegalArgumentException.class, () -> Uint32.valueOf(-1L));
    }

    @Test
    public void testBigLong() {
        assertThrows(IllegalArgumentException.class, () -> Uint32.valueOf(4294967296L));
    }

    @Test
    public void testNullValueOfString() {
        assertThrows(NullPointerException.class, () -> Uint32.valueOf((String) null));
    }
}
