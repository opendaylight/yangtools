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
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import org.junit.Test;

public class Decimal64Test {

    @Test(expected = NumberFormatException.class)
    public void testParseEmpty() {
        Decimal64.parse("");
    }

    @Test(expected = NumberFormatException.class)
    public void testParseSingleIllegal() {
        Decimal64.parse("a");
    }

    @Test(expected = NumberFormatException.class)
    public void testParseSingleHighIllegal() {
        Decimal64.parse(":");
    }

    @Test(expected = NumberFormatException.class)
    public void testParseZeroIllegal() {
        Decimal64.parse("0a");
    }

    @Test(expected = NumberFormatException.class)
    public void testParseZeroHighIllegal() {
        Decimal64.parse("0:");
    }

    @Test(expected = NumberFormatException.class)
    public void testParseZeroPointIllegal() {
        Decimal64.parse("0.a");
    }

    @Test(expected = NumberFormatException.class)
    public void testParseZeroPointHighIllegal() {
        Decimal64.parse("0.:");
    }

    @Test(expected = NumberFormatException.class)
    public void testParsePointIllegal() {
        Decimal64.parse(".a");
    }

    @Test(expected = NullPointerException.class)
    public void testParseNull() {
        Decimal64.parse(null);
    }

    @Test(expected = NumberFormatException.class)
    public void testParseMinus() {
        Decimal64.parse("-");
    }

    @Test(expected = NumberFormatException.class)
    public void testParsePlus() {
        Decimal64.parse("+");
    }

    @Test(expected = NumberFormatException.class)
    public void testParsePeriod() {
        Decimal64.parse(".");
    }

    @Test(expected = NumberFormatException.class)
    public void testParseTwoPeriods() {
        Decimal64.parse("..");
    }

    @Test(expected = NumberFormatException.class)
    public void testParseTrailingPeriod() {
        Decimal64.parse("0.");
    }

    @Test(expected = NumberFormatException.class)
    public void testParseMultiplePeriods() {
        Decimal64.parse("0.1.");
    }

    @Test
    public void testParseLongString() {
        Decimal64.parse("123456789012345678");
    }

    @Test
    public void testParseLongDecimal() {
        Decimal64.parse("0.12345678901234568");
    }

    @Test(expected = NumberFormatException.class)
    public void testParseTooLongString() {
        Decimal64.parse("1234567890123456789");
    }

    @Test(expected = NumberFormatException.class)
    public void testParseTooLongDecimal() {
        Decimal64.parse("0.123456789012345689");
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
        final Decimal64 one = Decimal64.parse("1");
        final Decimal64 two = Decimal64.parse("2");
        final Decimal64 three = Decimal64.parse("3");
        final Decimal64 negOne = Decimal64.parse("-1");
        final Decimal64 anotherOne = Decimal64.parse("1");

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
        final Decimal64 one = Decimal64.parse("1");
        final Decimal64 two = Decimal64.parse("2");
        final Decimal64 anotherOne = Decimal64.parse("1");

        assertTrue(one.equals(one));
        assertTrue(one.equals(anotherOne));
        assertFalse(one.equals(two));
        assertTrue(two.equals(two));
        assertFalse(two.equals(one));

        assertFalse(one.equals(new Object()));
    }

    @Test
    public void testConversions() {
        assertEquals(new BigDecimal("0.12"), Decimal64.parse("0.12").decimalValue());
        assertEquals(new BigDecimal("-0.12"), Decimal64.parse("-0.12").decimalValue());
        assertEquals(new BigDecimal("0.12"), Decimal64.parse("+0.12").decimalValue());
        assertEquals(new BigDecimal("123.456"), Decimal64.parse("123.456").decimalValue());
        assertEquals(new BigDecimal("-123.456"), Decimal64.parse("-123.456").decimalValue());

        assertEquals(0.12, Decimal64.parse("0.12").doubleValue(), 0);
        assertEquals(-0.12, Decimal64.parse("-0.12").doubleValue(), 0);

        assertEquals((float) 0.12, Decimal64.parse("0.12").floatValue(), 0);
        assertEquals((float) -0.12, Decimal64.parse("-0.12").floatValue(), 0);

        assertEquals(12345678901L, Decimal64.parse("12345678901").longValue());
        assertEquals(-12345678901L, Decimal64.parse("-12345678901").longValue());
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
        final Decimal64 parsed = Decimal64.parse(str);
        assertEquals(new Decimal64((byte) digits, intPart, fracPart, negative), parsed);
        return parsed;
    }
}
