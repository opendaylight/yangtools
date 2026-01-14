/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

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
}
