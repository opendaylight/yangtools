/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.RoundingMode;
import org.junit.jupiter.api.Test;

class YT1440Test {
    @Test
    void testScaleSame() {
        final var twenty = Decimal64.valueOf(2, 20);
        assertSame(twenty, twenty.scaleTo(2));

        // Do not tolerate null rounding even for no-op
        assertThrows(NullPointerException.class, () -> twenty.scaleTo(2, null));
    }

    @Test
    void testScaleZero() {
        final var two = Decimal64.valueOf(2, 0);
        final var one = two.scaleTo(1);
        assertEquals(1, one.scale());
        assertEquals(0, one.unscaledValue());
        final var three = two.scaleTo(3);
        assertEquals(3, three.scale());
        assertEquals(0, three.unscaledValue());
    }

    @Test
    void testScaleUpNoRemain() {
        // Template, scale=5
        final var two = Decimal64.valueOf(2, 20);

        // scale = 5
        final var five = two.scaleTo(5);
        assertEquals(5, five.scale());
        assertEquals(two, five);
        assertEquals("20.0", five.toString());

        // scale = 18 fails
        final var ex = assertThrows(ArithmeticException.class, () -> two.scaleTo(18));
        assertEquals("Increasing scale of 20.0 to 18 would overflow", ex.getMessage());
    }

    @Test
    void testScaleDownNoRemain() {
        // Template, scale=5
        final var five = Decimal64.valueOf(5, 20);

        // scale = 2
        final var two = five.scaleTo(2);
        assertEquals(2, two.scale());
        assertEquals(five, two);
        assertEquals("20.0", two.toString());
    }

    @Test
    void testScaleDownPositive() {
        final var two = Decimal64.valueOf("0.63");
        assertEquals(2, two.scale());
        assertEquals(63, two.unscaledValue());

        // Trim '3'
        assertScaleDown(7, two, 1, RoundingMode.UP);
        assertScaleDown(6, two, 1, RoundingMode.DOWN);
        assertScaleDown(7, two, 1, RoundingMode.CEILING);
        assertScaleDown(6, two, 1, RoundingMode.FLOOR);
        assertScaleDown(6, two, 1, RoundingMode.HALF_UP);
        assertScaleDown(6, two, 1, RoundingMode.HALF_DOWN);
        assertScaleDown(6, two, 1, RoundingMode.HALF_EVEN);

        final var three = Decimal64.valueOf("0.635");
        assertEquals(3, three.scale());
        assertEquals(635, three.unscaledValue());

        // Trim '5'
        assertScaleDown(64, three, 2, RoundingMode.UP);
        assertScaleDown(63, three, 2, RoundingMode.DOWN);
        assertScaleDown(64, three, 2, RoundingMode.CEILING);
        assertScaleDown(63, three, 2, RoundingMode.FLOOR);
        assertScaleDown(64, three, 2, RoundingMode.HALF_UP);
        assertScaleDown(63, three, 2, RoundingMode.HALF_DOWN);
        assertScaleDown(64, three, 2, RoundingMode.HALF_EVEN);

        // Trim '35'
        assertScaleDown(7, three, 1, RoundingMode.UP);
        assertScaleDown(6, three, 1, RoundingMode.DOWN);
        assertScaleDown(7, three, 1, RoundingMode.CEILING);
        assertScaleDown(6, three, 1, RoundingMode.FLOOR);
        assertScaleDown(6, three, 1, RoundingMode.HALF_UP);
        assertScaleDown(6, three, 1, RoundingMode.HALF_DOWN);
        assertScaleDown(6, three, 1, RoundingMode.HALF_EVEN);

        final var four = Decimal64.valueOf("0.6355");
        assertEquals(4, four.scale());
        assertEquals(6355, four.unscaledValue());

        // Trim 55
        assertScaleDown(64, four, 2, RoundingMode.UP);
        assertScaleDown(63, four, 2, RoundingMode.DOWN);
        assertScaleDown(64, four, 2, RoundingMode.CEILING);
        assertScaleDown(63, four, 2, RoundingMode.FLOOR);
        assertScaleDown(64, four, 2, RoundingMode.HALF_UP);
        assertScaleDown(64, four, 2, RoundingMode.HALF_DOWN);
        assertScaleDown(64, four, 2, RoundingMode.HALF_EVEN);

        final var five = Decimal64.valueOf("0.635").scaleTo(5);
        assertEquals(5, five.scale());
        assertEquals(63500, five.unscaledValue());

        // Trim 500
        assertScaleDown(64, five, 2, RoundingMode.UP);
        assertScaleDown(63, five, 2, RoundingMode.DOWN);
        assertScaleDown(64, five, 2, RoundingMode.CEILING);
        assertScaleDown(63, five, 2, RoundingMode.FLOOR);
        assertScaleDown(64, five, 2, RoundingMode.HALF_UP);
        assertScaleDown(63, five, 2, RoundingMode.HALF_DOWN);
        assertScaleDown(64, five, 2, RoundingMode.HALF_EVEN);
    }

    @Test
    void testScaleDownNegative() {
        final var two = Decimal64.valueOf("-0.63");
        assertEquals(2, two.scale());
        assertEquals(-63, two.unscaledValue());

        // Trim '3'
        assertScaleDown(-7, two, 1, RoundingMode.UP);
        assertScaleDown(-6, two, 1, RoundingMode.DOWN);
        assertScaleDown(-6, two, 1, RoundingMode.CEILING);
        assertScaleDown(-7, two, 1, RoundingMode.FLOOR);
        assertScaleDown(-6, two, 1, RoundingMode.HALF_UP);
        assertScaleDown(-6, two, 1, RoundingMode.HALF_DOWN);
        assertScaleDown(-6, two, 1, RoundingMode.HALF_EVEN);

        final var three = Decimal64.valueOf("-0.635");
        assertEquals(3, three.scale());
        assertEquals(-635, three.unscaledValue());

        // Trim '5'
        assertScaleDown(-64, three, 2, RoundingMode.UP);
        assertScaleDown(-63, three, 2, RoundingMode.DOWN);
        assertScaleDown(-63, three, 2, RoundingMode.CEILING);
        assertScaleDown(-64, three, 2, RoundingMode.FLOOR);
        assertScaleDown(-64, three, 2, RoundingMode.HALF_UP);
        assertScaleDown(-63, three, 2, RoundingMode.HALF_DOWN);
        assertScaleDown(-64, three, 2, RoundingMode.HALF_EVEN);

        // Trim '35'
        assertScaleDown(-7, three, 1, RoundingMode.UP);
        assertScaleDown(-6, three, 1, RoundingMode.DOWN);
        assertScaleDown(-6, three, 1, RoundingMode.CEILING);
        assertScaleDown(-7, three, 1, RoundingMode.FLOOR);
        assertScaleDown(-6, three, 1, RoundingMode.HALF_UP);
        assertScaleDown(-6, three, 1, RoundingMode.HALF_DOWN);
        assertScaleDown(-6, three, 1, RoundingMode.HALF_EVEN);

        final var four = Decimal64.valueOf("-0.6355");
        assertEquals(4, four.scale());
        assertEquals(-6355, four.unscaledValue());

        // Trim 55
        assertScaleDown(-64, four, 2, RoundingMode.UP);
        assertScaleDown(-63, four, 2, RoundingMode.DOWN);
        assertScaleDown(-63, four, 2, RoundingMode.CEILING);
        assertScaleDown(-64, four, 2, RoundingMode.FLOOR);
        assertScaleDown(-64, four, 2, RoundingMode.HALF_UP);
        assertScaleDown(-64, four, 2, RoundingMode.HALF_DOWN);
        assertScaleDown(-64, four, 2, RoundingMode.HALF_EVEN);

        final var five = Decimal64.valueOf("-0.635").scaleTo(5);
        assertEquals(5, five.scale());
        assertEquals(-63500, five.unscaledValue());

        // Trim 500
        assertScaleDown(-64, five, 2, RoundingMode.UP);
        assertScaleDown(-63, five, 2, RoundingMode.DOWN);
        assertScaleDown(-63, five, 2, RoundingMode.CEILING);
        assertScaleDown(-64, five, 2, RoundingMode.FLOOR);
        assertScaleDown(-64, five, 2, RoundingMode.HALF_UP);
        assertScaleDown(-63, five, 2, RoundingMode.HALF_DOWN);
        assertScaleDown(-64, five, 2, RoundingMode.HALF_EVEN);
    }

    @Test
    void testScaleDownTrim() {
        final var two = Decimal64.valueOf("0.63");
        final var ex = assertThrows(ArithmeticException.class, () -> two.scaleTo(1));
        assertEquals("Decreasing scale of 0.63 to 1 requires rounding", ex.getMessage());
    }

    private static void assertScaleDown(final long expectedUnscaled, final Decimal64 value, final int scale,
            final RoundingMode mode) {
        final var scaled = value.scaleTo(scale, mode);
        assertEquals(scale, scaled.scale());
        assertEquals(expectedUnscaled, scaled.unscaledValue());
    }
}
