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

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.model.api.stmt.MaxElementsArgument.Unbounded;

class MaxElementsArgumentTest {
    @Test
    void unboundedRecognized() {
        final var arg = assertInstanceOf(Unbounded.class, MaxElementsArgument.ofArgument("unbounded"));
        assertEquals("unbounded", arg.toString());
    }

    @Test
    void malformedUnboundedThrows() {
        final var ex = assertThrowsExactly(IllegalArgumentException.class,
            () -> MaxElementsArgument.ofArgument("unbounded "));
        assertEquals("'unbounded ' is not a valid max-value-arg on position 1: 'u' is not a valid non-zero-digit",
            ex.getMessage());
    }

    @Test
    void emptyThrows() {
        final var ex = assertThrowsExactly(IllegalArgumentException.class, () -> MaxElementsArgument.ofArgument(""));
        assertEquals("empty max-value-arg", ex.getMessage());
    }

    @Test
    void negativeThrows() {
        final var ex = assertThrowsExactly(IllegalArgumentException.class, () -> MaxElementsArgument.ofArgument("-1"));
        assertEquals("'-1' is not a valid max-value-arg on position 1: '-' is not a valid non-zero-digit",
            ex.getMessage());
    }

    @Test
    void zeroThrows() {
        final var ex = assertThrowsExactly(IllegalArgumentException.class, () -> MaxElementsArgument.ofArgument("0"));
        assertEquals("'0' is not a valid max-value-arg on position 1: '0' is not a valid non-zero-digit",
            ex.getMessage());
    }

    @Test
    void badThrows() {
        final var ex = assertThrowsExactly(IllegalArgumentException.class, () -> MaxElementsArgument.ofArgument("1a"));
        assertEquals("'1a' is not a valid max-value-arg on position 2: 'a' is not a valid DIGIT", ex.getMessage());
    }

    @Test
    void intParses() {
        final var arg = assertInstanceOf(MaxElementsArgument32.class, MaxElementsArgument.ofArgument("2147483647"));
        assertEquals("2147483647", arg.toString());
        assertEquals(Integer.MAX_VALUE, arg.asSaturatedInt());
        assertEquals(Integer.MAX_VALUE, arg.asSaturatedLong());
    }

    @Test
    void longParses() {
        final var arg = assertInstanceOf(MaxElementsArgument64.class,
            MaxElementsArgument.ofArgument("9223372036854775807"));
        assertEquals("9223372036854775807", arg.toString());
        assertEquals(Integer.MAX_VALUE, arg.asSaturatedInt());
        assertEquals(Long.MAX_VALUE, arg.asSaturatedLong());
    }

    @Test
    void bigParses() {
        final var arg = assertInstanceOf(MaxElementsArgumentBig.class,
            MaxElementsArgument.ofArgument("92233720368547758070"));
        assertEquals("92233720368547758070", arg.toString());
        assertEquals(Integer.MAX_VALUE, arg.asSaturatedInt());
        assertEquals(Long.MAX_VALUE, arg.asSaturatedLong());
    }
}
