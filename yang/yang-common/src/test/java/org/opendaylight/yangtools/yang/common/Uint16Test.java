/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.junit.Test;

public class Uint16Test {
    @Test
    public void testValueOf() {
        assertEquals(127, Uint16.valueOf(Byte.MAX_VALUE).byteValue());
        assertEquals(32767, Uint16.valueOf(Short.MAX_VALUE).shortValue());
        assertEquals(65535, Uint16.valueOf(65535).intValue());
        assertEquals(65535L, Uint16.valueOf(65535L).longValue());
        assertEquals(0, Uint16.valueOf("0").intValue());
    }

    @Test
    public void testCompareTo() {
        final Uint16 five = Uint16.valueOf(5);
        final Uint16 zero = Uint16.valueOf(0);
        final Uint16 max = Uint16.valueOf(65535);

        assertEquals(0, zero.compareTo(zero));
        assertThat(zero.compareTo(five), lessThan(0));
        assertThat(zero.compareTo(max), lessThan(0));

        assertThat(five.compareTo(zero), greaterThan(0));
        assertEquals(0, five.compareTo(five));
        assertThat(five.compareTo(max), lessThan(0));

        assertThat(max.compareTo(zero), greaterThan(0));
        assertThat(max.compareTo(five), greaterThan(0));
        assertEquals(0, max.compareTo(max));
    }

    @Test
    public void testEquals() {
        final Uint16 five = Uint16.valueOf(5);
        final Uint16 zero = Uint16.valueOf(0);
        final Uint16 max = Uint16.valueOf(65535);

        final Uint16 test = new Uint16(five);
        assertFalse(test.equals(zero));
        assertFalse(test.equals(new Object()));
        assertFalse(test.equals(max));
        assertTrue(test.equals(test));
        assertTrue(test.equals(five));
        assertTrue(five.equals(test));
    }

    @Test
    public void testToString() {
        assertEquals("0", Uint16.valueOf(0).toString());
        assertEquals("32767", Uint16.valueOf(32767).toString());
        assertEquals("32768", Uint16.valueOf(32768).toString());
        assertEquals("65535", Uint16.valueOf(65535).toString());
    }

    @Test
    public void testHashCode() {
        assertEquals(Short.hashCode((short)-63), Uint16.fromShortBits((short)-63).hashCode());
    }

    @Test
    public void testFloatValue() {
        assertEquals(0, Uint16.valueOf(0).floatValue(), 0);
    }

    @Test
    public void testDoubleValue() {
        assertEquals(0, Uint16.valueOf(0).doubleValue(), 0);
    }

    @Test
    public void testConversions() {
        assertSame(Uint16.valueOf(5), Uint16.valueOf(Uint8.valueOf(5)));
        assertSame(Uint16.valueOf(10), Uint16.valueOf(Uint32.valueOf(10)));
        assertSame(Uint16.valueOf(20), Uint16.valueOf(Uint64.valueOf(20)));

        assertEquals(Uint8.TEN, Uint16.TEN.toUint8());
        assertEquals(Uint32.valueOf(65535), Uint16.MAX_VALUE.toUint32());
        assertEquals(Uint64.valueOf(65535), Uint16.MAX_VALUE.toUint64());
    }

    @Test
    public void testToUint8() {
        assertThrows(IllegalArgumentException.class, () -> Uint16.MAX_VALUE.toUint8());
    }

    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        final Uint16 source = Uint16.valueOf(255);
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
        assertThrows(IllegalArgumentException.class, () -> Uint16.valueOf((byte)-1));
    }

    @Test
    public void testNegativeShort() {
        assertThrows(IllegalArgumentException.class, () -> Uint16.valueOf((short)-1));
    }

    @Test
    public void testNegativeInt() {
        assertThrows(IllegalArgumentException.class, () -> Uint16.valueOf(-1));
    }

    @Test
    public void testNegativeLong() {
        assertThrows(IllegalArgumentException.class, () -> Uint16.valueOf(-1L));
    }

    @Test
    public void testBigInt() {
        assertThrows(IllegalArgumentException.class, () -> Uint16.valueOf(65536));
    }

    @Test
    public void testBigLong() {
        assertThrows(IllegalArgumentException.class, () -> Uint16.valueOf(65536L));
    }

    @Test
    public void testNullValueOfString() {
        assertThrows(NullPointerException.class, () -> Uint16.valueOf((String) null));
    }
}
