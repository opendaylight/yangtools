/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.Decimal64;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;

class NumberUtilTest {
    @Test
    void testRangeCoveredForShort() {
        final short min = 100;
        final short superMin = 50;
        final short max = 200;
        final short superMax = 300;

        assertTrue(NumberUtil.isRangeCovered(min, max, superMin, superMax));
    }

    @Test
    void testRangeCoveredForLong() {
        final long min = 100L;
        final long superMin = 50L;
        final long max = 200L;
        final long superMax = 300L;

        assertTrue(NumberUtil.isRangeCovered(min, max, superMin, superMax));
    }

    @Test
    void testRangeCoveredForDecimal64() {
        final Decimal64 min = Decimal64.valueOf("100.0");
        final Decimal64 superMin = Decimal64.valueOf("50.0");
        final Decimal64 max = Decimal64.valueOf("200.0");
        final Decimal64 superMax = Decimal64.valueOf("300.0");

        assertTrue(NumberUtil.isRangeCovered(min, max, superMin, superMax));
    }

    @Test
    void testRangeCoveredForUint8() {
        final Uint64 min = Uint64.valueOf("100");
        final Uint64 superMin = Uint64.valueOf("50");
        final Uint64 max = Uint64.valueOf("200");
        final Uint64 superMax = Uint64.valueOf("250");

        assertTrue(NumberUtil.isRangeCovered(min, max, superMin, superMax));
    }

    @Test
    void testRangeCoveredForUint16() {
        final Uint16 min = Uint16.valueOf("100");
        final Uint16 superMin = Uint16.valueOf("50");
        final Uint16 max = Uint16.valueOf("200");
        final Uint16 superMax = Uint16.valueOf("300");

        assertTrue(NumberUtil.isRangeCovered(min, max, superMin, superMax));
    }

    @Test
    void testRangeCoveredForUint32() {
        final Uint32 min = Uint32.valueOf("100");
        final Uint32 superMin = Uint32.valueOf("50");
        final Uint32 max = Uint32.valueOf("200");
        final Uint32 superMax = Uint32.valueOf("300");

        assertTrue(NumberUtil.isRangeCovered(min, max, superMin, superMax));
    }

    @Test
    void testRangeCoveredForUint64() {
        final Uint64 min = Uint64.valueOf("100");
        final Uint64 superMin = Uint64.valueOf("50");
        final Uint64 max = Uint64.valueOf("200");
        final Uint64 superMax = Uint64.valueOf("300");

        assertTrue(NumberUtil.isRangeCovered(min, max, superMin, superMax));
    }

    @Test
    void testRangeCoveredForUnsupportedNumberType() {
        final double min = 100.0;
        final double superMin = 50.0;
        final double max = 200.0;
        final double superMax = 300.0;

        assertThrows(IllegalArgumentException.class, () -> NumberUtil.isRangeCovered(min, max, superMin, superMax));
    }

    @Test
    void testConverterToShort() {
        final Short shortNum = 20;
        final var numberFunction = NumberUtil.converterTo(Short.class);
        assertEquals(shortNum, numberFunction.apply(shortNum));

        final byte byteNum = 20;
        assertEquals(shortNum, numberFunction.apply(byteNum));

        final int intNum = 20;
        assertEquals(shortNum, numberFunction.apply(intNum));
    }

    @Test
    void testConverterToInteger() {
        final Integer intNum = 20;
        final byte byteNum = 20;
        final var numberFunction = NumberUtil.converterTo(Integer.class);
        assertEquals(intNum, numberFunction.apply(byteNum));
    }

    @Test
    void testConverterToLong() {
        final Long longNum = 20L;
        final var numberFunction = NumberUtil.converterTo(Long.class);
        assertEquals(longNum, numberFunction.apply(longNum));

        final byte byteNum = 20;
        assertEquals(longNum, numberFunction.apply(byteNum));

        final BigInteger bigIntNum = new BigInteger("20");
        assertEquals(longNum, numberFunction.apply(bigIntNum));
    }

    @Test
    void testConverterToBigDecimal() {
        final Decimal64 bigDecNum = Decimal64.valueOf("20.0");
        final var numberFunction = NumberUtil.converterTo(Decimal64.class);
        assertEquals(bigDecNum, numberFunction.apply(bigDecNum));

        int intNum = 20;
        assertEquals(bigDecNum, numberFunction.apply(intNum));

        double doubleNum = 20.0;
        assertEquals(bigDecNum, numberFunction.apply(doubleNum));
    }

    @Test
    void testConverterToUint64() {
        final Uint64 bigIntNum = Uint64.valueOf("20");
        final var numberFunction = NumberUtil.converterTo(Uint64.class);
        assertEquals(bigIntNum, numberFunction.apply(bigIntNum));

        final int intNum = 20;
        assertEquals(bigIntNum, numberFunction.apply(intNum));

        final BigDecimal bigDecNum = new BigDecimal(20.0);
        assertEquals(bigIntNum, numberFunction.apply(bigDecNum));
    }
}
