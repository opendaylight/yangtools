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

import com.google.common.primitives.UnsignedLong;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import org.junit.jupiter.api.Test;

public class Uint64Test {
    @Test
    public void testValueOf() {
        assertEquals(127, Uint64.valueOf(Byte.MAX_VALUE).byteValue());
        assertEquals(32767, Uint64.valueOf(Short.MAX_VALUE).shortValue());
        assertEquals(2147483647, Uint64.valueOf(Integer.MAX_VALUE).intValue());
        assertEquals(9223372036854775807L, Uint64.valueOf(Long.MAX_VALUE).longValue());
        assertEquals(0, Uint64.valueOf("0").intValue());
        assertEquals(2170205184637009920L, Uint64.valueOf(2170205184637009920L).longValue());
        assertEquals(2170205184637009920L, Uint64.valueOf(new BigInteger("2170205184637009920")).longValue());
    }

    @Test
    public void testSaturatedOf() {
        assertEquals(127, Uint64.saturatedOf((byte) 127).byteValue());
        assertEquals(127, Uint64.saturatedOf((short) 127).byteValue());
        assertEquals(127, Uint64.saturatedOf(127).byteValue());
        assertEquals(127, Uint64.saturatedOf(127L).byteValue());

        assertEquals(255, Uint64.saturatedOf((short) 255).intValue());
        assertEquals(255, Uint64.saturatedOf(255).intValue());
        assertEquals(255L, Uint64.saturatedOf(255L).longValue());
        assertEquals(2170205184637009920L, Uint64.saturatedOf(new BigInteger("2170205184637009920")).longValue());
    }

    @Test
    public void testCompareTo() {
        final Uint64 five = Uint64.valueOf(5);
        final Uint64 zero = Uint64.valueOf(0);
        final Uint64 max = Uint64.valueOf(4294967295L);

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
        final Uint64 five = Uint64.valueOf(5);
        final Uint64 zero = Uint64.valueOf(0);
        final Uint64 max = Uint64.valueOf(4294967295L);

        final Uint64 test = new Uint64(five);
        assertFalse(test.equals(zero));
        assertFalse(test.equals(new Object()));
        assertFalse(test.equals(max));
        assertTrue(test.equals(test));
        assertTrue(test.equals(five));
        assertTrue(five.equals(test));
    }

    @Test
    public void testToString() {
        assertEquals("0", Uint64.valueOf(0).toString());
        assertEquals("2147483647", Uint64.valueOf(2147483647L).toString());
        assertEquals("2147483648", Uint64.valueOf(2147483648L).toString());
        assertEquals("4294967295", Uint64.valueOf(4294967295L).toString());
    }

    @Test
    public void testHashCode() {
        assertEquals(Long.hashCode(-63), Uint64.fromLongBits(-63L).hashCode());
    }

    @Test
    public void testFloatValue() {
        assertEquals(0, Uint64.valueOf(0).floatValue(), 0);
    }

    @Test
    public void testDoubleValue() {
        assertEquals(0, Uint64.valueOf(0).doubleValue(), 0);
    }

    @Test
    public void testConversions() {
        assertSame(Uint64.valueOf(5), Uint64.valueOf(Uint8.valueOf(5)));
        assertSame(Uint64.valueOf(10), Uint64.valueOf(Uint16.valueOf(10)));
        assertSame(Uint64.valueOf(20), Uint64.valueOf(Uint32.valueOf(20)));
        assertEquals(Uint64.valueOf(30), Uint64.valueOf(new BigInteger("30")));

        assertSame(Uint64.valueOf(5), Uint64.valueOf(UnsignedLong.fromLongBits(5)));
        assertEquals(UnsignedLong.fromLongBits(5), Uint64.valueOf(5).toGuava());

        assertEquals(Uint8.TEN, Uint64.TEN.toUint8());
        assertEquals(Uint16.TEN, Uint64.TEN.toUint16());
        assertEquals(Uint32.TEN, Uint64.TEN.toUint32());
    }

    @Test
    public void testToUint8() {
        assertThrows(IllegalArgumentException.class, () -> Uint64.MAX_VALUE.toUint8());
    }

    @Test
    public void testToUint16() {
        assertThrows(IllegalArgumentException.class, () -> Uint64.MAX_VALUE.toUint16());
    }

    @Test
    public void testToUint32() {
        assertThrows(IllegalArgumentException.class, () -> Uint64.MAX_VALUE.toUint32());
    }

    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        final Uint64 source = Uint64.valueOf(255);
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
        assertThrows(IllegalArgumentException.class, () -> Uint64.valueOf((byte)-1));
        assertThrows(IllegalArgumentException.class, () -> Uint64.valueOf((short)-1));
        assertThrows(IllegalArgumentException.class, () -> Uint64.valueOf(-1));
        assertThrows(IllegalArgumentException.class, () -> Uint64.valueOf(-1L));
        assertThrows(IllegalArgumentException.class, () -> Uint64.valueOf(new BigInteger("-1")));

        assertEquals(Uint64.ZERO, Uint64.saturatedOf((byte)-1));
        assertEquals(Uint64.ZERO, Uint64.saturatedOf((short)-1));
        assertEquals(Uint64.ZERO, Uint64.saturatedOf(-1));
        assertEquals(Uint64.ZERO, Uint64.saturatedOf(-1L));
        assertEquals(Uint64.ZERO, Uint64.saturatedOf(new BigInteger("-1")));
    }

    @Test
    public void testLargeValues() {
        final BigInteger big = new BigInteger("10000000000000000", 16);
        assertThrows(IllegalArgumentException.class, () -> Uint64.valueOf(big));

        assertEquals(Uint64.MAX_VALUE, Uint64.saturatedOf(big));
    }

    @Test
    public void testNullValueOf() {
        assertThrows(NullPointerException.class, () -> Uint64.valueOf((String) null));
        assertThrows(NullPointerException.class, () -> Uint64.valueOf((BigInteger) null));
    }
}
