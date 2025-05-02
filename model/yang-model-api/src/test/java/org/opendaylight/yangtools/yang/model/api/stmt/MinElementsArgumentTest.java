/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.text.ParseException;
import org.junit.jupiter.api.Test;

class MinElementsArgumentTest {
    @Test
    void emptyThrows() {
        final var ex = assertThrowsExactly(ParseException.class, () -> MinElementsArgument.parse(""));
        assertEquals("empty min-value-arg", ex.getMessage());
        assertEquals(0, ex.getErrorOffset());
    }

    @Test
    void negativeThrows() {
        final var ex = assertThrowsExactly(ParseException.class, () -> MinElementsArgument.parse("-1"));
        assertEquals("'-' is not a valid non-zero-digit", ex.getMessage());
        assertEquals(0, ex.getErrorOffset());
    }

    @Test
    void moreThanNineThrows() {
        final var ex = assertThrowsExactly(ParseException.class, () -> MinElementsArgument.parse("1a"));
        assertEquals("'a' is not a valid DIGIT", ex.getMessage());
        assertEquals(1, ex.getErrorOffset());
    }

    @Test
    void lessThanZeroThrows() {
        final var ex = assertThrowsExactly(ParseException.class, () -> MinElementsArgument.parse("1/"));
        assertEquals("'/' is not a valid DIGIT", ex.getMessage());
        assertEquals(1, ex.getErrorOffset());
    }

    @Test
    void oneMoreThanNineThrows() {
        final var ex = assertThrowsExactly(ParseException.class, () -> MinElementsArgument.parse("a"));
        assertEquals("'a' is not a valid DIGIT", ex.getMessage());
        assertEquals(0, ex.getErrorOffset());
    }

    @Test
    void oneLessThanZeroThrows() {
        final var ex = assertThrowsExactly(ParseException.class, () -> MinElementsArgument.parse("/"));
        assertEquals("'/' is not a valid DIGIT", ex.getMessage());
        assertEquals(0, ex.getErrorOffset());
    }

    @Test
    void zeroOneThrows() {
        final var ex = assertThrowsExactly(ParseException.class, () -> MinElementsArgument.parse("01"));
        assertEquals("'0' is not a valid non-zero-digit", ex.getMessage());
        assertEquals(0, ex.getErrorOffset());
    }

    @Test
    void zeroParses() throws Exception {
        final var arg = assertInstanceOf(MinElementsArgument32.class, MinElementsArgument.parse("0"));
        assertEquals("0", arg.toString());
        assertEquals(-1, arg.lowerInt());
        assertEquals(-1L, arg.lowerLong());
        assertEquals(BigInteger.ONE.negate(), arg.lowerBig());
    }

    @Test
    void intParses() throws Exception {
        final var arg = assertInstanceOf(MinElementsArgument32.class, MinElementsArgument.parse("2147483647"));
        assertEquals("2147483647", arg.toString());
        assertEquals(2147483646, arg.lowerInt());
        assertEquals(2147483646L, arg.lowerLong());
        assertEquals(BigInteger.valueOf(2147483646), arg.lowerBig());
    }

    @Test
    void longParses() throws Exception {
        final var arg = assertInstanceOf(MinElementsArgument64.class, MinElementsArgument.parse("9223372036854775807"));
        assertEquals("9223372036854775807", arg.toString());
        assertEquals(2147483647, arg.lowerInt());
        assertEquals(9223372036854775806L, arg.lowerLong());
        assertEquals(BigInteger.valueOf(9223372036854775806L), arg.lowerBig());
    }

    @Test
    void bigParses() throws Exception {
        final var arg = assertInstanceOf(MinElementsArgumentBig.class,
            MinElementsArgument.parse("92233720368547758070"));
        assertEquals("92233720368547758070", arg.toString());
        assertEquals(Integer.MAX_VALUE, arg.lowerInt());
        assertEquals(Long.MAX_VALUE, arg.lowerLong());
        assertEquals(new BigInteger("92233720368547758069"), arg.lowerBig());
    }

    @Test
    void ofMinusOneBig() {
        final var ex = assertThrowsExactly(IllegalArgumentException.class,
            () -> MinElementsArgument.of(BigInteger.ONE.negate()));
        assertEquals("negative value -1", ex.getMessage());
    }

    @Test
    void ofMinusOneLong() {
        final var ex = assertThrowsExactly(IllegalArgumentException.class, () -> MinElementsArgument.of(-1L));
        assertEquals("negative value -1", ex.getMessage());
    }

    @Test
    void ofMinusOneInt() {
        final var ex = assertThrowsExactly(IllegalArgumentException.class, () -> MinElementsArgument.of(-1));
        assertEquals("negative value -1", ex.getMessage());
    }

    @Test
    void intMatches() {
        final var arg = MinElementsArgument.of(5);
        assertFalse(arg.matches(4));
        assertFalse(arg.matches(3L));
        assertTrue(arg.matches(BigInteger.TEN));
    }

    @Test
    void longMatches() {
        final var arg = MinElementsArgument.of(1L + Integer.MAX_VALUE);
        assertFalse(arg.matches(Integer.MAX_VALUE));
        assertTrue(arg.matches(1L + Integer.MAX_VALUE));
        assertTrue(arg.matches(BigInteger.valueOf(Integer.MAX_VALUE).add(BigInteger.TWO)));
    }

    @Test
    void bigMatches() {
        final var arg = MinElementsArgument.of(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE));
        assertFalse(arg.matches(Integer.MAX_VALUE));
        assertFalse(arg.matches(1L + Integer.MAX_VALUE));
        assertTrue(arg.matches(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.TWO)));
    }

    @Test
    void compareToWorks() {
        final var intOne = MinElementsArgument.of(1);
        assertInstanceOf(MinElementsArgument32.class, intOne);
        assertThat(intOne).isEqualByComparingTo(intOne);

        final var intTwo = MinElementsArgument.of(2);
        assertInstanceOf(MinElementsArgument32.class, intTwo);
        assertThat(intTwo).isEqualByComparingTo(intTwo);

        assertThat(intOne).isLessThan(intTwo);
        assertThat(intTwo).isGreaterThan(intOne);

        final var longOne = MinElementsArgument.of(1L + Integer.MAX_VALUE);
        assertInstanceOf(MinElementsArgument64.class, longOne);
        assertThat(longOne).isEqualByComparingTo(longOne);
        assertThat(intOne).isLessThan(longOne);
        assertThat(longOne).isGreaterThan(intOne);

        final var longTwo = MinElementsArgument.of(2L + Integer.MAX_VALUE);
        assertInstanceOf(MinElementsArgument64.class, longTwo);
        assertThat(longTwo).isEqualByComparingTo(longTwo);

        assertThat(longOne).isLessThan(longTwo);
        assertThat(longTwo).isGreaterThan(longOne);

        final var bigOne = MinElementsArgument.of(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE));
        assertInstanceOf(MinElementsArgumentBig.class, bigOne);
        assertThat(bigOne).isEqualByComparingTo(bigOne);
        assertThat(intOne).isLessThan(bigOne);
        assertThat(longOne).isLessThan(bigOne);
        assertThat(bigOne).isGreaterThan(intOne);
        assertThat(bigOne).isGreaterThan(longOne);

        final var bigTwo = MinElementsArgument.of(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.TWO));
        assertInstanceOf(MinElementsArgumentBig.class, bigTwo);
        assertThat(bigTwo).isEqualByComparingTo(bigTwo);
        assertThat(intTwo).isLessThan(bigTwo);
        assertThat(longTwo).isLessThan(bigTwo);
        assertThat(bigTwo).isGreaterThan(intTwo);
        assertThat(bigTwo).isGreaterThan(longTwo);

        assertThat(bigOne).isLessThan(bigTwo);
        assertThat(bigTwo).isGreaterThan(bigOne);
    }
}
