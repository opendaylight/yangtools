/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.model.util.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Function;
import org.junit.Test;

public class NumberUtilTest {

    @Test
    public void testRangeCoveredForShort() {
        final short min = 100;
        final short superMin = 50;
        final short max = 200;
        final short superMax = 300;

        assertTrue(NumberUtil.isRangeCovered(min, max, superMin, superMax));
    }

    @Test
    public void testRangeCoveredForLong() {
        final long min = 100L;
        final long superMin = 50L;
        final long max = 200L;
        final long superMax = 300L;

        assertTrue(NumberUtil.isRangeCovered(min, max, superMin, superMax));
    }

    @Test
    public void testRangeCoveredForBigDecimal() {
        final BigDecimal min = new BigDecimal(100.0);
        final BigDecimal superMin = new BigDecimal(50.0);
        final BigDecimal max = new BigDecimal(200.0);
        final BigDecimal superMax = new BigDecimal(300.0);

        assertTrue(NumberUtil.isRangeCovered(min, max, superMin, superMax));
    }

    @Test
    public void testRangeCoveredForBigInteger() {
        final BigInteger min = new BigInteger("100");
        final BigInteger superMin = new BigInteger("50");
        final BigInteger max = new BigInteger("200");
        final BigInteger superMax = new BigInteger("300");

        assertTrue(NumberUtil.isRangeCovered(min, max, superMin, superMax));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRangeCoveredForUnsupportedNumberType() {
        final double min = 100.0;
        final double superMin = 50.0;
        final double max = 200.0;
        final double superMax = 300.0;

        NumberUtil.isRangeCovered(min, max, superMin, superMax);
    }

    @Test
    public void testConverterToShort() {
        final Short shortNum = 20;
        final Function<Number, Short> numberFunction = NumberUtil.converterTo(Short.class);
        assertEquals(shortNum, numberFunction.apply(shortNum));

        final byte byteNum = 20;
        assertEquals(shortNum, numberFunction.apply(byteNum));

        final int intNum = 20;
        assertEquals(shortNum, numberFunction.apply(intNum));
    }

    @Test
    public void testConverterToInteger() {
        final Integer intNum = 20;
        final byte byteNum = 20;
        final Function<Number, Integer> numberFunction = NumberUtil.converterTo(Integer.class);
        assertEquals(intNum, numberFunction.apply(byteNum));
    }

    @Test
    public void testConverterToLong() {
        final Long longNum = 20L;
        final Function<Number, Long> numberFunction = NumberUtil.converterTo(Long.class);
        assertEquals(longNum, numberFunction.apply(longNum));

        final byte byteNum = 20;
        assertEquals(longNum, numberFunction.apply(byteNum));

        final BigInteger bigIntNum = new BigInteger("20");
        assertEquals(longNum, numberFunction.apply(bigIntNum));
    }

    @Test
    public void testConverterToBigDecimal() {
        BigDecimal bigDecNum = new BigDecimal(20.0);
        final Function<Number, BigDecimal> numberFunction = NumberUtil.converterTo(BigDecimal.class);
        assertEquals(bigDecNum, numberFunction.apply(bigDecNum));

        int intNum = 20;
        assertEquals(bigDecNum, numberFunction.apply(intNum));

        double doubleNum = 20.0;
        bigDecNum = new BigDecimal("20.0");
        assertEquals(bigDecNum, numberFunction.apply(doubleNum));
    }

    @Test public void testConverterToBigInteger() {
        final BigInteger bigIntNum = new BigInteger("20");
        final Function<Number, BigInteger> numberFunction = NumberUtil.converterTo(BigInteger.class);
        assertEquals(bigIntNum, numberFunction.apply(bigIntNum));

        final int intNum = 20;
        assertEquals(bigIntNum, numberFunction.apply(intNum));

        final BigDecimal bigDecNum = new BigDecimal(20.0);
        assertEquals(bigIntNum, numberFunction.apply(bigDecNum));
    }
}
