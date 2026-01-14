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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.text.ParseException;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.model.api.stmt.MaxElementsArgument.Unbounded;

class MaxElementsArgumentTest {
    @Test
    void unboundedRecognized() throws Exception {
        final var arg = assertInstanceOf(Unbounded.class, MaxElementsArgument.parse("unbounded"));
        assertEquals("unbounded", arg.toString());
    }

    @Test
    void malformedUnboundedThrows() {
        final var ex = assertThrowsExactly(ParseException.class, () -> MaxElementsArgument.parse("unbounded "));
        assertEquals("'u' is not a valid non-zero-digit", ex.getMessage());
        assertEquals(0, ex.getErrorOffset());
    }

    @Test
    void emptyThrows() {
        final var ex = assertThrowsExactly(ParseException.class, () -> MaxElementsArgument.parse(""));
        assertEquals("empty max-value-arg", ex.getMessage());
        assertEquals(0, ex.getErrorOffset());
    }

    @Test
    void negativeThrows() {
        final var ex = assertThrowsExactly(ParseException.class, () -> MaxElementsArgument.parse("-1"));
        assertEquals("'-' is not a valid non-zero-digit", ex.getMessage());
        assertEquals(0, ex.getErrorOffset());
    }

    @Test
    void zeroThrows() {
        final var ex = assertThrowsExactly(ParseException.class, () -> MaxElementsArgument.parse("0"));
        assertEquals("'0' is not a valid non-zero-digit", ex.getMessage());
        assertEquals(0, ex.getErrorOffset());
    }

    @Test
    void badThrows() {
        final var ex = assertThrowsExactly(ParseException.class, () -> MaxElementsArgument.parse("1a"));
        assertEquals("'a' is not a valid DIGIT", ex.getMessage());
        assertEquals(1, ex.getErrorOffset());
    }

    @Test
    void intParses() throws Exception {
        final var arg = assertInstanceOf(MaxElementsArgument32.class, MaxElementsArgument.parse("2147483647"));
        assertEquals("2147483647", arg.toString());
        assertEquals(Integer.MAX_VALUE, arg.asSaturatedInt());
        assertEquals(Integer.MAX_VALUE, arg.asSaturatedLong());
    }

    @Test
    void longParses() throws Exception {
        final var arg = assertInstanceOf(MaxElementsArgument64.class, MaxElementsArgument.parse("9223372036854775807"));
        assertEquals("9223372036854775807", arg.toString());
        assertEquals(Integer.MAX_VALUE, arg.asSaturatedInt());
        assertEquals(Long.MAX_VALUE, arg.asSaturatedLong());
    }

    @Test
    void bigParses() throws Exception {
        final var arg = assertInstanceOf(MaxElementsArgumentBig.class,
            MaxElementsArgument.parse("92233720368547758070"));
        assertEquals("92233720368547758070", arg.toString());
        assertEquals(Integer.MAX_VALUE, arg.asSaturatedInt());
        assertEquals(Long.MAX_VALUE, arg.asSaturatedLong());
    }

    @Test
    void ofZeroBig() {
        final var ex = assertThrowsExactly(IllegalArgumentException.class,
            () -> MaxElementsArgument.of(BigInteger.ZERO));
        assertEquals("non-positive value 0", ex.getMessage());
    }

    @Test
    void ofZeroLong() {
        final var ex = assertThrowsExactly(IllegalArgumentException.class, () -> MaxElementsArgument.of(0L));
        assertEquals("non-positive value 0", ex.getMessage());
    }

    @Test
    void ofZeroInt() {
        final var ex = assertThrowsExactly(IllegalArgumentException.class, () -> MaxElementsArgument.of(0));
        assertEquals("non-positive value 0", ex.getMessage());
    }

    @Test
    void unboundedMatches() {
        final var unbounded = MaxElementsArgument.of();
        assertTrue(unbounded.matches(0));
        assertTrue(unbounded.matches(1L));
        assertTrue(unbounded.matches(BigInteger.TEN));
    }

    @Test
    void unboundedMethods() {
        final var unbounded = MaxElementsArgument.of();
        assertEquals(Integer.MAX_VALUE, unbounded.asSaturatedInt());
        assertEquals(Long.MAX_VALUE, unbounded.asSaturatedLong());
        assertSame(unbounded, unbounded.intern());
    }

    @Test
    void intMatches() {
        final var arg = MaxElementsArgument.of(5);
        assertTrue(arg.matches(4));
        assertTrue(arg.matches(3L));
        assertFalse(arg.matches(BigInteger.TEN));
    }

    @Test
    void longMatches() {
        final var arg = MaxElementsArgument.of(1L + Integer.MAX_VALUE);
        assertTrue(arg.matches(Integer.MAX_VALUE));
        assertTrue(arg.matches(1L + Integer.MAX_VALUE));
        assertFalse(arg.matches(BigInteger.valueOf(Integer.MAX_VALUE).add(BigInteger.TWO)));
    }


    @Test
    void bigMatches() {
        final var arg = MaxElementsArgument.of(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE));
        assertTrue(arg.matches(Integer.MAX_VALUE));
        assertTrue(arg.matches(1L + Integer.MAX_VALUE));
        assertFalse(arg.matches(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.TWO)));
    }

    @Test
    void compareToWorks() {
        final MaxElementsArgument unbounded = MaxElementsArgument.of();
        assertThat(unbounded).isEqualByComparingTo(unbounded);

        final MaxElementsArgument intOne = MaxElementsArgument.of(1);
        assertInstanceOf(MaxElementsArgument32.class, intOne);
        assertThat(intOne).isEqualByComparingTo(intOne);
        assertThat(intOne).isLessThan(unbounded);
        assertThat(unbounded).isGreaterThan(intOne);

        final MaxElementsArgument intTwo = MaxElementsArgument.of(2);
        assertInstanceOf(MaxElementsArgument32.class, intTwo);
        assertThat(intTwo).isEqualByComparingTo(intTwo);
        assertThat(intTwo).isLessThan(unbounded);
        assertThat(unbounded).isGreaterThan(intTwo);

        assertThat(intOne).isLessThan(intTwo);
        assertThat(intTwo).isGreaterThan(intOne);

        final MaxElementsArgument longOne = MaxElementsArgument.of(1L + Integer.MAX_VALUE);
        assertInstanceOf(MaxElementsArgument64.class, longOne);
        assertThat(longOne).isEqualByComparingTo(longOne);
        assertThat(longOne).isLessThan(unbounded);
        assertThat(unbounded).isGreaterThan(longOne);
        assertThat(intOne).isLessThan(longOne);
        assertThat(longOne).isGreaterThan(intOne);

        final MaxElementsArgument longTwo = MaxElementsArgument.of(2L + Integer.MAX_VALUE);
        assertInstanceOf(MaxElementsArgument64.class, longTwo);
        assertThat(longTwo).isEqualByComparingTo(longTwo);
        assertThat(longTwo).isLessThan(unbounded);
        assertThat(unbounded).isGreaterThan(longTwo);

        assertThat(longOne).isLessThan(longTwo);
        assertThat(longTwo).isGreaterThan(longOne);

        final MaxElementsArgument bigOne =
            MaxElementsArgument.of(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE));
        assertInstanceOf(MaxElementsArgumentBig.class, bigOne);
        assertThat(bigOne).isEqualByComparingTo(bigOne);
        assertThat(bigOne).isLessThan(unbounded);
        assertThat(unbounded).isGreaterThan(bigOne);
        assertThat(intOne).isLessThan(bigOne);
        assertThat(longOne).isLessThan(bigOne);
        assertThat(bigOne).isGreaterThan(intOne);
        assertThat(bigOne).isGreaterThan(longOne);

        final MaxElementsArgument bigTwo =
            MaxElementsArgument.of(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.TWO));
        assertInstanceOf(MaxElementsArgumentBig.class, bigTwo);
        assertThat(bigTwo).isEqualByComparingTo(bigTwo);
        assertThat(bigTwo).isLessThan(unbounded);
        assertThat(unbounded).isGreaterThan(bigTwo);
        assertThat(intTwo).isLessThan(bigTwo);
        assertThat(longTwo).isLessThan(bigTwo);
        assertThat(bigTwo).isGreaterThan(intTwo);
        assertThat(bigTwo).isGreaterThan(longTwo);

        assertThat(bigOne).isLessThan(bigTwo);
        assertThat(bigTwo).isGreaterThan(bigOne);
    }
}
