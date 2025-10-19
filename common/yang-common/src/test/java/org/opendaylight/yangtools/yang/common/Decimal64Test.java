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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HexFormat;
import org.junit.jupiter.api.Test;

class Decimal64Test {
    @Test
    void testParseEmpty() {
        assertThrows(NumberFormatException.class, () -> Decimal64.valueOf(""));
    }

    @Test
    void testParseSingleIllegal() {
        assertThrows(NumberFormatException.class, () -> Decimal64.valueOf("a"));
    }

    @Test
    void testParseSingleHighIllegal() {
        assertThrows(NumberFormatException.class, () -> Decimal64.valueOf(":"));
    }

    @Test
    void testParseZeroIllegal() {
        assertThrows(NumberFormatException.class, () -> Decimal64.valueOf("0a"));
    }

    @Test
    void testParseZeroHighIllegal() {
        assertThrows(NumberFormatException.class, () -> Decimal64.valueOf("0:"));
    }

    @Test
    void testParseZeroPointIllegal() {
        assertThrows(NumberFormatException.class, () -> Decimal64.valueOf("0.a"));
    }

    @Test
    void testParseZeroPointHighIllegal() {
        assertThrows(NumberFormatException.class, () -> Decimal64.valueOf("0.:"));
    }

    @Test
    void testParsePointIllegal() {
        assertThrows(NumberFormatException.class, () -> Decimal64.valueOf(".a"));
    }

    @Test
    void testParseMinus() {
        assertThrows(NumberFormatException.class, () -> Decimal64.valueOf("-"));
    }

    @Test
    void testParsePlus() {
        assertThrows(NumberFormatException.class, () -> Decimal64.valueOf("+"));
    }

    @Test
    void testParsePeriod() {
        assertThrows(NumberFormatException.class, () -> Decimal64.valueOf("."));
    }

    @Test
    void testParseTwoPeriods() {
        assertThrows(NumberFormatException.class, () -> Decimal64.valueOf(".."));
    }

    @Test
    void testParseTrailingPeriod() {
        assertThrows(NumberFormatException.class, () -> Decimal64.valueOf("0."));
    }

    @Test
    void testParseMultiplePeriods() {
        assertThrows(NumberFormatException.class, () -> Decimal64.valueOf("0.1."));
    }

    @Test
    void testParseLongString() {
        assertEquals(Decimal64.of(1, 1234567890123456780L), Decimal64.valueOf("123456789012345678"));
    }

    @Test
    void testParseLongDecimal() {
        assertEquals(Decimal64.of(18, 12345678901234568L), Decimal64.valueOf("0.12345678901234568"));
    }

    @Test
    void testFractionLimits() {
        assertEquals(Decimal64.of(1, 9223372036854775807L), Decimal64.valueOf("922337203685477580.7"));
        assertEquals(Decimal64.of(18, 9223372036854775807L), Decimal64.valueOf("9.223372036854775807"));

        assertThrows(NumberFormatException.class, () -> Decimal64.valueOf("922337203685477580.71"));
        assertThrows(NumberFormatException.class, () -> Decimal64.valueOf("9.2233720368547758071"));
    }

    @Test
    void testParseTooLongString() {
        assertThrows(NumberFormatException.class, () -> Decimal64.valueOf("1234567890123456789"));
    }

    @Test
    void testParseTooLongDecimal() {
        assertThrows(NumberFormatException.class, () -> Decimal64.valueOf("0.1234567890123456789"));
    }

    @Test
    void testParse() {
        assertParsedVariants("0", 0, 0, 1);
        assertParsedVariants("0.00", 0, 0, 1);
        assertParsedVariants("00.0", 0, 0, 1);
        assertParsedVariants("000.0", 0, 0, 1);
        assertCanonicalVariants("10.0", 10, 0, 1);
        assertCanonicalVariants("10.09", 10, 9, 2);
        assertParsedVariants("10.0900900", 10, 9009, 5);
        assertParsedVariants("0002210.09", 2210, 9, 2);

        var parsed = assertParsedString("0.0", 0, 0, 1, false);
        parsed = assertParsedString("+0.0", 0, 0, 1, false);
        assertEquals("0.0", parsed.toString());
        assertEquals("0.0", parsed.toString());
        parsed = assertParsedString("-0.0", 0, 0, 1, true);
        assertEquals("0.0", parsed.toString());

        assertCanonicalVariants("1.0", 1, 0, 1);
        assertCanonicalVariants("2.3", 2, 3, 1);
    }

    @Test
    @SuppressWarnings("SelfComparison")
    void testCompare() {
        final var one = Decimal64.valueOf("1");
        final var two = Decimal64.valueOf("2");
        final var three = Decimal64.valueOf("3");
        final var negOne = Decimal64.valueOf("-1");
        final var anotherOne = Decimal64.valueOf("1");

        assertEquals(0, one.compareTo(one));
        assertEquals(0, one.compareTo(anotherOne));
        assertEquals(-1, one.compareTo(two));
        assertEquals(-1, one.compareTo(three));
        assertEquals(1, one.compareTo(negOne));

        assertEquals(1, two.compareTo(one));
        assertEquals(1, two.compareTo(anotherOne));
        assertEquals(-1, two.compareTo(three));
        assertEquals(1, one.compareTo(negOne));
    }

    @Test
    void testEquals() {
        final var one = Decimal64.valueOf("1");
        final var two = Decimal64.valueOf("2");
        final var anotherOne = Decimal64.valueOf("1");

        assertTrue(one.equals(one));
        assertTrue(one.equals(anotherOne));
        assertFalse(one.equals(two));
        assertTrue(two.equals(two));
        assertFalse(two.equals(one));

        assertFalse(one.equals(new Object()));
    }

    @Test
    void testConversions() {
        assertEquals(new BigDecimal("0.12"), Decimal64.valueOf("0.12").decimalValue());
        assertEquals(new BigDecimal("-0.12"), Decimal64.valueOf("-0.12").decimalValue());
        assertEquals(new BigDecimal("0.12"), Decimal64.valueOf("+0.12").decimalValue());
        assertEquals(new BigDecimal("123.456"), Decimal64.valueOf("123.456").decimalValue());
        assertEquals(new BigDecimal("-123.456"), Decimal64.valueOf("-123.456").decimalValue());

        assertEquals(0.12, Decimal64.valueOf("0.12").doubleValue(), 0);
        assertEquals(-0.12, Decimal64.valueOf("-0.12").doubleValue(), 0);

        assertEquals((float) 0.12, Decimal64.valueOf("0.12").floatValue(), 0);
        assertEquals((float) -0.12, Decimal64.valueOf("-0.12").floatValue(), 0);

        assertEquals(12345678901L, Decimal64.valueOf("12345678901").longValue());
        assertEquals(-12345678901L, Decimal64.valueOf("-12345678901").longValue());
    }

    @Test
    void testFactories() {
        assertEquals("0.0", Decimal64.valueOf(1, (byte) 0).toString());
        assertEquals("1.0", Decimal64.valueOf(1, (byte) 1).toString());
        assertEquals("-1.0", Decimal64.valueOf(1, (byte) -1).toString());

        assertEquals("0.0", Decimal64.valueOf(1, (short) 0).toString());
        assertEquals("1.0", Decimal64.valueOf(1, (short) 1).toString());
        assertEquals("-1.0", Decimal64.valueOf(1, (short) -1).toString());

        assertEquals("0.0", Decimal64.valueOf(1, 0).toString());
        assertEquals("1.0", Decimal64.valueOf(1, 1).toString());
        assertEquals("-1.0", Decimal64.valueOf(1, -1).toString());

        assertEquals("0.0", Decimal64.valueOf(1, 0L).toString());
        assertEquals("1.0", Decimal64.valueOf(1, 1L).toString());
        assertEquals("-1.0", Decimal64.valueOf(1, -1L).toString());

        assertEquals("0.0", Decimal64.valueOf(0.0F, RoundingMode.UNNECESSARY).toString());
        assertEquals("1.0", Decimal64.valueOf(1.0F, RoundingMode.UNNECESSARY).toString());
        assertEquals("-1.0", Decimal64.valueOf(-1.0F, RoundingMode.UNNECESSARY).toString());

        assertEquals("0.0", Decimal64.valueOf(0.0D, RoundingMode.UNNECESSARY).toString());
        assertEquals("1.0", Decimal64.valueOf(1.0D, RoundingMode.UNNECESSARY).toString());
        assertEquals("-1.0", Decimal64.valueOf(-1.0D, RoundingMode.UNNECESSARY).toString());

        assertEquals("0.0", Decimal64.valueOf(BigDecimal.ZERO).toString());
        assertEquals("1.0", Decimal64.valueOf(BigDecimal.ONE).toString());
        assertEquals("-1.0", Decimal64.valueOf(BigDecimal.ONE.negate()).toString());
    }

    @Test
    void testLeadingZeroToString() {
        assertEquals("-0.63", Decimal64.valueOf("-0.63").toString());
    }

    @Test
    void testFractionPartToString() {
        assertEquals("0.3", Decimal64.valueOf("0.3").toString());
        assertEquals("0.03", Decimal64.valueOf("0.03").toString());
        assertEquals("0.003", Decimal64.valueOf("0.003").toString());
        assertEquals("-0.3", Decimal64.valueOf("-0.3").toString());
        assertEquals("-0.03", Decimal64.valueOf("-0.03").toString());
        assertEquals("-0.003", Decimal64.valueOf("-0.003").toString());
    }

    @Test
    void testScalingToString() {
        assertEquals("30.0", Decimal64.of(1, 300).toString());
        assertEquals("3.0", Decimal64.of(2, 300).toString());
        assertEquals("0.3", Decimal64.of(3, 300).toString());
        assertEquals("0.03", Decimal64.of(4, 300).toString());
    }

    @Test
    void testBoundaries() {
        assertEquals(-128L, Decimal64.valueOf(1, Byte.MIN_VALUE).longValue());
        assertEquals(127L, Decimal64.valueOf(1, Byte.MAX_VALUE).longValue());
        assertEquals(-32768L, Decimal64.valueOf(2, Short.MIN_VALUE).longValue());
        assertEquals(32767L, Decimal64.valueOf(2, Short.MAX_VALUE).longValue());
        assertEquals(-2147483648L, Decimal64.valueOf(3, Integer.MIN_VALUE).longValue());
        assertEquals(2147483647L, Decimal64.valueOf(3, Integer.MAX_VALUE).longValue());
    }

    @Test
    void testByteValueExact() {
        assertEquals(Byte.MIN_VALUE, Decimal64.valueOf(1, Byte.MIN_VALUE).byteValueExact());
        assertEquals(Byte.MAX_VALUE, Decimal64.valueOf(1, Byte.MAX_VALUE).byteValueExact());
    }

    @Test
    void testByteValueExactFrac() {
        final var dec = Decimal64.valueOf("1.1");
        assertThrows(ArithmeticException.class, () -> dec.byteValueExact());
    }

    @Test
    void testByteValueExactRange() {
        final var dec = Decimal64.valueOf(1, Byte.MAX_VALUE + 1);
        assertThrows(ArithmeticException.class, () -> dec.byteValueExact());
    }

    @Test
    void testShortValueExact() {
        assertEquals(Short.MIN_VALUE, Decimal64.valueOf(1, Short.MIN_VALUE).shortValueExact());
        assertEquals(Short.MAX_VALUE, Decimal64.valueOf(1, Short.MAX_VALUE).shortValueExact());
    }

    @Test
    void testShortValueExactFrac() {
        final var dec = Decimal64.valueOf("1.1");
        assertThrows(ArithmeticException.class, () -> dec.shortValueExact());
    }

    @Test
    void testShortValueExactRange() {
        final var dec = Decimal64.valueOf(1, Short.MAX_VALUE + 1);
        assertThrows(ArithmeticException.class, () -> dec.shortValueExact());
    }

    @Test
    void testIntValueExact() {
        assertEquals(Integer.MIN_VALUE, Decimal64.valueOf(1, Integer.MIN_VALUE).intValueExact());
        assertEquals(Integer.MAX_VALUE, Decimal64.valueOf(1, Integer.MAX_VALUE).intValueExact());
    }

    @Test
    void testIntValueExactFrac() {
        final var dec = Decimal64.valueOf("1.1");
        assertThrows(ArithmeticException.class, () -> dec.intValueExact());
    }

    @Test
    void testIntValueExactRange() {
        final var dec = Decimal64.valueOf(1, Integer.MAX_VALUE + 1L);
        assertThrows(ArithmeticException.class, () -> dec.intValueExact());
    }

    @Test
    void testLongValueExactFrac() {
        final var dec = Decimal64.valueOf("1.1");
        assertThrows(ArithmeticException.class, () -> dec.longValueExact());
    }

    @Test
    void testLongValueOfBits() {
        final var dec = Decimal64.valueOf(2, 25552555555L);
        assertEquals(2, dec.scale());
        assertEquals(2555255555500L, dec.unscaledValue());
    }

    @Test
    void testLongValueOfNegativeBits() {
        final var dec = Decimal64.valueOf(2, -25552555555L);
        assertEquals(2, dec.scale());
        assertEquals(-2555255555500L, dec.unscaledValue());
    }

    @Test
    void testByteRange() {
        for (int i = 1; i <= 16; ++i) {
            assertEquals(Byte.MIN_VALUE, Decimal64.valueOf(i, Byte.MIN_VALUE).byteValueExact());
            assertEquals(Byte.MAX_VALUE, Decimal64.valueOf(i, Byte.MAX_VALUE).byteValueExact());
        }
        for (int i = 17; i <= 18; ++i) {
            int scale = i;
            assertThrows(IllegalArgumentException.class, () -> Decimal64.valueOf(scale, Byte.MIN_VALUE));
            assertThrows(IllegalArgumentException.class, () -> Decimal64.valueOf(scale, Byte.MAX_VALUE));
        }
    }

    @Test
    void testShortRange() {
        for (int i = 1; i <= 14; ++i) {
            assertEquals(Short.MIN_VALUE, Decimal64.valueOf(i, Short.MIN_VALUE).shortValueExact());
            assertEquals(Short.MAX_VALUE, Decimal64.valueOf(i, Short.MAX_VALUE).shortValueExact());
        }
        for (int i = 15; i <= 18; ++i) {
            int scale = i;
            assertThrows(IllegalArgumentException.class, () -> Decimal64.valueOf(scale, Short.MIN_VALUE));
            assertThrows(IllegalArgumentException.class, () -> Decimal64.valueOf(scale, Short.MAX_VALUE));
        }
    }

    @Test
    void testIntRange() {
        for (int i = 1; i <= 9; ++i) {
            assertEquals(Integer.MIN_VALUE, Decimal64.valueOf(i, Integer.MIN_VALUE).intValueExact());
            assertEquals(Integer.MAX_VALUE, Decimal64.valueOf(i, Integer.MAX_VALUE).intValueExact());
        }
        for (int i = 10; i <= 18; ++i) {
            int scale = i;
            assertThrows(IllegalArgumentException.class, () -> Decimal64.valueOf(scale, Integer.MIN_VALUE));
            assertThrows(IllegalArgumentException.class, () -> Decimal64.valueOf(scale, Integer.MAX_VALUE));
        }
    }

    @Test
    void testLongRange() {
        for (int i = 1; i <= 18; ++i) {
            int scale = i;
            assertThrows(IllegalArgumentException.class, () -> Decimal64.valueOf(scale, Long.MIN_VALUE));
            assertThrows(IllegalArgumentException.class, () -> Decimal64.valueOf(scale, Long.MAX_VALUE));
        }
    }

    @Test
    void testSerialization() throws Exception {
        final var source = Decimal64.valueOf("-0.63");
        final var bos = new ByteArrayOutputStream();
        try (var oos = new ObjectOutputStream(bos)) {
            oos.writeObject(source);
        }

        final var bytes = bos.toByteArray();
        assertEquals("""
            aced0005737200306f72672e6f70656e6461796c696768742e79616e67746f6f6c732e79616e672e636f6d6d6f6e2e446563696d616\
            c363400000000000000010200024200066f66667365744a000576616c7565787200106a6176612e6c616e672e4e756d62657286ac95\
            1d0b94e08b020000787001ffffffffffffffc1""", HexFormat.of().formatHex(bytes));

        try (var ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            assertEquals(source, ois.readObject());
        }
    }

    private static void assertCanonicalVariants(final String str, final long intPart, final long fracPart,
            final int digits) {
        assertCanonicalString(str, intPart, fracPart, digits, false);
        assertCanonicalString("-" + str, intPart, fracPart, digits, true);

        final var parsed = assertParsedString("+" + str, intPart, fracPart, digits, false);
        assertEquals(str, parsed.toString());
    }

    private static void assertParsedVariants(final String str, final long intPart, final long fracPart,
            final int digits) {
        assertParsedString(str, intPart, fracPart, digits, false);
        assertParsedString("-" + str, intPart, fracPart, digits, true);
        assertParsedString("+" + str, intPart, fracPart, digits, false);
    }

    private static void assertCanonicalString(final String str, final long intPart, final long fracPart,
            final int digits, final boolean negative) {
        final var parsed = assertParsedString(str, intPart, fracPart, digits, negative);
        assertEquals(str, parsed.toString());
    }

    private static Decimal64 assertParsedString(final String str, final long intPart, final long fracPart,
            final int digits, final boolean negative) {
        final var parsed = Decimal64.valueOf(str);
        assertEquals(new Decimal64((byte) digits, intPart, fracPart, negative), parsed);
        return parsed;
    }
}
