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
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import org.junit.Test;

public class Decimal64Test {
    @Test
    public void testParseEmpty() {
        assertThrows(NumberFormatException.class, () -> Decimal64.valueOf(""));
    }

    @Test
    public void testParseSingleIllegal() {
        assertThrows(NumberFormatException.class, () -> Decimal64.valueOf("a"));
    }

    @Test
    public void testParseSingleHighIllegal() {
        assertThrows(NumberFormatException.class, () -> Decimal64.valueOf(":"));
    }

    @Test
    public void testParseZeroIllegal() {
        assertThrows(NumberFormatException.class, () -> Decimal64.valueOf("0a"));
    }

    @Test
    public void testParseZeroHighIllegal() {
        assertThrows(NumberFormatException.class, () -> Decimal64.valueOf("0:"));
    }

    @Test
    public void testParseZeroPointIllegal() {
        assertThrows(NumberFormatException.class, () -> Decimal64.valueOf("0.a"));
    }

    @Test
    public void testParseZeroPointHighIllegal() {
        assertThrows(NumberFormatException.class, () -> Decimal64.valueOf("0.:"));
    }

    @Test
    public void testParsePointIllegal() {
        assertThrows(NumberFormatException.class, () -> Decimal64.valueOf(".a"));
    }

    @Test
    public void testParseMinus() {
        assertThrows(NumberFormatException.class, () -> Decimal64.valueOf("-"));
    }

    @Test
    public void testParsePlus() {
        assertThrows(NumberFormatException.class, () -> Decimal64.valueOf("+"));
    }

    @Test
    public void testParsePeriod() {
        assertThrows(NumberFormatException.class, () -> Decimal64.valueOf("."));
    }

    @Test
    public void testParseTwoPeriods() {
        assertThrows(NumberFormatException.class, () -> Decimal64.valueOf(".."));
    }

    @Test
    public void testParseTrailingPeriod() {
        assertThrows(NumberFormatException.class, () -> Decimal64.valueOf("0."));
    }

    @Test
    public void testParseMultiplePeriods() {
        assertThrows(NumberFormatException.class, () -> Decimal64.valueOf("0.1."));
    }

    @Test
    public void testParseLongString() {
        Decimal64.valueOf("123456789012345678");
    }

    @Test
    public void testParseLongDecimal() {
        Decimal64.valueOf("0.12345678901234568");
    }

    @Test
    public void testParseTooLongString() {
        assertThrows(NumberFormatException.class, () -> Decimal64.valueOf("1234567890123456789"));
    }

    @Test
    public void testParseTooLongDecimal() {
        assertThrows(NumberFormatException.class, () -> Decimal64.valueOf("0.123456789012345689"));
    }

    @Test
    public void testParse() {
        assertParsedVariants("0", 0, 0, 1);
        assertParsedVariants("0.00", 0, 0, 1);
        assertParsedVariants("00.0", 0, 0, 1);
        assertParsedVariants("000.0", 0, 0, 1);
        assertCanonicalVariants("10.0", 10, 0, 1);
        assertCanonicalVariants("10.09", 10, 9, 2);
        assertParsedVariants("10.0900900", 10, 9009, 5);
        assertParsedVariants("0002210.09", 2210, 9, 2);

        Decimal64 parsed = assertParsedString("0.0", 0, 0, 1, false);
        parsed = assertParsedString("+0.0", 0, 0, 1, false);
        assertEquals("0.0", parsed.toString());
        assertEquals("0.0", parsed.toString());
        parsed = assertParsedString("-0.0", 0, 0, 1, true);
        assertEquals("0.0", parsed.toString());

        assertCanonicalVariants("1.0", 1, 0, 1);
        assertCanonicalVariants("2.3", 2, 3, 1);
    }

    @Test
    public void testCompare() {
        final Decimal64 one = Decimal64.valueOf("1");
        final Decimal64 two = Decimal64.valueOf("2");
        final Decimal64 three = Decimal64.valueOf("3");
        final Decimal64 negOne = Decimal64.valueOf("-1");
        final Decimal64 anotherOne = Decimal64.valueOf("1");

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
    public void testEquals() {
        final Decimal64 one = Decimal64.valueOf("1");
        final Decimal64 two = Decimal64.valueOf("2");
        final Decimal64 anotherOne = Decimal64.valueOf("1");

        assertTrue(one.equals(one));
        assertTrue(one.equals(anotherOne));
        assertFalse(one.equals(two));
        assertTrue(two.equals(two));
        assertFalse(two.equals(one));

        assertFalse(one.equals(new Object()));
    }

    @Test
    public void testConversions() {
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
    public void testFactories() {
        assertEquals("0.0", Decimal64.valueOf((byte) 0).toString());
        assertEquals("1.0", Decimal64.valueOf((byte) 1).toString());
        assertEquals("-1.0", Decimal64.valueOf((byte) -1).toString());

        assertEquals("0.0", Decimal64.valueOf((short) 0).toString());
        assertEquals("1.0", Decimal64.valueOf((short) 1).toString());
        assertEquals("-1.0", Decimal64.valueOf((short) -1).toString());

        assertEquals("0.0", Decimal64.valueOf(0).toString());
        assertEquals("1.0", Decimal64.valueOf(1).toString());
        assertEquals("-1.0", Decimal64.valueOf(-1).toString());

        assertEquals("0.0", Decimal64.valueOf(0L).toString());
        assertEquals("1.0", Decimal64.valueOf(1L).toString());
        assertEquals("-1.0", Decimal64.valueOf(-1L).toString());

        assertEquals("0.0", Decimal64.valueOf(0.0).toString());
        assertEquals("1.0", Decimal64.valueOf(1.0).toString());
        assertEquals("-1.0", Decimal64.valueOf(-1.0).toString());

        assertEquals("0.0", Decimal64.valueOf(BigDecimal.ZERO).toString());
        assertEquals("1.0", Decimal64.valueOf(BigDecimal.ONE).toString());
        assertEquals("-1.0", Decimal64.valueOf(BigDecimal.ONE.negate()).toString());
    }

    @Test
    public void testBoundaries() {
        assertEquals(-128L, Decimal64.valueOf(Byte.MIN_VALUE).longValue());
        assertEquals(127L, Decimal64.valueOf(Byte.MAX_VALUE).longValue());
        assertEquals(-32768L, Decimal64.valueOf(Short.MIN_VALUE).longValue());
        assertEquals(32767L, Decimal64.valueOf(Short.MAX_VALUE).longValue());
        assertEquals(-2147483648L, Decimal64.valueOf(Integer.MIN_VALUE).longValue());
        assertEquals(2147483647L, Decimal64.valueOf(Integer.MAX_VALUE).longValue());
    }

    @Test
    public void testByteValueExact() {
        assertEquals(Byte.MIN_VALUE, Decimal64.valueOf(Byte.MIN_VALUE).byteValueExact());
        assertEquals(Byte.MAX_VALUE, Decimal64.valueOf(Byte.MAX_VALUE).byteValueExact());
    }

    @Test
    public void testByteValueExactFrac() {
        final Decimal64 dec = Decimal64.valueOf("1.1");
        assertThrows(ArithmeticException.class, () -> dec.byteValueExact());
    }

    @Test
    public void testByteValueExactRange() {
        final Decimal64 dec = Decimal64.valueOf(Byte.MAX_VALUE + 1);
        assertThrows(ArithmeticException.class, () -> dec.byteValueExact());
    }

    @Test
    public void testShortValueExact() {
        assertEquals(Short.MIN_VALUE, Decimal64.valueOf(Short.MIN_VALUE).shortValueExact());
        assertEquals(Short.MAX_VALUE, Decimal64.valueOf(Short.MAX_VALUE).shortValueExact());
    }

    @Test
    public void testShortValueExactFrac() {
        final Decimal64 dec = Decimal64.valueOf("1.1");
        assertThrows(ArithmeticException.class, () -> dec.shortValueExact());
    }

    @Test
    public void testShortValueExactRange() {
        final Decimal64 dec = Decimal64.valueOf(Short.MAX_VALUE + 1);
        assertThrows(ArithmeticException.class, () -> dec.shortValueExact());
    }

    @Test
    public void testIntValueExact() {
        assertEquals(Integer.MIN_VALUE, Decimal64.valueOf(Integer.MIN_VALUE).intValueExact());
        assertEquals(Integer.MAX_VALUE, Decimal64.valueOf(Integer.MAX_VALUE).intValueExact());
    }

    @Test
    public void testIntValueExactFrac() {
        final Decimal64 dec = Decimal64.valueOf("1.1");
        assertThrows(ArithmeticException.class, () -> dec.intValueExact());
    }

    @Test
    public void testIntValueExactRange() {
        final Decimal64 dec = Decimal64.valueOf(Integer.MAX_VALUE + 1L);
        assertThrows(ArithmeticException.class, () -> dec.intValueExact());
    }

    @Test
    public void testLongValueExactFrac() {
        final Decimal64 dec = Decimal64.valueOf("1.1");
        assertThrows(ArithmeticException.class, () -> dec.longValueExact());
    }

    private static void assertCanonicalVariants(final String str, final long intPart, final long fracPart,
            final int digits) {
        assertCanonicalString(str, intPart, fracPart, digits, false);
        assertCanonicalString("-" + str, intPart, fracPart, digits, true);

        final Decimal64 parsed = assertParsedString("+" + str, intPart, fracPart, digits, false);
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
        final Decimal64 parsed = assertParsedString(str, intPart, fracPart, digits, negative);
        assertEquals(str, parsed.toString());
    }

    private static Decimal64 assertParsedString(final String str, final long intPart, final long fracPart,
            final int digits, final boolean negative) {
        final Decimal64 parsed = Decimal64.valueOf(str);
        assertEquals(new Decimal64((byte) digits, intPart, fracPart, negative), parsed);
        return parsed;
    }
}
